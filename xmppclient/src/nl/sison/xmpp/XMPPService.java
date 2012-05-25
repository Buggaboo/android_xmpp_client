package nl.sison.xmpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.BuddyEntityDao;
import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntity;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import de.greenrobot.dao.QueryBuilder;

public class XMPPService extends Service {
	/**
	 * ISSUES
	 */
	/**
	 * - roster subscription request, packet listener, filter presence type
	 * Presence.Type.subscribe -> popup dialog (yes, later, never) here:
	 * http://www.igniterealtime.org/builds/smack/docs/
	 * latest/documentation/roster.html
	 */
	/**
	 * TODO - intercept packet to enable connection to providers
	 */

	/**
	 * ROADMAP
	 */
	/**
	 * TODO - location aware advertising - voice messages?
	 */
	/**
	 * TODO - qr code triggered offers (in a shop?) buy shit together etc.
	 */
	/**
	 * TODO - usage tracker (gps?)
	 */

	private static final String TAG = "XMPPService";

	private ConcurrentHashMap<Long, XMPPConnection> connection_hashmap;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			makeToast("Intent received:" + intent.getAction());
			// TODO - status = away unavailable etc.
			// TODO - start chat / send message
		}
	};

	// // Unique Identification Number for the Notification.
	// // We use it on Notification start, and to cancel it.
	public static final String KEY_CONNECTION_INDEX = "USTHUAS34027334H";
	public static final String KEY_BUDDY_INDEX = "UST32323HUAS34027334H";
	public static final String ACTION_BUDDY_PRESENCE_UPDATE = "2<>p>>34UEOEOUOUO";
	public static final String ACTION_BUDDY_NEW_MESSAGE = "89776868tthfHGTHM";
	public static final String ACTION_CONNECTION_LOST = "fg&*thou<oo";
	public static final String JID = "239eunheun34808";
	public static final String MANY_JID = "239443342eunheun34808";
	public static final String MESSAGE = "239e#$%unheun34808";
	public static final String FROM_JID = "23heun348$%$#&08";
	public static final int PENDING_INTENT_REQUEST_CODE = 1244324;

	@Override
	public void onCreate() {
		super.onCreate();
		makeConnectionsFromDatabase();
	}

	private void makeConnectionsFromDatabase() {
		List<ConnectionConfigurationEntity> all_conns = getAllConnectionConfigurations();
		connection_hashmap = new ConcurrentHashMap<Long, XMPPConnection>();
		weakenNetworkOnMainThreadPolicy(); // TODO - remove and implement this
											// as asynctask or runnable when
											// debugging is
											// done, makeToast must run on main
											// thread or is invisible
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
				setListeners(connection, cc.getId(), label);
				connection.getRoster().setSubscriptionMode(
						SubscriptionMode.accept_all);
				populateBuddyLists(connection, label);
			}
		}
	}

	private void populateBuddyLists(XMPPConnection connection, String label) {
		DaoSession daoSession = DatabaseUtil.getWriteableDatabaseSession(this);
		QueryBuilder<BuddyEntity> qb = daoSession.getBuddyEntityDao()
				.queryBuilder();
		Roster roster = connection.getRoster();
		for (RosterEntry re : roster.getEntries()) {
			List<BuddyEntity> query_result = qb.where(
					BuddyEntityDao.Properties.Partial_jid.eq(re.getUser()))
					.list();
			BuddyEntity b;
			if (query_result.isEmpty()) {
				b = new BuddyEntity();
			} else {
				b = query_result.get(0);
			}

			String partial_jid = re.getUser();
			makeToast("re.getUser(): " + partial_jid);
			Presence p = roster.getPresence(partial_jid); // TODO - experiment
															// with
															// partial_jid

			setBuddyEntryBasic(b, partial_jid);
			if (p != null) {
				setBuddyEntryPresence(b, p);
			}

			daoSession.insertOrReplace(b);
		}
		DatabaseUtil.close();
	}

	private void setBuddyEntryPresence(BuddyEntity b, Presence p) {
		b.setIsAvailable(p.isAvailable());
		makeToast("p.isAvailable(): " + p.isAvailable());

		b.setIsAway(p.isAway());
		makeToast("p.isAway(): " + p.isAway());

		b.setPresence_status(p.getStatus());
		makeToast("p.getStatus(): " + p.getStatus());
		b.setPresence_type(p.getType().toString());
		// b.setPresence_mode(p.getMode().toString());
	}

	private void setBuddyEntryBasic(BuddyEntity b, String partial_jid) {
		b.setPartial_jid(partial_jid);
		if (b.getNickname() == null || b.getNickname().isEmpty()) {
			b.setNickname(partial_jid);
		}
	}

	private void setListeners(XMPPConnection connection, final long cc_id,
			final String label) {
		setConnectionListeners(connection, cc_id, label);
		setRosterListeners(connection, label);
		setIncomingMessageListener(connection, label);
		setOutgoingMessageListener(connection, label);
	}

	private void setOutgoingMessageListener(XMPPConnection connection,
			final String label) {
		connection.addPacketInterceptor(new PacketInterceptor() {
			public void interceptPacket(Packet p) {
				storeMessage((Message) p);
			}
		}, new PacketFilter() {
			public boolean accept(Packet p) {
				return p instanceof Message;
			}
		});
	}

	/**
	 * TODO - figure out what to do with this, general listener
	 * 
	 * @param label
	 * @param connection
	 */
	private void setIncomingMessageListener(XMPPConnection connection,
			final String label) {
		connection.addPacketListener(new PacketListener() {
			public void processPacket(Packet p) {
				Message m = (Message) p;
				broadcastMessage(m);
				storeMessage(m);
			}
		}, new PacketFilter() {
			public boolean accept(Packet p) {
				return p instanceof Message;
			}
		});
	}

	private void broadcastMessage(Message m) {
		Intent intent = new Intent(ACTION_BUDDY_NEW_MESSAGE);
		intent.putExtra(FROM_JID, m.getFrom());
		intent.putExtra(MESSAGE, m.getBody());
		sendBroadcast(intent);
	}

	private void storeMessage(Message m) {
		DaoSession daoSession = DatabaseUtil.getWriteableDatabaseSession(this);
		MessageEntity message = new MessageEntity();
		message.setContent(m.getBody());
		message.setReceived_date(new Date());
		message.setSender_jid(StringUtils.parseBareAddress(m.getFrom()));
		message.setReceiver_jid(StringUtils.parseBareAddress(m.getTo()));
		daoSession.getMessageEntityDao().insert(message);
		DatabaseUtil.close();
	}

	private void broadcastPresenceUpdate(Presence p) {
		String from = p.getFrom();
		Intent intent = new Intent(ACTION_BUDDY_PRESENCE_UPDATE);
		intent.putExtra(JID, p.getFrom());

		DaoSession daoSession = DatabaseUtil.getWriteableDatabaseSession(this);
		QueryBuilder<BuddyEntity> qb = daoSession.getBuddyEntityDao()
				.queryBuilder();
		List<BuddyEntity> query_result = qb.where(
				BuddyEntityDao.Properties.Partial_jid.eq(StringUtils
						.parseBareAddress(p.getFrom()))).list();

		// create entity if it doesn't exist yet
		// otherwise the broadcast will be pointless
		BuddyEntity b;
		if (query_result.isEmpty()) {
			b = new BuddyEntity();
			setBuddyEntryBasic(b, StringUtils.parseBareAddress(from));
			setBuddyEntryPresence(b, p);
		} else {
			b = query_result.get(0);
		}
		b.setLast_seen_resource(StringUtils.parseResource(from));

		if (p.isAvailable()) {
			b.setLast_seen_online_date(new Date());
		}

		intent.putExtra(KEY_BUDDY_INDEX, daoSession.insertOrReplace(b));

		sendBroadcast(intent);
	}

	private void broadcastRosterUpdate(Collection<String> usernames) {
		Intent intent = new Intent(ACTION_BUDDY_PRESENCE_UPDATE);
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.addAll(usernames);
		intent.putExtra(MANY_JID, arrayList);
		sendBroadcast(intent);
	}

	private void setRosterListeners(XMPPConnection connection,
			final String label) {
		Roster roster = connection.getRoster();
		roster.addRosterListener(new RosterListener() {

			public void presenceChanged(Presence p) {
				makeToast(label + ": presence changed:" + p.getFrom()); // TODO
																		// -
																		// determine
																		// if
																		// getFrom
																		// returns
																		// partial
																		// jid

				broadcastPresenceUpdate(p);
			}

			public void entriesUpdated(Collection<String> usernames) {
				for (String username : usernames)
					// TODO remove
					makeToast(label + ": buddy list item updated: " + username);
				broadcastRosterUpdate(usernames);
			}

			public void entriesDeleted(Collection<String> usernames) {
				for (String username : usernames)
					// TODO remove
					makeToast(label + ": buddy list item deleted: " + username);
				broadcastRosterUpdate(usernames);
			}

			public void entriesAdded(Collection<String> usernames) {
				for (String username : usernames)
					// TODO remove
					makeToast(label + ": buddy list items added: " + username);
				broadcastRosterUpdate(usernames);
			}
		});
	}

	private void broadcastConnectionUpdate(final long cc_id) {
		Intent intent = new Intent(ACTION_CONNECTION_LOST);
		intent.putExtra(KEY_CONNECTION_INDEX, cc_id);
		sendBroadcast(intent);
	}

	private void setConnectionListeners(XMPPConnection connection,
			final long cc_id, final String label) {
		connection.addConnectionListener(new ConnectionListener() {

			public void reconnectionSuccessful() {
				makeToast(label + ": Reconnection successful");
				broadcastConnectionUpdate(cc_id);
			}

			public void reconnectionFailed(Exception ex) {
				makeToast(label + ": Reconnection failed");
			}

			public void reconnectingIn(int countdown) {
				makeToast(label + ": Reconnecting in " + countdown);
			}

			public void connectionClosedOnError(Exception ex) {
				makeToast(label + ": Connection closed on error");
				broadcastConnectionUpdate(cc_id);
			}

			public void connectionClosed() {
				makeToast(label + ": Connection closed");
				broadcastConnectionUpdate(cc_id);
			}
		});
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
			cc.setConnection_success(cc.getConnection_success() + 1);
		} catch (XMPPException e) {
			connection = null;
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
		return connection;
	}

	private void weakenNetworkOnMainThreadPolicy() { // TODO recode with
														// AsyncTask, maybe even
														// all listeners
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
				.getConnectionConfigurationEntityDao().load(cc_id);
		// TODO there's a lag before the database is written to, so the table
		// appears to be empty right before it's read
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
		for (long key : connection_hashmap.keySet()) {
			XMPPConnection conn = connection_hashmap.get(key);
			if (conn != null && conn.isConnected()) {
				conn.disconnect();
			}
		}
		makeToast("onDestroy");
	}

	/**
	 * Show a notification while this service is running. TODO - use this to
	 * notify if anyone is chatting with you, last message stuff
	 */
	/**
	 * TODO refactor notification to some other service with a broadcast
	 * receiver
	 */

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
