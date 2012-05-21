package nl.sison.xmpp;

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BuddyListActivity extends ListActivity {
	private static final String TAG = "BuddyListActivity";
	private ArrayAdapter<?> adapter;
	private ArrayList<RosterEntry> buddies;
	private Roster roster;
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
		// register context menu for the dialog
		registerForContextMenu(getListView());
		if (buddies != null && !buddies.isEmpty() && roster != null) {
			adapter = new BuddyAdapter(this, buddies, roster);
			makeToast("BuddyAdapter is refreshed");
		}
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
