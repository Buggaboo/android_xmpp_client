package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.BuddyEntity;
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

public class ChatActivity extends Activity {
	/**
	 * Intent action
	 */
	public static final String ACTION_REQUEST_DELIVER_MESSAGE = "23yididxb3@#$%";

	/**
	 * Intent extras
	 */
	public static final String THREAD = "23yididxb3@#$%44";
	public static final String MESSAGE = "23yidxb3@#$%444";
	public static final String KEY_BUDDY_INDEX = "23yidb3@#$Z44";

	private boolean top_orientation = false; // TODO create dialog
	private ListView chat_list;
	private Button submit;
	private EditText input;

	private String own_jid, other_jid;
	private String thread;
	// private ArrayList<String> group_chat_jids; // TODO
	// private String group_chat_thread; // TODO

	private BroadcastReceiver receiver;

	private MessageAdapter adapter;

	private long buddy_id;

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

			DaoSession daoSession = DatabaseUtil
					.getReadOnlyDatabaseSession(context);
			MessageEntity message = daoSession.load(MessageEntity.class,
					message_id);

			if (message.getBuddyId() != buddy_id) {
				DatabaseUtil.close();
				return;
			}

			DatabaseUtil.close();

			makeToast("adapter adding message - start");
			adapter.add(message);
			makeToast("adapter adding message - end");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (top_orientation) {
			setContentView(R.layout.working_frame_layout_top);
			chat_list = (ListView) findViewById(R.id.chat_top_input);
			submit = (Button) findViewById(R.id.submit_top_input);
			input = (EditText) findViewById(R.id.text_input_top_input);

		} else {
			setContentView(R.layout.working_frame_layout_bottom);
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
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (intent.hasExtra(BuddyListActivity.KEY_BUDDY_INDEX)) {
			buddy_id = bundle.getLong(BuddyListActivity.KEY_BUDDY_INDEX);
		}

		if (intent.hasExtra(BuddyListActivity.THREAD)) {
			thread = bundle.getString(BuddyListActivity.THREAD);
		}

		setupListView();

		/**
		 * TODO - figure out
		 * 
		 * There's a funny thing about pressing enter twice on the text input,
		 * it triggers some things, but doesn't actually cause the service to
		 * send the message
		 */
		submit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent messageIntent = new Intent(
						ACTION_REQUEST_DELIVER_MESSAGE);
				messageIntent.putExtra(THREAD, thread);
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

	private void setupListView() { // TODO broken! fix it!
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);

		QueryBuilder<MessageEntity> qb = daoSession.getMessageEntityDao()
				.queryBuilder();
		/*
		 * NOTE: this breaks because other_jid is not initialized
		 * qb.where(Properties.Receiver_jid.eq(other_jid)); qb.or(
		 * Properties.Sender_jid.eq(other_jid), null, null);
		 */
		
		qb.where(Properties.BuddyId.eq(buddy_id));
		// NOTE: this presents a challenge for group chat
		// You probably need a groupchat activity for this thing
		// and a different database

		List<MessageEntity> chat_history = qb.list();

		adapter = new MessageAdapter(this, chat_history, own_jid);

		if (chat_history.size() != 0) {
			chat_list.setAdapter(adapter);

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
