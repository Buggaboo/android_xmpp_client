package nl.sison.xmpp;

import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import nl.sison.xmpp.dao.MessageEntity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * @author Jasm Sison
 * 
 */
public class MessageAdapter extends ArrayAdapter<MessageEntity> {
	private String own_jid;

	public MessageAdapter(Context context, List<MessageEntity> chat_history,
			String jid) {
		super(context, 0);
		this.setNotifyOnChange(true);
		if (chat_history != null && chat_history.size() > 0)
			this.addAll(chat_history); // TODO empty list, throw
										// IllegalArguments, then catch in
										// Fragment then add empty list_item
										// with some bullshit text like:
										// "no history"
		this.own_jid = jid;
	}

	@Override
	public long getItemId(int position) {
		// TODO - give option to select the message, load in an e-mail editor or
		// something
		return super.getItem(position).getId();
	}

	/**
	 * TODO - Remove inflater, replace inflater with real objects (no xml is
	 * faster)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(
				getContext()).inflate(R.layout.chat_item_layout, parent, false);

		TextView text_date = (TextView) itemLayout
				.findViewById(R.id.chat_item_date);
		TextView text_message = (TextView) itemLayout
				.findViewById(R.id.chat_item_text);

		MessageEntity msg = getItem(position);

		changeAppearance(text_date, text_message, msg);

		return itemLayout;
	}

	private void changeAppearance(TextView text_date, TextView text_message,
			MessageEntity msg) {
		text_date.setText(msg.getReceived_date().toString() + " "
				+ msg.getSender_jid());
		text_message.setText(msg.getContent());

		text_date.setTextColor(Color.WHITE);
		text_message.setTextColor(Color.WHITE);

		String sender_partial_jid = StringUtils.parseBareAddress(msg
				.getSender_jid());
		if (own_jid.equals(sender_partial_jid)) {
			text_date.setBackgroundColor(Color.GRAY);
			text_message.setBackgroundColor(Color.GRAY);
		} else {
			text_date.setBackgroundColor(Color.BLACK);
			text_message.setBackgroundColor(Color.BLACK);
		}
	}
}
