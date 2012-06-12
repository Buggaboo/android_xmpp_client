package nl.antonius.zorgdashboardwidget;

import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class BuddyListActivity extends ListActivity
{
	private static final String TAG = "BuddyListActivity";
	private static final boolean debug = false;

	// source:
	// http://united-coders.com/phillip-steffensen/android-dealing-with-listactivities-customized-listadapters-and-custom-designed-0

	public void onCreate(Bundle icicle)
	{

		super.onCreate(icicle);


		// makeToast("Constructing buddy list.");

		// makeToast("Constructing buddy adapter.");
		BuddyAdapter adapter = new BuddyAdapter(XMPPConnector.getRoster(),
				BuddyListActivity.this);		

		// makeToast("Set list adapter.");
		setListAdapter(adapter);
		
		// TODO indicate who you're already chatting with
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		
		// TODO - W make nicknames editable
		// TODO + M make offline people not a choice
		// TODO - M hide fuckers who are _always_ offline, give the user a dialog
		// TODO - W show away message if person is away
		
		Presence presence = XMPPConnector.getPresence(position);
				
		// presence
		if (presence.getType() == Presence.Type.available)
		{
			setResult(position); // set return value
			getIntent().putExtra("buddy_jid", XMPPConnector.getFullJID(position));
			// NOTE: in case the Roster changes due to logouts, confirm with getFullJID(...)
			finish();
		}
//		else if (presence.getType() == Presence.Type.unavailable)
//		{
//			
//		}

	}
	
}
