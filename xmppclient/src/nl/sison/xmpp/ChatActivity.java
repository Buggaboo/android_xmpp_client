package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntity;
import nl.sison.xmpp.dao.MessageEntityDao.Properties;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.greenrobot.dao.QueryBuilder;

public class ChatActivity extends Activity {
	/**
	 * Intent action
	 */
	public static final String ACTION_REQUEST_DELIVER_MESSAGE = "23yididxb3@#$%";

	/**
	 * Intent extras
	 */
	public static final String THREAD = "23yididxb3@#$%444";
	public static final String MESSAGE = "23yid´´idxb3@#$%444";
	public static final String KEY_BUDDY_INDEX = "23yid´´idxb3@#$Z44";
	

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

	class MessageBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_SENT)) {
				long message_id = intent.getExtras().getLong(
						XMPPService.KEY_MESSAGE_INDEX);
				DaoSession daoSession = DatabaseUtil
						.getReadOnlyDatabaseSession(context);
				MessageEntity message = daoSession.load(MessageEntity.class,
						message_id);
				adapter.add(message);
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		receiver = new MessageBroadcastReceiver();
		IntentFilter actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_SENT); // DONE!
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_ERROR);
		actionFilter.addAction(XMPPService.ACTION_BUDDY_NEW_MESSAGE);
		actionFilter.addAction(XMPPService.ACTION_BUDDY_PRESENCE_UPDATE);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_LOST);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_RESUMED);
		registerReceiver(receiver, actionFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();

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

		Intent intent = getIntent();
		if (intent.hasExtra(XMPPService.KEY_BUDDY_INDEX)) {
			buddy_id = intent.getExtras().getLong(XMPPService.KEY_BUDDY_INDEX);
		}

		setupListView();

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

	}

	private void setupListView() {
		// TODO - read from the database
		// ArrayList<HashMap<String, String>> chat_history = new
		// ArrayList<HashMap<String, String>>();
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		QueryBuilder<MessageEntity> qb = daoSession.getMessageEntityDao()
				.queryBuilder();
		qb.where(Properties.Receiver_jid.eq(other_jid));
		qb.or(Properties.Sender_jid.eq(other_jid), null, null); // TODO or match
																// against
																// thread
		List<MessageEntity> chat_history = qb.list();

		// qb.where(Properties.Thread.eq(thread)); // TODO instead of matching
		// by jid

		adapter = new MessageAdapter(this, chat_history, own_jid);

		if (chat_history.size() != 0) {
			chat_list.setAdapter(adapter);

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}
}
