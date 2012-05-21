package nl.sison.xmpp;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

public class XMPPService extends Service {

	private static final String TAG = "XMPPService";
	private static final int XMPP_CONNECTED = 0;
	private static final int PI_REQUEST_CODE = 0; // Pending Intent request code
													// for filter?
	// // TODO figure out
	private NotificationManager notificationManager;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.local_service_started;

	public final static String KEY_CONNECTION_INDEX = "USTHUAS34027334H";

	private ConcurrentHashMap<Long, XMPPConnection> connection_hashmap;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		XMPPService getService() {
			makeToast("binding");
			return XMPPService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		makeToast("onCreate");
		makeConnectionsFromDatabase();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
	}

	private void makeConnectionsFromDatabase() {
		List<ConnectionConfigurationEntity> all_conns = getAllConnectionConfigurations();
		connection_hashmap = new ConcurrentHashMap<Long, XMPPConnection>();
		weakenNetworkOnMainThreadPolicy(); // TODO - remove and implement this
											// as asynctask or runnable when
											// debugging is
											// done
		for (final ConnectionConfigurationEntity cc : all_conns) {
			final String label = cc.getLabel();
			XMPPConnection connection;
			try {
				connection = connectToServer(cc);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				connection = null;
			}
			if (connection != null) {
				connection_hashmap.put(cc.getId(), connection);
				connection.addConnectionListener(new ConnectionListener() {

					public void reconnectionSuccessful() {
						makeToast(label + ": Reconnection successful");
					}

					public void reconnectionFailed(Exception arg0) {
						makeToast(label + ": Reconnection failed");
					}

					public void reconnectingIn(int countdown) {
						makeToast(label + ": Reconnecting in " + countdown);
					}

					public void connectionClosedOnError(Exception arg0) {
						makeToast(label + ": Connection closed on error");
					}

					public void connectionClosed() {
						makeToast(label + ": Connection closed");
					}
				});
			}
		}
	}

	private XMPPConnection connectToServer(ConnectionConfigurationEntity cc)
			throws NumberFormatException {
		ConnectionConfiguration xmpp_conn_config = new ConnectionConfiguration(
				cc.getServer(), Integer.valueOf(cc.getPort()), cc.getDomain());
		xmpp_conn_config.setCompressionEnabled(cc.getCompressed());
		xmpp_conn_config
				.setSASLAuthenticationEnabled(cc.getSaslauthenticated());
		// xmpp_conn_config.setReconnectionAllowed(true); // TODO - turn off if
		// conflict (e.g.same jid) or hopelessly bad settings

		XMPPConnection connection = new XMPPConnection(xmpp_conn_config);
		try {
			connection.connect();
			makeToast(cc.getLabel() + "is connected:"
					+ connection.isConnected());
			connection.login(cc.getUsername(), cc.getPassword(),
					cc.getResource());
			makeToast(cc.getLabel() + "is authenticated:"
					+ connection.isAuthenticated());
		} catch (XMPPException e) {
			connection = null;
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
		return connection;
	}

	private void weakenNetworkOnMainThreadPolicy() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	private List<ConnectionConfigurationEntity> getAllConnectionConfigurations() {
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		List<ConnectionConfigurationEntity> all = daoSession
				.getConnectionConfigurationEntityDao().loadAll();
		DatabaseUtil.close();
		return all;
	}

	// TODO refactor this away to the the DatabaseUtil, this is the same as
	// onListItemClick code in ConnectionListActivity
	private ConnectionConfigurationEntity getConnectionConfiguration(long cc_id) {
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		ConnectionConfigurationEntity cc = daoSession
				.getConnectionConfigurationEntityDao().queryBuilder()
				.where(Properties.Id.eq(cc_id)).list().get(0);
		DatabaseUtil.close();
		return cc;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		if (intent.hasExtra(CRUDConnectionActivity.RESTART_CONNECTION)) {
			long cc_id = intent.getExtras().getLong(
					CRUDConnectionActivity.RESTART_CONNECTION);
			connectToServer(getConnectionConfiguration(cc_id));
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Cancel the persistent notification.
		notificationManager.cancel(NOTIFICATION);
		for (long key : connection_hashmap.keySet()) {
			XMPPConnection conn = connection_hashmap.get(key);
			if (conn != null && conn.isConnected()) {
				conn.disconnect();
			}
		}
		makeToast("onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		makeToast("onUnbind");
		return super.onUnbind(intent);
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Show a notification while this service is running.
	 */
	private void createNotificationAndNotify(CharSequence text) {
		Context context = (Context) this;
		Intent notificationIntent = new Intent(context, XMPPService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				PI_REQUEST_CODE, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Resources res = context.getResources();
		Notification.Builder builder = new Notification.Builder(context);

		builder.setContentIntent(contentIntent)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(
						BitmapFactory.decodeResource(res,
								R.drawable.ic_launcher))
				.setTicker(res.getString(R.string.my_ticker))
				.setWhen(System.currentTimeMillis()).setAutoCancel(true)
				.setContentTitle(res.getString(R.string.my_notification_title))
				.setContentText(text);
		Notification n = builder.getNotification();

		nm.notify(XMPP_CONNECTED, n);
	}

	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		createNotificationAndNotify(getText(R.string.local_service_started));
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 
	 * This is called by BuddyListActivity to get the Roster and Buddy list
	 * 
	 * @param conn_id
	 *            Row index which also maps to Sqlite database row, it serves to
	 *            fetch the connection from the connection HashMap.
	 * @return
	 */
	public Roster getRoster(long conn_id) {
		XMPPConnection connection = connection_hashmap.get(conn_id);
		if (connection == null)
			return null;
		if (connection.isConnected() && connection.isAuthenticated()) {
			return connection.getRoster();
		}
		return null;
	}
}
