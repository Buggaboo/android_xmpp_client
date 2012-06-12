package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.BuddyEntityDao;
import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntity;
import nl.sison.xmpp.dao.MessageEntityDao.Properties;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
public class ChatActivity extends Activity {
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

	private final static String TAG = "ChatActivity";

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (top_orientation) {
			setContentView(R.layout.chat_top_oriented_layout);
			chat_list = (ListView) findViewById(R.id.chat_top_input);
			submit = (Button) findViewById(R.id.submit_top_input);
			input = (EditText) findViewById(R.id.text_input_top_input);

		} else {
			setContentView(R.layout.chat_bottom_oriented_layout);
			chat_list = (ListView) findViewById(R.id.chat_bottom_input);
			submit = (Button) findViewById(R.id.submit_bottom_input);
			input = (EditText) findViewById(R.id.text_input_bottom_input);
		}

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
		registerReceiver(receiver, actionFilter);

		// in case it was on yet
		startService(new Intent(ChatActivity.this, XMPPService.class));
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();

		if (bundle.containsKey(BuddyListActivity.KEY_BUDDY_INDEX)) {
			buddy_id = bundle.getLong(BuddyListActivity.KEY_BUDDY_INDEX);
			own_jid = bundle.getString(BuddyListActivity.JID);
		} else if (bundle.containsKey(XMPPNotificationService.KEY_BUDDY_INDEX)) {
			buddy_id = bundle.getLong(XMPPNotificationService.KEY_BUDDY_INDEX);
			own_jid = bundle.getString(XMPPNotificationService.JID);
		}

		// strategy to cleanup notifications
		preventNotificationOfActiveBuddy();
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
					sendBroadcast(messageIntent);
				}
			}
		});

		submit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {

			}
		});

	}

	private void preventNotificationOfActiveBuddy() {
		BuddyEntityDao dao = DatabaseUtils.getWriteableDatabaseSession(this)
				.getBuddyEntityDao();
		List<BuddyEntity> deactivated_buddies = dao.loadAll();
		for (BuddyEntity buddy : deactivated_buddies) {
			buddy.setIsActive(false);
			dao.insertOrReplace(buddy);
		}
		// dao.insertInTx(deactivated_buddies); // TODO - determine why this
		// triggers a SQLiteConstraintException
		BuddyEntity active_buddy = dao.load(buddy_id);
		active_buddy.setIsActive(true);
		dao.insertOrReplace(active_buddy);
		DatabaseUtils.close();
	}

	private void broadcastRequestRemoveNotifications() {
		Intent request_remove_notifications = new Intent(
				ChatActivity.ACTION_REQUEST_REMOVE_NOTIFICATIONS);
		request_remove_notifications.putExtra(KEY_BUDDY_INDEX, buddy_id);
		sendBroadcast(request_remove_notifications);
	}

	private void setupListView() { // TODO broken! fix it!
		DaoSession daoSession = DatabaseUtils.getReadOnlyDatabaseSession(this);

		QueryBuilder<MessageEntity> qb = daoSession.getMessageEntityDao()
				.queryBuilder();

		qb.where(Properties.BuddyId.eq(buddy_id));
		// NOTE: this presents a challenge for group chat
		// You probably need a groupchat activity for this thing
		// and a different database

		chat_history = qb.list();
		DatabaseUtils.close();

		adapter = new MessageAdapter(this, chat_history, own_jid);

		chat_list.setAdapter(adapter);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		releaseBuddyForNotification();
	}

	private void releaseBuddyForNotification() {
		DaoSession dao = DatabaseUtils.getWriteableDatabaseSession(this);
		BuddyEntity b = dao.load(BuddyEntity.class, buddy_id);
		b.setIsActive(false);
		dao.insertOrReplace(b);
		DatabaseUtils.close();
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
