package nl.antonius.zorgdashboardwidget;

import java.util.Collection;
import java.util.Iterator;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BuddyAdapter extends BaseAdapter
{
	private static String TAG = "BuddyAdapter";
	Context context;
	// RosterEntry[] roster_array;
	Roster roster;

	public BuddyAdapter(Roster roster, Context c)
	{
		super();
		this.context = c;
		this.roster = roster;

		// TODO class List<String> -> Buddy with photo, clickable with other
		// attributes
	}

	public int getCount()
	{
		return roster.getEntries().size();
	}

	public Object getItem(int position)
	{
		// TODO fix!
		Collection<RosterEntry> entry_collection = roster.getEntries();
		return entry_collection.toArray()[position]; // TODO make more
														// efficient(?)
	}

	public long getItemId(int position)
	{
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO - merge full jids together (i.e. same e-mail prefix, different
		// resource string) -> tip:
		// StringUtils.parseBareAddress("jasm@meh.nl/resource-goes-away");

		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.buddyitem_row_layout, parent, false);

		TextView buddy_name = (TextView) itemLayout
				.findViewById(R.id.buddy_name);

		RosterEntry buddy = (RosterEntry) getItem(position);
		String nickname = buddy.getName();
		buddy_name.setText(nickname);

		Presence presence = roster.getPresence(buddy.getUser());

		// presence
		if (presence.getType() == Presence.Type.available)
		{
			buddy_name.setBackgroundColor(Color.GREEN);
		} else
		{
			buddy_name.setBackgroundColor(Color.RED);
		}

		// TODO set presence info in the last TextView
		return itemLayout;
	}

}
