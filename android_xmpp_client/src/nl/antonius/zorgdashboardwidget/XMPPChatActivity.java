package nl.antonius.zorgdashboardwidget;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nl.antonius.zorgdashboardwidget.dao.DaoMaster;
import nl.antonius.zorgdashboardwidget.dao.DaoMaster.OpenHelper;
import nl.antonius.zorgdashboardwidget.dao.DaoSession;
import nl.antonius.zorgdashboardwidget.dao.MessageEntity;
import nl.antonius.zorgdashboardwidget.dao.MessageEntityDao;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import de.greenrobot.dao.QueryBuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class XMPPChatActivity extends Activity
{

	static private boolean top_orientation = false;
	Button submit;

	ListView chat_list;
	private MessageAdapter chat_adapter;

	private AlertDialog quitDialog;
	private Dialog settingsDialog;
	private static final String TAG = "XMPPChatActivity";
	private EditText input;
	private int buddy_id;
	private List<HashMap<String, String>> im_trail;
	private static boolean debug = true;
	// private String full_jid; // TODO refactor to XMPPConnector
	private MessageHandler messageHandler;
	private static int PICK_BUDDY = 0; // for the Intent requestCode.

	private OpenHelper daoHelper;
	private DaoMaster daoMaster;
	private DaoSession daoSession;

	// private Handler handler;

	private void setupDatabase()
	{
		// TODO
//		daoMaster = new DaoMaster(db); daoSession = daoMaster.newSession();
//		// TODO create tables if they don't exist
//		DaoMaster.createAllTables(new SQLiteDatabase(), true);

		daoHelper = new DaoMaster.DevOpenHelper(this, "XMPPMessages", null);
		daoMaster = new DaoMaster(daoHelper.getWritableDatabase());
		daoSession = daoMaster.newSession();
	}

	private List<HashMap<String, String>> getAllMessages()
	{
		String buddy_jid = XMPPConnector.getFullJID(buddy_id);
		MessageEntityDao messageDao = daoSession.getMessageEntityDao();
		QueryBuilder<MessageEntity> qb = (QueryBuilder<MessageEntity>) messageDao
				.queryBuilder().orderDesc(MessageEntityDao.Properties.Received_date)
				.where(MessageEntityDao.Properties.Sender_jid.eq(buddy_jid))
				.or(MessageEntityDao.Properties.Receiver_jid.eq(buddy_jid), null,
						null);
		
//		List list = qb.list();
		for (MessageEntity m : qb.listLazy())
		{
			
		}
		
		
		// TODO - M order by date
		return null;
	}

	// // Processes all incoming messages
	// private void addChatListeners(MessageHandler messageHandler)
	// {
	// // Add a packet listener to get all messages sent to us
	// PacketFilter filter = new MessageTypeFilter(Message.Type.chat); // except
	// this filter gets in the way
	// XMPPConnector.addPacketListener(messageHandler, filter);
	// }

	// private void setupInterThreadCommunication()
	// {
	// handler = new Handler();
	// }

	private void connectAndAuthenticate()
	{

		try
		{
			// TODO - haal op uit database
			// TODO - haal op encrypted!
			String server = getString(R.string.xmpp_server_name);
			String username = getString(R.string.xmpp_username);
			String password = getString(R.string.xmpp_password);
			String resource = getString(R.string.xmpp_resource);
			// full_jid = username + '@' + getString(R.string.xmpp_jid_domain)
			// + '/' + resource; // TODO get login data from DAO

			XMPPConnector.connect(server, username, password, resource);
			// makeToast("Attempting to connect to XMPP server.");

		} catch (ExceptionInInitializerError e)
		{
			makeToast("DNS resolution error (probably).");
			createQuitDialog(R.string.unable_to_connect_to_server);
			quitDialog.show();
			return;
		}

	}

	private int getBuddyIdFromIntent()
	{
		int buddy_id = getIntent().getIntExtra("buddy_id", -1);
		makeToast("buddy: " + buddy_id);
		/**
		 * TODO - create chat fragment(?)
		 */
		return buddy_id;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// makeToast("Create.");
		buddy_id = getBuddyIdFromIntent();
		// after this onResume is called
		// setupInterThreadCommunication();
	}

	private void createQuitDialog(int string_resource_id)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(string_resource_id)
				.setCancelable(true)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								quitDialog.dismiss();
								XMPPChatActivity.this.finish();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								dialog.cancel();
								onResume();
							}
						})
				.setNeutralButton(R.string.settings,
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								createSettingsDialog();
								settingsDialog.show();
							}
						});
		quitDialog = builder.create();
	}

	private void createSettingsDialog()
	{
		if (settingsDialog != null)
			return;
		settingsDialog = new Dialog(XMPPChatActivity.this);
		settingsDialog.setContentView(R.layout.custom_settings_dialog);

		Button b = (Button) settingsDialog.findViewById(R.id.btn_settings_back);
		b.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				settingsDialog.hide();
			}
		});

		b = (Button) settingsDialog.findViewById(R.id.btn_settings_save);
		b.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				// TODO save settings to DB
				makeToast("TODO: save server settings to db.");
			}
		});
	}

	private boolean invalidBuddyIndex(int b)
	{
		return b < 0;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// makeToast("Resume.");

		if (!testConnectionAndServer())
			return;

		if (invalidBuddyIndex(buddy_id))
		{
			startActivityForResult(new Intent(XMPPChatActivity.this,
					BuddyListActivity.class), PICK_BUDDY);
		} else
		{
			messageHandler = new MessageHandler(this, buddy_id);
			// addChatListeners(messageHandler);
			setupViews(messageHandler);
		}
	}

	private boolean notConnectedAndNotAuthenticated()
	{
		return !(XMPPConnector.isConnected() || XMPPConnector.isAuthenticated());
	}

	private boolean testConnectionAndServer()
	{
		if (XMPPConnector.hasNoConnectivity(getApplication()))
		{
			createQuitDialog(R.string.no_connectivity);
			return false;
		}

		if (notConnectedAndNotAuthenticated())
		{
			// makeToast("Attempting to connect and authenticate.");
			connectAndAuthenticate();
			// TODO offer offline mode, read database entries
		}

		// TODO wait symbols and delay, try x number of times then show quit
		// dialog
		if (notConnectedAndNotAuthenticated())
		{
			createQuitDialog(R.string.unable_to_connect_to_server);
			return false;
		}

		makeToast("Connected and authenticated.");
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int buddy_id, Intent data)
	{
		super.onActivityResult(requestCode, buddy_id, data);

		if (requestCode == PICK_BUDDY)
		{
			this.buddy_id = buddy_id;
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		XMPPConnector.disconnect();
		makeToast("Destroy.");
	}

	@Override
	public void onBackPressed()
	{
		// super.onBackPressed(); // NOTE: turn on -> auto quit
		createQuitDialog(R.string.are_you_sure_you_want_to_exit);
		quitDialog.show();
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event)
	{
		makeToast("keycode: " + keycode);
		if (keycode == KeyEvent.KEYCODE_MENU)
		{
			startActivityForResult(new Intent(XMPPChatActivity.this,
					BuddyListActivity.class), 0);
		}
		return super.onKeyDown(keycode, event);
	}

	private void makeToast(String message)
	{
		// TODO - refactor to XMPPConnector
		if (!debug)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}

	private void setupViews(MessageHandler messageHandler)
	{
		if (top_orientation)
		{
			setContentView(R.layout.working_frame_layout_top);
			chat_list = (ListView) findViewById(R.id.chat_top_input);
			submit = (Button) findViewById(R.id.submit_top_input);
			input = (EditText) findViewById(R.id.text_input_top_input);

		} else
		{
			setContentView(R.layout.working_frame_layout_bottom);
			chat_list = (ListView) findViewById(R.id.chat_bottom_input);
			submit = (Button) findViewById(R.id.submit_bottom_input);
			input = (EditText) findViewById(R.id.text_input_bottom_input);
		}

		// TODO - W send a request ImageSwitch or ViewAnimator wait symbol
		setupListView();
		setupSubmitButton(messageHandler);

	}

	/**
	 * TODO refactor custom adapter
	 */
	private void setupListView()
	{
		// create the grid item mapping
		String[] from = new String[]
		{ getString(R.string.date_literal), getString(R.string.text_literal) };
		int[] to = new int[]
		{ R.id.chat_item_date, R.id.chat_item_text };

		// TODO - read from the database
		im_trail = new ArrayList<HashMap<String, String>>();

		chat_adapter = new MessageAdapter(this, im_trail, R.layout.chat_item,
				from, to);
		chat_list.setAdapter(chat_adapter);

	}

	private void setupSubmitButton(MessageHandler messageHandler)
	{
		// setup submit button
		submit.setOnClickListener(messageHandler);
	}

	// NOTE: This was previously in MessageHandler but the ListView didn't want
	// to update
	void returnFocusToInput()
	{
		input.setText("");
	}

	// NOTE: This was previously in MessageHandler but the ListView didn't want
	// to update
	public void showMessage(String input, final boolean own_message)
	{
		// TODO + M refactor AdapterView to show different background color

		HashMap<String, String> map = new HashMap<String, String>();

		map.put(getString(R.string.date_literal),
				formatStringDatetime(new Date(System.currentTimeMillis())));
		map.put(getString(R.string.text_literal), input);

		if (top_orientation)
		{
			im_trail.add(0, map); // TODO - M if and only if not equal to
									// previous message; (i.e. you can't
									// input the same shit again)
		} else
		{
			im_trail.add(map);
		}

		// handler.post(new Runnable()
		// {
		// public void run()
		// {
		// Log.i(TAG, "Testing 1");
		chat_adapter.alterViews(own_message);
		// Log.i(TAG, "Testing 2");
		chat_adapter.notifyDataSetChanged();
		// Log.i(TAG, "Testing 3");
		returnFocusToInput();
		// }
		// });
	}

	// NOTE: This was previously in MessageHandler but the ListView didn't want
	// to update
	public String formatStringDatetime(Date d)
	{
		// TODO W "Vandaag om <meh> uur / meh minuten geleden"
		return d.toGMTString();
	}

	private class MessageHandler implements View.OnClickListener/*
																 * ,
																 * PacketListener
																 */
	{
		private String buddy_jid;
		private Chat chat;
		private XMPPChatActivity context;

		// private String thread;
		// TODO - S implement thread support in message

		public MessageHandler(XMPPChatActivity context, int buddy_id)
		{
			this(context, XMPPConnector.getFullJID(buddy_id));
		}

		public MessageHandler(XMPPChatActivity context, String buddy_jid)
		{
			// TODO
			// - fix onCreate setupDatabase, request partial jid from Database

			this.context = context;
			// makeToast("Chatting with a buddy: " + buddy_jid);
			this.buddy_jid = buddy_jid;

			chat = XMPPConnector.getChatManager().createChat(buddy_jid, null);

			// process all incoming messages from a single buddy
			// chat.addMessageListener(new MessageListener()
			// {
			// public void processMessage(Chat c, Message m)
			// {
			// Log.i(TAG, "Testing all systems:" + m.getBody());
			// }
			// });
		}

		public void onClick(View v)
		{
			String sanitized = sanitizeString(input.getText());
			if (sanitized.length() == 0)
				return;

			try
			{
				// TODO store in db: not yet delivered messages
				chat.sendMessage(sanitized);
			} catch (XMPPException e)
			{
				makeToast("Unable to deliver message to " + buddy_jid);
				// TODO - retry after x times

				// TODO - set in db: flag sent/delivered
				e.printStackTrace();
			}

			showMessage(sanitized, true);
			// TODO M - store in db

			returnFocusToInput();
		}

		public void showMessage(String input, final boolean own_message)
		{
			context.showMessage(input, own_message);
		}

		private String sanitizeString(String s)
		{
			return s.trim();
		}

		private String sanitizeString(CharSequence s)
		{
			return sanitizeString(s.toString());
		}

		// process all messages from everybody, unless you put on a filter
		// public void processPacket(Packet packet)
		// {
		// Message message = (Message) packet;
		// if (message.getBody() != null)
		// {
		// String fromName = StringUtils.parseBareAddress(message
		// .getFrom());
		//
		// Log.i(TAG, "processPacket: Got text [" + message.getBody()
		// + "] from [" + fromName + "]");
		//
		// // TODO - store all messages in in database, only add
		// // messages to the im_trail, then notify gui for changes
		//
		// // showMessage(message.getBody(), false);
		//
		// // messages.add(fromName + ":");
		// // messages.add(message.getBody()); // Add the incoming
		// // message to the list view mHandler.post(new Runnable() {
		// // public void run() { setListAdapter(); } });
		//
		// }
		//
		// }

	}

}
