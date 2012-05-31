package nl.sison.xmpp;

import java.util.List;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;
import android.app.AlertDialog;
import android.app.ListActivity;
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
import de.greenrobot.dao.QueryBuilder;

/**
 * 
 * @author jasm
 *
 */
// TODO implement titlebar http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android
public class ConnectionListActivity extends ListActivity {
	private final static String TAG = "BuddyListActivity";
	private AlertDialog crudConnectionDialog;
	private ArrayAdapter<?> adapter;

	public final static int RQ_NEW_CONN = 0; // intent request code
	public final static int RQ_MODIFY_CONN = 1;
	public final static int RQ_DELETE_CONN = 2;

	public final static String CONNECTION_ROW_INDEX = "USTHUASNOEH#@$$**&*UAONETUH";

	// source:
	// http://united-coders.com/phillip-steffensen/android-dealing-with-listactivities-customized-listadapters-and-custom-designed-0

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		startService(new Intent(ConnectionListActivity.this, XMPPService.class));
	}

	@Override
	protected void onResume() {
		super.onResume();
//		makeToast("onResume");
		refreshList();
	}

	private void refreshList() {
		// register context menu for the dialog
		registerForContextMenu(getListView());

		List<ConnectionConfigurationEntity> all_conns = getAllConnectionConfigurations();
		if (all_conns.size() > 0) {

			adapter = new ConnectionAdapter(all_conns,
					ConnectionListActivity.this);

		} else {
			createCRConnectionDialog(getString(R.string.request_create_conn));
		}

		setListAdapter(adapter);

		DatabaseUtil.close();
	}

	private List<ConnectionConfigurationEntity> getAllConnectionConfigurations() {
		// TODO get connections from database, produce array list
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		ConnectionConfigurationEntityDao conn_conf_dao = daoSession
				.getConnectionConfigurationEntityDao();
		List<ConnectionConfigurationEntity> all_conns = conn_conf_dao.loadAll();
		DatabaseUtil.close();
		return all_conns;
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(ConnectionListActivity.this,
				BuddyListActivity.class);
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		intent.putExtra(CONNECTION_ROW_INDEX, getCCRowIdxFromPosition(id, daoSession));
		DatabaseUtil.close();
		startActivity(intent);
	}

	private long getCCRowIdxFromPosition(long id, DaoSession daoSession) {
		ConnectionConfigurationEntity conn_conf = (ConnectionConfigurationEntity) daoSession
		.getConnectionConfigurationEntityDao().loadAll().toArray()[(int) id];
		long connection_index = conn_conf.getId();
		return connection_index;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			ConnectionConfigurationEntity cc = (ConnectionConfigurationEntity) getListAdapter()
					.getItem(info.position);
			createCRUDConnectionDialog(cc.getLabel());
			crudConnectionDialog.show();
		} catch (ClassCastException e) {
			return;
		}
	}

	private void createCRConnectionDialog(CharSequence message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.create_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
//								makeToast("create connection");
								createNewConnection();
							}
						});
		builder.create().show();
	}

	private void createNewConnection() {
		startActivityForResult(new Intent(ConnectionListActivity.this,
				CRUDConnectionActivity.class), RQ_NEW_CONN);
	}

	private void modifyConnection(String message) {
		Intent intent = new Intent(ConnectionListActivity.this,
				CRUDConnectionActivity.class);
		DaoSession daoSession = DatabaseUtil.getReadOnlyDatabaseSession(this);
		ConnectionConfigurationEntityDao ccdao = daoSession
				.getConnectionConfigurationEntityDao();
		QueryBuilder<ConnectionConfigurationEntity> qb = ccdao.queryBuilder();
		Long ccid = qb.where(Properties.Label.eq(message)).build().list()
				.get(0).getId();
//		makeToast("intent extra " + ccid);
		intent.putExtra(CONNECTION_ROW_INDEX, ccid);
		DatabaseUtil.close();
		startActivityForResult(intent, RQ_MODIFY_CONN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void deleteConnection(String message) {
//		makeToast("Deleting " + message);
		DaoSession daoSession = DatabaseUtil.getWriteableDatabaseSession(this);
		ConnectionConfigurationEntityDao ccdao = daoSession
				.getConnectionConfigurationEntityDao();
		QueryBuilder<ConnectionConfigurationEntity> qb = ccdao.queryBuilder()
				.where(Properties.Label.eq(message)).limit(1);
		ccdao.delete(qb.list().get(0));
		DatabaseUtil.close();
	}

	private void createDialogDeleteConnection(final String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.sure_remove_conn) + message)
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
//								makeToast("yes, delete connection");
								deleteConnection(message);
								refreshList();
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
//								makeToast("no, preserve connection");
							}
						}).create().show();
	}

	private void createCRUDConnectionDialog(final String message) {
		// TODO set presence with dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
				.setCancelable(true)
				.setPositiveButton(R.string.create_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
//								makeToast("create connection");
								createNewConnection();
								// TODO - send Intent to service for reconnect,
								// (easiest way: restart service)
							}
						})
				.setNegativeButton(R.string.remove_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
//								makeToast("delete connection");
								createDialogDeleteConnection(message);
								// TODO - send Intent to service for reconnect,
								// (easiest way: restart service)
							}

						})
				.setNeutralButton(R.string.modify_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
//								makeToast("update connection");
								modifyConnection(message);
								// TODO - send Intent to service for reconnect,
								// (easiest way: restart service)
							}
						});
		crudConnectionDialog = builder.create();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO - remove stopService, connection has to persist?
		stopService(new Intent(ConnectionListActivity.this, XMPPService.class));
//		makeToast("onDestroy");

		// TODO - reconsider why XMPPService should not or also be killed
	};
}
