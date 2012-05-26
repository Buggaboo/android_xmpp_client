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
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BuddyListActivity extends ListActivity {
	private static final String TAG = "BuddyListActivity";
	private ArrayAdapter<?> adapter;
	private long conn_id;
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
				// if (intent.hasExtra(XMPPService.JID)) {
				//
				// }
				// if(intent.hasExtra(XMPPService.KEY_BUDDY_INDEX))
				// {
				//
				// }
				makeToast("Refreshing adapter");
				refreshList();

			}
			if (intent.getAction().equals(XMPPService.ACTION_CONNECTION_LOST)) {

			}
			makeToast("Exit onReceive");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		conn_id = getIntent().getExtras().getLong(
				ConnectionListActivity.CONNECTION_ROW_INDEX);

		IntentFilter actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_BUDDY_NEW_MESSAGE);
		actionFilter.addAction(XMPPService.ACTION_BUDDY_PRESENCE_UPDATE);
		actionFilter.addAction(XMPPService.ACTION_CONNECTION_LOST);
		receiver = new BuddyListReceiver();
		registerReceiver(receiver, actionFilter);

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
		makeToast("onResume");
	}

	private void refreshList() {
		// TODO finish context menu for the dialog
		adapter = null;
		registerForContextMenu(getListView());
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		List<BuddyEntity> buddies = daoSession.getBuddyEntityDao().loadAll();

		makeToast("buddies.size(): " + buddies.size());

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
