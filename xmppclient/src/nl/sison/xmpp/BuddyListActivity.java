package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;

public class BuddyListActivity extends ListActivity {
	private ArrayAdapter<?> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshList();
	}

	private void refreshList() {
		// register context menu for the dialog
		registerForContextMenu(getListView());

//		List<ConnectionConfigurationEntity> all_conns = getAllConnectionConfigurations();
//		if (all_conns.size() > 0) {
//
//			adapter = new BuddyAdapter(,
//					BuddyListActivity.this);
//
//		} else {
//			// no buddies
//		}

		setListAdapter(adapter);

		DatabaseUtil.close();
	}

}
