package nl.sison.xmpp;

import java.util.ArrayList;

import nl.sison.xmpp.dao.BuddyEntity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BuddyAdapter extends ArrayAdapter<BuddyEntity> {

	private ArrayList<BuddyEntity> buddies;

	public BuddyAdapter(Context context, ArrayList<BuddyEntity> buddies) {
		super(context, 0);
		this.buddies = buddies;
	}

	public int getCount() {
		return buddies.size();
	}

	public BuddyEntity getItem(int position) {
		return (BuddyEntity) buddies.get(position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(
				getContext()).inflate(R.layout.buddy_item, parent, false);

		BuddyEntity buddy = getItem(position);

		setBuddyIdentifierText(itemLayout, buddy);
		setPresenceView(itemLayout, buddy);

		return itemLayout;
	}

	private void setBuddyIdentifierText(ViewGroup parent, BuddyEntity buddy) {
		TextView buddy_jid_view = (TextView) parent
				.findViewById(R.id.buddy_jid);
		if (buddy.getNickname() != null) {
			buddy_jid_view.setText(buddy.getNickname());
		} else {
			buddy_jid_view.setText(buddy.getPartial_jid());
		}

	}

	private void setPresenceView(ViewGroup parent, BuddyEntity buddy) {
		TextView buddy_presence_view = (TextView) parent
				.findViewById(R.id.buddy_presence);
		if (buddy.getIsAvailable()) {
			buddy_presence_view.setBackgroundColor(Color.GREEN);
			if (buddy.getIsAway())
				buddy_presence_view.setBackgroundColor(Color.MAGENTA);
		} else {
			buddy_presence_view.setBackgroundColor(Color.RED);
		}
	}
}
