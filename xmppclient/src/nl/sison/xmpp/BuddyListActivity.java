package nl.sison.xmpp;

import java.util.ArrayList;
import java.util.List;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.DaoSession;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BuddyListActivity extends ListActivity {
	private static final String TAG = "BuddyListActivity";
	private ArrayAdapter<?> adapter;
	private long conn_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		conn_id = getIntent().getExtras().getLong(
				ConnectionListActivity.CONNECTION_ROW_INDEX);
		makeToast("connection index:" + conn_id);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshList();
		makeToast("onResume");
	}

	private void refreshList() {
		// TODO finish context menu for the dialog
		registerForContextMenu(getListView());
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		List<BuddyEntity> buddies = daoSession.getBuddyEntityDao().loadAll();

		makeToast("buddies.size(): " + buddies.size());
		
		if (buddies == null || buddies.size() == 0) // TODO determine if necessary
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
