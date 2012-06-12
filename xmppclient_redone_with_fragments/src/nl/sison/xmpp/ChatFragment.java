package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntity;
import nl.sison.xmpp.dao.MessageEntityDao.Properties;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import de.greenrobot.dao.QueryBuilder;

// TODO implement away message
// TODO implement titlebar http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android
// TODO implement group chat
/**
 * 
 * @author Jasm Sison
 * 
 */
public class ChatFragment extends Fragment {
	/**
	 * Intent action
	 */
	public static final String ACTION_REQUEST_DELIVER_MESSAGE = "23yididxb3@#{}$%";
	public static final String ACTION_REQUEST_REMOVE_NOTIFICATIONS = "23yid#@idxb3@#$%";

	/**
	 * Intent extras
	 */
	public static final String MESSAGE = "23yidxb3@#$%444";
	public static final String KEY_BUDDY_INDEX = "23yidb3@#$Z44";

	private boolean top_orientation = false; // TODO create dialog
	private ListView chat_list;
	private Button submit;
	private EditText input;

	private String own_jid;

	// private ArrayList<String> group_chat_jids; // TODO
	// private String group_chat_thread; // TODO

	private BroadcastReceiver receiver;

	private MessageAdapter adapter;

	private long buddy_id;

	private List<MessageEntity> chat_history;

	private final static String TAG = "ChatFragment";

	class MessageBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			long message_id = -1;

			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_ERROR)) {
				makeToast("An error occurred when attempting to deliver this message bla");
				return;
			}

			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_SENT)) {
				message_id = intent.getExtras().getLong(
						XMPPService.KEY_MESSAGE_INDEX);
				input.setText("");
				input.setFocusable(true);
			}

			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_INCOMING)) {
				Bundle bundle = intent.getExtras();
				message_id = bundle.getLong(XMPPService.KEY_MESSAGE_INDEX);
			}

			DaoSession daoSession = DatabaseUtils
					.getReadOnlyDatabaseSession(context);
			MessageEntity message = daoSession.load(MessageEntity.class,
					message_id);

			// this prevents messages from other buddies to leak into this
			// context
			if (message == null || message.getBuddyId() != buddy_id) {
				DatabaseUtils.close();
				return;
			}

			DatabaseUtils.close();
			broadcastRequestRemoveNotifications();
			adapter.add(message);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View parent_view;

		if (top_orientation) {
			parent_view = LayoutInflater.from(getActivity()).inflate(R.layout.chat_bottom_oriented_layout, null, false);
			chat_list = (ListView) parent_view.findViewById(R.id.chat_top_input);
			submit = (Button) parent_view.findViewById(R.id.submit_top_input);
			input = (EditText) parent_view.findViewById(R.id.text_input_top_input);

		} else {
			parent_view = LayoutInflater.from(getActivity()).inflate(R.layout.chat_top_oriented_layout, null, false);
			chat_list = (ListView) parent_view.findViewById(R.id.chat_bottom_input);
			submit = (Button) parent_view.findViewById(R.id.submit_bottom_input);
			input = (EditText) parent_view.findViewById(R.id.text_input_bottom_input);
		}
		return parent_view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		receiver = new MessageBroadcastReceiver();
		IntentFilter actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_SENT); // DONE!
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_ERROR); // DONE!
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_INCOMING); // DONE!

		// TODO place the following in their own receiver
		// TODO unregister the receivers onDestroy
		// actionFilter.addAction(XMPPService.ACTION_BUDDY_PRESENCE_UPDATE);
		// actionFilter.addAction(XMPPService.ACTION_CONNECTION_LOST);
		// actionFilter.addAction(XMPPService.ACTION_CONNECTION_RESUMED);
		getActivity().registerReceiver(receiver, actionFilter);

		// in case it was on yet
		getActivity()
				.startService(new Intent(getActivity(), XMPPService.class));
	}

	@Override
	public void onResume() {
		super.onResume();

		Bundle bundle = getArguments();

		if (bundle.containsKey(BuddyListFragment.KEY_BUDDY_INDEX)) {
			buddy_id = bundle.getLong(BuddyListFragment.KEY_BUDDY_INDEX);
			own_jid = bundle.getString(BuddyListFragment.JID);
		} else if (bundle.containsKey(XMPPNotificationService.KEY_BUDDY_INDEX)) {
			buddy_id = bundle.getLong(XMPPNotificationService.KEY_BUDDY_INDEX);
			own_jid = bundle.getString(XMPPNotificationService.JID);
		}

		broadcastRequestRemoveNotifications();

		setupListView();

		submit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent messageIntent = new Intent(
						ACTION_REQUEST_DELIVER_MESSAGE);
				String message = input.getText().toString().trim();
				messageIntent.putExtra(MESSAGE, message);
				messageIntent.putExtra(KEY_BUDDY_INDEX, buddy_id);
				if (message.length() != 0) {
					getActivity().sendBroadcast(messageIntent);
				}
			}
		});

		submit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO - do something with this event...
			}
		});

	}

	private void broadcastRequestRemoveNotifications() {
		Intent request_remove_notifications = new Intent(
				ChatFragment.ACTION_REQUEST_REMOVE_NOTIFICATIONS);
		request_remove_notifications.putExtra(KEY_BUDDY_INDEX, buddy_id);
		getActivity().sendBroadcast(request_remove_notifications);
	}

	private void setupListView() { // TODO broken! fix it!
		DaoSession daoSession = DatabaseUtils
				.getReadOnlyDatabaseSession(getActivity()
						.getApplicationContext());

		QueryBuilder<MessageEntity> qb = daoSession.getMessageEntityDao()
				.queryBuilder();

		qb.where(Properties.BuddyId.eq(buddy_id)); //  match with foreign key (buddy)
		// NOTE: this presents a challenge for group chat
		// You probably need a groupchat activity for this thing
		// and a different database

		chat_history = qb.list();
		DatabaseUtils.close();

		adapter = new MessageAdapter(getActivity(), chat_history, own_jid);
		
		chat_list.setAdapter(adapter);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		getActivity().unregisterReceiver(receiver);
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast
				.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
