package nl.sison.xmpp;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.dao.QueryBuilder;

import nl.sison.xmpp.dao.MessageEntity;
import nl.sison.xmpp.dao.MessageEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {
	ListView chat_list;
	private boolean top_orientation; // TODO create dialog
	private Button submit;
	private EditText input;
	private ArrayAdapter chat_adapter;
	private String own_jid, other_jid;
//	private String group_chat_jids; // TODO
//	private String group_chat_thread; // TODO

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// this.setListAdapter(adapter);
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
			intent.getExtras().getLong(XMPPService.KEY_BUDDY_INDEX);
		}

		// create the grid item mapping
		setupListView();
		
	}

	private void setupListView() {
		String[] from = new String[]
		{ getString(R.string.date_literal), getString(R.string.text_literal) };
		int[] to = new int[]
		{ R.id.chat_item_date, R.id.chat_item_text };

		// TODO - read from the database
//		ArrayList<HashMap<String, String>> chat_history = new ArrayList<HashMap<String, String>>();
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		QueryBuilder<MessageEntity> qb = daoSession.getMessageEntityDao().queryBuilder();
		qb.where(Properties.Receiver_jid.eq(other_jid));
		qb.or(Properties.Sender_jid.eq(other_jid), null, null);
//		.where(.or();

//		chat_adapter = new MessageAdapter(this, chat_history, R.layout.chat_item, from, to);
//		chat_list.setAdapter(chat_adapter);
	}
}
