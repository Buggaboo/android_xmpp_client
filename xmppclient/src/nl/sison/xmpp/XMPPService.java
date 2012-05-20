package nl.sison.xmpp;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.DaoSession;

import org.jivesoftware.smack.ConnectionConfiguration;
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
	private NotificationManager mNM;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.local_service_started;

	public final static String KEY_CONNECTION_INDEX = "USTHUAS34027334H";

	private ConcurrentHashMap<Long, XMPPConnection> conn_hash_map;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		XMPPService getService() {
			return XMPPService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		makeToast("onCreate");
		makeConnectionsFromDatabase();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
	}

	private void makeConnectionsFromDatabase() {
		List<ConnectionConfigurationEntity> all_conns = getAllConnectionConfigurations();
		conn_hash_map = new ConcurrentHashMap<Long, XMPPConnection>();
		weakenNetworkOnMainThreadPolicy(); // TODO - remove and implement this
											// as asynctask when debugging is
											// done
		for (final ConnectionConfigurationEntity cc : all_conns) {
			// new Runnable() {
			// TODO use AsyncTask to fire up connections
			// public void run() {
			// ConnectionConfiguration xmpp_conn_config = new
			// ConnectionConfiguration(
			// cc.getServer(), Integer.valueOf(cc.getPort()));
			// TODO - change port to integer type
			// xmpp_conn_config.setReconnectionAllowed(true);

			// ConnectionConfiguration xmpp_conn_config = new
			// ConnectionConfiguration(
			// "talk.google.com", 5222, "gmail.com"); // TODO add domain (jid
			// part)
			//
			// xmpp_conn_config.setCompressionEnabled(true);
			// xmpp_conn_config.setSASLAuthenticationEnabled(true);
			// XMPPConnection connection = new XMPPConnection(xmpp_conn_config);
			// conn_hash_map.put(cc.getId(), connection);
			// try {
			// connection.connect();
			// makeToast("isConnected: " + connection.isConnected());
			// // connection.login(cc.getUsername(), cc.getPassword(),
			// cc.getResource());
			// // connection.login("testiculartestee", "crossbodyleadtester",
			// "android");
			// } catch (XMPPException e) {
			// connection = null;
			// e.printStackTrace();
			// Log.e(TAG, e.toString());
			// }

			ConnectionConfiguration xmpp_conn_config = new ConnectionConfiguration(
					cc.getServer(), Integer.valueOf(cc.getPort()),
					cc.getDomain());
			xmpp_conn_config.setCompressionEnabled(cc.getCompressed());
			xmpp_conn_config.setSASLAuthenticationEnabled(cc
					.getSaslauthenticated());
			// xmpp_conn_config.setReconnectionAllowed(true); // TODO
			XMPPConnection connection = new XMPPConnection(xmpp_conn_config);

			try {
				connection.connect();
				makeToast(cc.getLabel() + ":" + connection.isConnected());
				connection.login(cc.getUsername(), cc.getPassword(),
						cc.getResource());
			} catch (XMPPException e) {
				connection = null;
				e.printStackTrace();
				Log.e(TAG, e.toString());
			}

			conn_hash_map.put(cc.getId(), connection);

		}
		// };
		// }
		// testConnection(all); // TODO remove test
	}

	private void weakenNetworkOnMainThreadPolicy() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	private void testConnection(List<ConnectionConfigurationEntity> all) {
		// TODO remove test
		for (final ConnectionConfigurationEntity cc : all) {
			XMPPConnection conn = conn_hash_map.get(cc.getId());
			if (conn != null)

				makeToast(cc.getLabel()
						+ "is connected and authenticated: "
						+ String.valueOf(conn.isAuthenticated()
								&& conn.isConnected()));
			else
				makeToast("Connection failed.");
		}
	}

	private List<ConnectionConfigurationEntity> getAllConnectionConfigurations() {
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		List<ConnectionConfigurationEntity> all = daoSession
				.getConnectionConfigurationEntityDao().loadAll();
		DatabaseUtil.close();
		return all;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		for (long key : conn_hash_map.keySet()) {
			XMPPConnection conn = conn_hash_map.get(key);
			conn.disconnect();
		}
		makeToast("onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Show a notification while this service is running.
	 */
	private void createNotificationAndNotify(CharSequence text) {
		Context ctx = (Context) this;
		Intent notificationIntent = new Intent(ctx, XMPPService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(ctx,
				PI_REQUEST_CODE, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager nm = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Resources res = ctx.getResources();
		Notification.Builder builder = new Notification.Builder(ctx);

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
}
