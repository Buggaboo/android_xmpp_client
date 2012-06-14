package nl.sison.xmpp;

import java.util.List;

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

	public BuddyAdapter(Context context, List<BuddyEntity> buddies) {
		super(context, 0);
		this.setNotifyOnChange(true);
//		this.addAll(buddies);
		for (BuddyEntity b : buddies) {
			add(b);
		}
	}
	
	@Override
	public long getItemId(int position) {
		return super.getItem(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(
				getContext()).inflate(R.layout.buddy_item_layout, parent, false);

		BuddyEntity buddy = getItem(position);

		setBuddyIdentifierText(itemLayout, buddy);
		setPresenceView(itemLayout, buddy);

		return itemLayout;
	}
	
	private void setBuddyIdentifierText(ViewGroup parent, BuddyEntity buddy) {
		TextView buddy_jid_view = (TextView) parent
				.findViewById(R.id.buddy_item_jid);
		TextView buddy_nickname_view = (TextView) parent
				.findViewById(R.id.buddy_item_nickname);
		
		buddy_jid_view.setText(buddy.getPartial_jid() + "c TODO remove");
		
		if (buddy.getNickname() == null || buddy.getNickname().isEmpty()) {
			buddy_nickname_view.setText(buddy.getPartial_jid() + "a TODO remove");
		} else {
			buddy_nickname_view.setText(buddy.getNickname() + "b TODO remove");
		}

	}

	private void setPresenceView(ViewGroup parent, BuddyEntity buddy) {
		TextView buddy_presence_view = (TextView) parent
				.findViewById(R.id.buddy_presence);
		if (buddy.getIsAvailable()) {
			buddy_presence_view.setBackgroundColor(Color.GREEN);
		}else if (buddy.getIsAway()) {
			buddy_presence_view.setBackgroundColor(Color.MAGENTA);
		} else {
			buddy_presence_view.setBackgroundColor(Color.RED);
		}
	}
}
