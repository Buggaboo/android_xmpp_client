package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConnectionAdapter extends ArrayAdapter
{
	private final static String TAG = "ConnectionAdapter";
	private List<ConnectionConfigurationEntity> connections;

	public ConnectionAdapter(List<ConnectionConfigurationEntity> all_conns, Context context)
	{
		super(context, 0);
		this.connections = all_conns;
	}

	public int getCount()
	{
		return connections.size();
	}

	public Object getItem(int position)
	{
		return (Object) connections.get(position);
	}

	public long getItemId(int position)
	{
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(getContext())
				.inflate(R.layout.connection_item, parent, false);
		
		TextView connection_view = (TextView) itemLayout
				.findViewById(R.id.connection_name);

		ConnectionConfigurationEntity item = (ConnectionConfigurationEntity) getItem(position);
		connection_view.setText(item.getLabel());		
		
		return itemLayout;
	}
}
