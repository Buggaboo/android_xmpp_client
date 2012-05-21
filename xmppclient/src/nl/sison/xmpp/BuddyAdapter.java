package nl.sison.xmpp;

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BuddyAdapter extends ArrayAdapter<RosterEntry> {

	private ArrayList<RosterEntry> buddies;
	private Roster roster;

	public BuddyAdapter(Context context, ArrayList<RosterEntry> buddies,
			Roster roster) {
		super(context, 0);
		this.buddies = buddies;
		this.roster = roster;
		// TODO Auto-generated constructor stub
	}

	public int getCount() {
		return buddies.size();
	}

	public RosterEntry getItem(int position) {
		return (RosterEntry) buddies.toArray()[position];
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(
				getContext()).inflate(R.layout.buddy_item, parent, false);

		RosterEntry buddy = (RosterEntry) buddies.toArray()[position];

		setBuddyIdentifierText(parent, buddy);
		setPresenceView(parent, buddy);

		return itemLayout;

	}

	private void setBuddyIdentifierText(ViewGroup parent, RosterEntry buddy) {
		TextView buddy_jid_view = (TextView) parent
				.findViewById(R.id.buddy_jid);
		buddy_jid_view.setText(buddy.getName() + "/" + buddy.getUser());
	}

	private void setPresenceView(ViewGroup parent, RosterEntry buddy) {

		TextView buddy_presence_view = (TextView) parent
				.findViewById(R.id.buddy_presence);
		if (isBuddyAvailable(buddy)) {
			buddy_presence_view.setBackgroundColor(Color.GREEN);
		} else {
			buddy_presence_view.setBackgroundColor(Color.RED);
		}
	}

	private boolean isBuddyAvailable(RosterEntry buddy) {
		return roster.getPresence(buddy.getUser()).getType()
				.equals(Presence.Type.available);
	}

}
