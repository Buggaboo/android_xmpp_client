package nl.sison.xmpp;

import java.util.ArrayList;
import java.util.List;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.DaoSession;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BuddyListActivity extends ListActivity {
	private static final String TAG = "BuddyListActivity";
	public static final int RC_CREATE_NEW_THREAD_FROM_JID = 1;
	public static final int RC_CONTINUE_OLD_THREAD = 1;
	private ArrayAdapter<?> adapter;
	private BroadcastReceiver receiver;

	public class BuddyListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			makeToast("Enter onReceive");
			if (intent.getAction().equals(XMPPService.ACTION_BUDDY_NEW_MESSAGE)) {
				if (intent.hasExtra(XMPPService.FROM_JID)) {

				}
			}
			if (intent.getAction().equals(
					XMPPService.ACTION_BUDDY_PRESENCE_UPDATE)) {
				if (intent.hasExtra(XMPPService.KEY_BUDDY_INDEX)) {
					makeToast("buddy row index: "
							+ intent.getLongExtra(XMPPService.KEY_BUDDY_INDEX,
									0));
				}
//				makeToast("Refreshing adapter");
				refreshList();
//				adapter.notifyDataSetChanged();
			}
			if (intent.getAction().equals(XMPPService.ACTION_CONNECTION_LOST)) {

			}
			makeToast("Exit onReceive");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getIntent().getExtras().getLong(
				ConnectionListActivity.CONNECTION_ROW_INDEX);

		IntentFilter actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_BUDDY_NEW_MESSAGE);
		actionFilter.addAction(XMPPService.ACTION_BUDDY_PRESENCE_UPDATE);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_LOST);
		receiver = new BuddyListReceiver();
		registerReceiver(receiver, actionFilter);

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Intent intent = new Intent(BuddyListActivity.this, ChatActivity.class);
		
//		intent.setAction(ACTION_REQUEST_CHAT);
		// TODO
		
		// - match jid
		
		// - get chronological last chat thread
		
		// - confirm chat recent

//		startActivityForResult(intent, RC_CREATE_NEW_THREAD_FROM_JID);
//		startActivityForResult(intent, RC_CONTINUE_OLD_THREAD);
	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshList();
	}

	/**
	 * TODO reimplement using notifyDataSetChanged
	 * http://stackoverflow.com/questions/3669325/notifydatasetchanged-example/5092426#5092426
	 */
	private void refreshList() {
		// TODO finish context menu for the dialog
		adapter = null;
		registerForContextMenu(getListView());
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		List<BuddyEntity> buddies = daoSession.getBuddyEntityDao().loadAll();

		if (buddies == null || buddies.size() == 0) // TODO determine if
													// necessary
		{
			DatabaseUtil.close();
			return;
		}

		adapter = new BuddyAdapter(this, new ArrayList<BuddyEntity>(buddies));
		DatabaseUtil.close();
		setListAdapter(adapter);
	}
	
	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
