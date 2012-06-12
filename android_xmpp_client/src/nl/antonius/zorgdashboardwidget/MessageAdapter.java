package nl.antonius.zorgdashboardwidget;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MessageAdapter extends SimpleAdapter
{
	boolean own_message;

	public MessageAdapter(Context context,
			List<HashMap<String, String>> fillMaps, int view_group_resource_id,
			String[] from, int[] to)
	{
		super(context, fillMaps, view_group_resource_id, from, to);
	}

	public int getCount()
	{
		return super.getCount();
	}

	public Object getItem(int position)
	{
		return super.getItem(position);
	}

	public long getItemId(int position)
	{
		return super.getItemId(position);
	}

	public void alterViews(boolean b)
	{
		own_message = b;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = super.getView(position, convertView, parent);
		// TODO - test if necessary: merge full jids together (i.e. same e-mail
		// prefix, different
		// resource string)
		// TODO change color of background etc. when own_message:boolean ==
		// false

		if (!own_message)
			return v;

		TextView text_date = (TextView) v.findViewById(R.id.chat_item_date);
		TextView text_message = (TextView) v.findViewById(R.id.chat_item_text);

		text_date.setBackgroundColor(Color.GRAY);
		text_message.setBackgroundColor(Color.GRAY);

		text_date.setTextColor(Color.BLACK);
		text_message.setTextColor(Color.BLACK);

		return v;
	}

}
