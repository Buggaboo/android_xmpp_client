package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.BuddyEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

// TODO implement titlebar http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android
/**
 * 
 * @author Jasm Sison
 * 
 */
public class BuddyListActivity extends ListActivity {
	private static final String TAG = "BuddyListActivity";

	/**
	 * Intent request code
	 */
	public static final int RC_CREATE_NEW_THREAD_FROM_JID = 1111;
	public static final int RC_CONTINUE_OLD_THREAD = 1112;

	/**
	 * Intent actions
	 */
	public static final String ACTION_REQUEST_CHAT = "Oent.p8p39392"; // TODO

	/**
	 * Intent extras
	 */
	public static final String KEY_BUDDY_INDEX = "98238.ce";
	public static final String THREAD = "982ruucece";
	public static final String JID = "9xx8238e";

	private BuddyAdapter adapter;
	private BroadcastReceiver receiver;

	// ConcurrentHashMap<long, String> // TODO prevent extra database buddy
	// queries to get jid from id

	public class BuddyListReceiver extends BroadcastReceiver {
		/**
		 * TODO Refactor to different receivers, - one for connections, - one
		 * for buddies, - one for chats
		 */

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_INCOMING)) {
				if (intent.hasExtra(XMPPService.FROM_JID)) {
					// TODO message from certain jid, give visual feedback
					// (flashing listitem or something), load the activity again
					// with the correct connection
				}
			}
			if (intent.getAction().equals(
					XMPPService.ACTION_BUDDY_PRESENCE_UPDATE)) {
				if (intent.hasExtra(XMPPService.KEY_BUDDY_INDEX)) {
					// makeToast("buddy row index: "
					// + intent.getLongExtra(XMPPService.KEY_BUDDY_INDEX,
					// 0));
					// TODO determine to do with this specific buddy id
					BuddyEntity be = DatabaseUtil.getReadOnlyDatabaseSession(
							context)
							.load(BuddyEntity.class,
									intent.getLongExtra(
											XMPPService.KEY_BUDDY_INDEX, 0));
					DatabaseUtil.close();
					refreshList(be.getConnectionId());
				}

			}
			if (intent.getAction().equals(XMPPService.ACTION_CONNECTION_LOST)) {
				// TODO change the buddy presences to offline
			}
			if (intent.getAction()
					.equals(XMPPService.ACTION_CONNECTION_RESUMED)) {
				// TODO make all buddies that are available online, but
				// ACTION_BUDDY_PRESENCE_UPDATE should already do that (test!)
			}
			if (intent.getAction().equals(
					XMPPService.ACTION_REQUEST_CHAT_GRANTED)) {
				Bundle bundle = intent.getExtras();
				Intent startActivityIntent = new Intent(BuddyListActivity.this,
						ChatActivity.class);
				startActivityIntent.putExtra(KEY_BUDDY_INDEX,
						bundle.getLong(XMPPService.KEY_BUDDY_INDEX));
				startActivityIntent.putExtra(THREAD,
						bundle.getString(XMPPService.THREAD));
				startActivityIntent.putExtra(JID,
						bundle.getString(XMPPService.JID));
				startActivity(startActivityIntent);
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getIntent().getExtras().getLong(
				ConnectionListActivity.KEY_CONNECTION_INDEX);

		IntentFilter actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_INCOMING);
		actionFilter.addAction(XMPPService.ACTION_BUDDY_PRESENCE_UPDATE);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_LOST);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_RESUMED);
		actionFilter.addAction(XMPPService.ACTION_REQUEST_CHAT_GRANTED);
		actionFilter.addAction(XMPPService.ACTION_REQUEST_CHAT_ERROR);
		receiver = new BuddyListReceiver();
		registerReceiver(receiver, actionFilter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position,
			long buddy_id) {
		// Intent intent = new Intent(BuddyListActivity.this,
		// ChatActivity.class);

		Intent intent = new Intent(ACTION_REQUEST_CHAT);
		intent.putExtra(KEY_BUDDY_INDEX, buddy_id);
		sendBroadcast(intent);

		// TODO
		// @ match jid -> boeit ook niet
		// @ get chronological last chat thread, -> dat boeit niet...
		// - confirm chat recent -> I don't recall wtf I mean by this...
	}

	/**
	 * TODO - implement context Menu set presence, using dialogs
	 */

	/**
	 * TODO - implement subscribe, unsubscribe buddylist, using dialogs
	 */

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO + filter on cc_id, all buddies have a foreign key to cc
		refreshList(getIntent().getLongExtra(
				ConnectionListActivity.KEY_CONNECTION_INDEX, 0));
	}

	/**
	 * TODO reimplement using notifyDataSetChanged
	 * http://stackoverflow.com/questions
	 * /3669325/notifydatasetchanged-example/5092426#5092426
	 */
	private void refreshList(long cc_id) {
		// TODO finish context menu for the dialog
		// TODO consider deleting the buddies, instead of throwing away the
		// whole adapter
		adapter = null;
		registerForContextMenu(getListView());
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		// List<BuddyEntity> buddies = daoSession.getBuddyEntityDao().loadAll();
		List<BuddyEntity> buddies = daoSession.getBuddyEntityDao()
				.queryBuilder().where(Properties.ConnectionId.eq(cc_id)).list();

		// This is necessary, in case a new account is made, and there are no
		// buddies to connect to.
		if (buddies == null || buddies.size() == 0) {
			DatabaseUtil.close();
			return;
		}

		// testing purposes // TODO remove
		for (BuddyEntity be : buddies) {
			Log.i(TAG, "id: " + be.getId());
			Log.i(TAG, "isAway: " + be.getIsAway());
			Log.i(TAG, "isAvailable: " + be.getIsAvailable());
		}

		adapter = new BuddyAdapter(this, buddies);
		DatabaseUtil.close();
		setListAdapter(adapter);
	}

	@Deprecated
	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
