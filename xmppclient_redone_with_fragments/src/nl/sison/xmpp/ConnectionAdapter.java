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

/**
 * 
 * @author Jasm Sison
 * 
 */
public class ConnectionAdapter extends
		ArrayAdapter<ConnectionConfigurationEntity> {

	public ConnectionAdapter(Context context,
			List<ConnectionConfigurationEntity> connections) {
		super(context, 0);
		this.setNotifyOnChange(true);
		if (connections != null && connections.size() > 0) {
			this.addAll(connections);
		}
	}

	@Override
	public long getItemId(int position) {
		return super.getItem(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(
				getContext()).inflate(R.layout.connection_item_layout, parent, false);

		TextView connection_view = (TextView) itemLayout
				.findViewById(R.id.connection_name);

		ConnectionConfigurationEntity item = getItem(position);
		connection_view.setText(item.getLabel());

		return itemLayout;
	}
}
