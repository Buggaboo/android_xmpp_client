package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.BuddyEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

// TODO implement titlebar http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android
/**
 * 
 * @author Jasm Sison
 * 
 */
public class BuddyListFragment extends ListFragment {
	private static final String TAG = "BuddyListFragment";

	/**
	 * TODO - remove useless request code
	 */
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

	private IntentFilter actionFilter;

	public class BuddyListReceiver extends BroadcastReceiver {
		/**
		 * TODO Refactor to different receivers, - one for connections, - one
		 * for buddies, - one for chats
		 */

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_INCOMING)) {
				if (intent.hasExtra(XMPPService.KEY_MESSAGE_INDEX)) {
					// TODO message from certain jid, give visual feedback
					// (flashing listitem or something), load the activity again
					// with the correct connection
				}
			}
			// the logic, one buddy's presence status changes, let's refresh
			// them all
			if (intent.getAction().equals(
					XMPPService.ACTION_BUDDY_PRESENCE_UPDATE)) {
				if (intent.hasExtra(XMPPService.KEY_BUDDY_INDEX)) {
					BuddyEntity be = DatabaseUtils.getReadOnlyDatabaseSession(
							context)
							.load(BuddyEntity.class,
									intent.getLongExtra(
											XMPPService.KEY_BUDDY_INDEX, 0));
					DatabaseUtils.close();
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

				Intent fragmentIntent = new Intent(getActivity(),
						ChatFragment.class);

				// pass on the intent, using different keys
				fragmentIntent.putExtra(KEY_BUDDY_INDEX,
						bundle.getLong(XMPPService.KEY_BUDDY_INDEX));
				fragmentIntent.putExtra(JID, bundle.getString(XMPPService.JID));

				// startActivity(startActivityIntent); // TODO + replace
				// startActivity with calls for a new Fragment
				((FragmentLoader) getActivity()).loadFragment(fragmentIntent);
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_INCOMING);
		actionFilter.addAction(XMPPService.ACTION_BUDDY_PRESENCE_UPDATE);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_LOST);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_RESUMED);
		actionFilter.addAction(XMPPService.ACTION_REQUEST_CHAT_GRANTED);
		actionFilter.addAction(XMPPService.ACTION_REQUEST_CHAT_ERROR);
		receiver = new BuddyListReceiver();
		getActivity().registerReceiver(receiver, actionFilter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long buddy_id) {
		Intent intent = new Intent(ACTION_REQUEST_CHAT);
		intent.putExtra(KEY_BUDDY_INDEX, buddy_id);
		getActivity().sendBroadcast(intent); // TODO register broadcast receiver
												// on activity
	}

	/**
	 * TODO - implement context Menu set presence, using dialogs
	 */

	/**
	 * TODO - implement subscribe, unsubscribe buddylist, using dialogs
	 */
	/**
	 * TODO - implement set nickname
	 */
/*
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			long buddy_id = getListAdapter().getItemId(info.position);
			DaoSession rdao = DatabaseUtils.getReadOnlyDatabaseSession(
					getActivity());
			final BuddyEntity buddy = rdao.load(BuddyEntity.class, buddy_id);
			DatabaseUtils.close();

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(buddy.getPartial_jid());
			String[] buddy_setup_action = new String[2];// { "Set Nickname",
														// "Vibrate" };
			if (buddy.getVibrate()) {
				buddy_setup_action[1] = getActivity().getString(
						R.string.vibrate)
						+ " " + getActivity().getString(R.string.on);
			} else {
				buddy_setup_action[1] = getActivity().getString(
						R.string.vibrate)
						+ " " + getActivity().getString(R.string.off);
			}
			builder.setItems(buddy_setup_action,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							if (i == 1)
							{
								buddy.setVibrate(!buddy.getVibrate());
							}
							DaoSession wdao = DatabaseUtils.getWriteableDatabaseSession(getActivity());
							wdao.insertOrReplace(buddy);
						}
					});
			AlertDialog d = builder.create();
			d.show();
		} catch (ClassCastException e) {
			e.printStackTrace();
			return;
		}
	}
*/
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		setEmptyText(getActivity().getString(R.string.no_buddy_item)); // causes
																		// crash
																		// if I
																		// put
																		// this
																		// in
																		// onCreate
		getActivity().registerReceiver(receiver, actionFilter);
		refreshList(getArguments().getLong(
				ConnectionListFragment.KEY_CONNECTION_INDEX, 0));
	}

	/**
	 * TODO reimplement using notifyDataSetChanged
	 * http://stackoverflow.com/questions
	 * /3669325/notifydatasetchanged-example/5092426#5092426
	 */
	private void refreshList(long cc_id) {
		// TODO finish context menu for the dialog
		// TODO consider deleting the buddies, instead of throwing away the
		// whole adapter, if this turns out to be a memory hog
		adapter = null;
		registerForContextMenu(getListView());
		DaoSession daoSession = DatabaseUtils
				.getReadOnlyDatabaseSession(getActivity()
						.getApplicationContext());
		List<BuddyEntity> buddies = daoSession.getBuddyEntityDao()
				.queryBuilder().where(Properties.ConnectionId.eq(cc_id)).list();

		// This is necessary, in case a new account is made, and there are no
		// buddies to connect to.
		if (buddies == null || buddies.size() == 0) {
			DatabaseUtils.close();
			return;
		}

		adapter = new BuddyAdapter(getActivity(), buddies);
		DatabaseUtils.close();
		setListAdapter(adapter);
	}

	@Deprecated
	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(getActivity().getApplicationContext(),
				message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
