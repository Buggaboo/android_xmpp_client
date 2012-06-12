package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao;
import nl.sison.xmpp.dao.DaoSession;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * @author Jasm Sison
 * 
 */
// TODO implement titlebar
// http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android
public class ConnectionListFragment extends ListFragment {
	private final static String TAG = "BuddyListFragment";
	private AlertDialog crudConnectionDialog;
	private ArrayAdapter<ConnectionConfigurationEntity> adapter;

	public final static int RQ_NEW_CONN = 0; // intent request code
	public final static int RQ_MODIFY_CONN = 1;
	public final static int RQ_DELETE_CONN = 2;

	public final static String KEY_CONNECTION_INDEX = "H#@$$**&*UAONETUH";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		// getActivity().registerReceiver(receiver, filter)
		// TODO implement green indicator the connection is online
		// TODO implement register broadcastreceiver
		refreshList();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// TODO implement register broadcastreceiver 
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			getActivity().unregisterReceiver(receiver);
		} catch (IllegalArgumentException ex) {
			makeToast("The receiver is not registered.");
		}
	}	

	private void refreshList() {
		// register context menu for the dialog
		registerForContextMenu(getListView());

		List<ConnectionConfigurationEntity> connections = getAllConnectionConfigurations();
		if (connections != null && connections.size() > 0) {
			adapter = new ConnectionAdapter(getActivity(), connections);
			setListAdapter(adapter);
		} else {
			setEmptyText(getString(R.string.conn_non_available));
			createCRConnectionDialog(getString(R.string.request_create_conn));
		}

		DatabaseUtils.close();
	}

	private List<ConnectionConfigurationEntity> getAllConnectionConfigurations() {
		// TODO get connections from database, produce array list
		DaoSession daoSession = DatabaseUtils
				.getReadOnlyDatabaseSession(getActivity());
		ConnectionConfigurationEntityDao conn_conf_dao = daoSession
				.getConnectionConfigurationEntityDao();
		List<ConnectionConfigurationEntity> all_conns = conn_conf_dao.loadAll();
		DatabaseUtils.close();
		return all_conns;
	}

	@Deprecated
	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast
				.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long cc_id) {
		super.onListItemClick(l, v, position, cc_id);
		Intent intent = new Intent(getActivity(), BuddyListFragment.class);
		intent.putExtra(KEY_CONNECTION_INDEX, cc_id);
		// startActivity(intent); // deprecated
		((SinglePanelActivity) getActivity()).loadFragment(intent);
		// TODO depending on the layout, load the fragment
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			long cc_id = getListAdapter().getItemId(info.position);
			createCRUDConnectionDialog(cc_id);
			crudConnectionDialog.show();
		} catch (ClassCastException e) {
			return;
		}
	}

	private void createCRConnectionDialog(CharSequence message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.create_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								createNewConnection();
							}
						});
		builder.create().show();
	}

	private void createNewConnection() {
		// startActivityForResult(new Intent(getActivity(),
		// CRUDConnectionFragment.class), RQ_NEW_CONN); // TODO - deprecate
		// field, remove field
		// TODO - determine if you really need startActivityForResult
		((FragmentLoader) getActivity()).loadFragment(new Intent(getActivity(),
				CRUDConnectionFragment.class)); // broken
	}

	private void modifyConnection(final long cc_id) {
		Intent intent = new Intent(getActivity(), CRUDConnectionFragment.class);
		intent.putExtra(KEY_CONNECTION_INDEX, cc_id);
		DatabaseUtils.close();
		((FragmentLoader) getActivity()).loadFragment(intent); // TODO - fix
																// broken
	}

	private void deleteConnection(final long cc_id) {
		// makeToast("Deleting " + message);
		DaoSession daoSession = DatabaseUtils
				.getWriteableDatabaseSession(getActivity());
		ConnectionConfigurationEntityDao ccdao = daoSession
				.getConnectionConfigurationEntityDao();
		ccdao.deleteByKey(cc_id);
		DatabaseUtils.close();
	}

	private void createDialogDeleteConnection(final long cc_id,
			final String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getString(R.string.sure_remove_conn) + message)
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// makeToast("yes, delete connection");
								deleteConnection(cc_id);
								refreshList();
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// makeToast("no, preserve connection");
							}
						}).create().show();
	}

	private void createCRUDConnectionDialog(final long cc_id) {
		ConnectionConfigurationEntity cc = DatabaseUtils
				.getReadOnlyDatabaseSession(getActivity()).load(
						ConnectionConfigurationEntity.class, cc_id);
		final String message = cc.getLabel();

		// TODO @ set presence with dialog per provider, no move that to the
		// buddylistfrag
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
				.setCancelable(true)
				.setPositiveButton(R.string.create_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// makeToast("create connection");
								createNewConnection();
								// TODO - send Intent to service for reconnect,
								// (easiest way: restart service)
							}
						})
				.setNegativeButton(R.string.remove_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// makeToast("delete connection");
								createDialogDeleteConnection(cc_id, message);
								// TODO - send Intent to service for reconnect,
								// (easiest way: restart service)
							}

						})
				.setNeutralButton(R.string.modify_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// makeToast("update connection");
								modifyConnection(cc_id);
								// TODO - send Intent to service for reconnect,
								// (easiest way: restart service)
							}
						});
		crudConnectionDialog = builder.create();
	}
}
