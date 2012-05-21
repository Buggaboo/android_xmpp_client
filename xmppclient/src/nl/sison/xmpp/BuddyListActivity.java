package nl.sison.xmpp;

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BuddyListActivity extends ListActivity {
	private static final String TAG = "BuddyListActivity";
	private ArrayAdapter<?> adapter;
	private XMPPService service;
	private ArrayList<RosterEntry> buddies;
	private Roster roster;
	private long conn_id;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((XMPPService.LocalBinder) binder).getService();
			makeToast("service is bound: " + (service != null));
		}

		public void onServiceDisconnected(ComponentName className) {
			service = null;
			roster = null;
			makeToast("service is disconnected");
		}
	};

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
		serviceConnection = null; // TODO determine this prevents a leak? // known leaky bug: since the
									// anonymous non static type has a leaky
									// weak reference to the spawning class
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
		doBindService();
		getServiceData();
		if (buddies != null && !buddies.isEmpty() && roster != null) {
			adapter = new BuddyAdapter(this, buddies, roster);
			makeToast("BuddyAdapter is refreshed");
		}
		unbindService(serviceConnection);
		setListAdapter(adapter);
	}

	void doBindService() {
		boolean b = bindService(new Intent(BuddyListActivity.this,
				XMPPService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		makeToast("bind to service (XMPPService): " + b);
	}

	public void getServiceData() {
		if (service != null) {
			buddies.clear();
			roster = service.getRoster(conn_id);
			buddies.addAll(roster.getEntries());
		}
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
