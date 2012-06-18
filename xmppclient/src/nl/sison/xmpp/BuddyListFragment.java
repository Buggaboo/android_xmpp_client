package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.BuddyEntityDao;
import nl.sison.xmpp.dao.BuddyEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

// TODO implement titlebar http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android
/**
 * 
 * @author Jasm Sison
 * 
 */
public class BuddyListFragment extends ListFragment {
	private static final String TAG = "BuddyListFragment";

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
					BuddyEntity be = DatabaseUtils.getReadOnlySession(context)
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
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			long buddy_id = getListAdapter().getItemId(info.position);
			DaoSession rdao = DatabaseUtils.getReadOnlySession(getActivity());
			final BuddyEntity buddy = rdao.load(BuddyEntity.class, buddy_id);
			DatabaseUtils.close();

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(buddy.getPartial_jid());
			String[] buddy_setup_action = new String[2];

			final int NICKNAME_OPTION = 0;
			final int VIBRATE_OPTION = 1;
			final int SET_MASTER_PASSWORD_OPTION = 2;
			final int ENCRYPT_MESSAGES_BY_BUDDY = 3;
			// TODO use the master password to encrypt the messages by a
			// buddy, but store the password in a salted hashed form on the
			// device

			Activity act = getActivity();
			
			buddy_setup_action[NICKNAME_OPTION] = act.getString(
					R.string.change_nickname);
			
			// TODO - fix: I suspect buddy.getVibrate is causing a nullptr
			if (buddy.getVibrate()) {
				buddy_setup_action[VIBRATE_OPTION] = act.getString(
						R.string.vibrate)
						+ " " + act.getString(R.string.off);
			} else {
				buddy_setup_action[VIBRATE_OPTION] = act.getString(
						R.string.vibrate)
						+ " " + act.getString(R.string.on);
			}

			builder.setItems(buddy_setup_action,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							Activity act = getActivity();
							if (i == VIBRATE_OPTION) {
								BuddyEntityDao wdao = DatabaseUtils
										.getWriteableSession(act)
										.getBuddyEntityDao();
								BuddyEntity _buddy = wdao.load(buddy.getId());
								_buddy.setVibrate(!buddy.getVibrate());
								wdao.insertOrReplace(_buddy);
								DatabaseUtils.close();
								return;
							} else if (i == NICKNAME_OPTION) {
								AlertDialog.Builder alert = new AlertDialog.Builder(
										act);

								alert.setTitle(act.getString(
										R.string.change_nickname));
								alert.setMessage(buddy.getPartial_jid());

								// Set an EditText view to get user input
								final EditText input = new EditText(
										act);
								if (buddy.getNickname() != null
										&& !buddy.getNickname().isEmpty()) {
									input.setText(buddy.getNickname());
									input.setSelectAllOnFocus(true);
								} else {
									input.setText("");
									input.setFocusable(true);
								}
								alert.setView(input);

								alert.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int button_id) {
												BuddyEntityDao wdao = DatabaseUtils
														.getWriteableSession(
																getActivity())
														.getBuddyEntityDao();
												BuddyEntity _buddy = wdao
														.load(buddy.getId());
												_buddy.setNickname(input
														.getText().toString());
												wdao.insertOrReplace(_buddy);
												DatabaseUtils.close();

												// refresh the list (e.g.
												// reflect the change in
												// nickname)
												refreshList(buddy
														.getConnectionId());
											}
										});

								alert.setNegativeButton(
										android.R.string.cancel,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int button_id) {
												dialog.dismiss();
											}
										});
								alert.show();
							}
						}
					});
			builder.create().show();
		} catch (ClassCastException e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		setEmptyText(getActivity().getString(R.string.no_buddy_item));
		getActivity().registerReceiver(receiver, actionFilter);
		refreshList(getArguments().getLong(
				ConnectionListFragment.KEY_CONNECTION_INDEX, 0));
	}

	private void refreshList(long cc_id) {
		// TODO finish context menu for the dialog
		// TODO consider deleting the buddies, instead of throwing away the
		// whole adapter, if this turns out to be a memory hog
		adapter = null;
		registerForContextMenu(getListView());
		DaoSession daoSession = DatabaseUtils.getReadOnlySession(getActivity()
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
