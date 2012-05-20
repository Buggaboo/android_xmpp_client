package nl.sison.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CRUDConnectionActivity extends Activity {
	public static final String RESTART_CONNECTION = "asnehnaoseuthaoseuthaoseuth2234";

	private final String TAG = "CRUDConnectionActivity";

	private Long conn_config_id = (long) 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View list_view = LayoutInflater.from(this).inflate(
				R.layout.edit_connection, null, false);
		if (isExtantConnection()) {
			showValuesFromDatabase(list_view);
		}
		setButtons(list_view);
		setContentView(list_view);
	}

	private void showValuesFromDatabase(View list_view) {
		long ccid = getIntent().getExtras().getLong(
				ConnectionListActivity.KEY_CONNECTION_INDEX);

		makeToast("intent extra " + ccid);

		ConnectionConfigurationEntity cc = DatabaseUtil
				.getReadOnlyDatabaseSession(this)
				.getConnectionConfigurationEntityDao().queryBuilder()
				.where(Properties.Id.eq(ccid)).list().get(0);

		conn_config_id = cc.getId();

		setTextViewData(list_view, R.id.conn_label, cc.getLabel());
		setTextViewData(list_view, R.id.conn_port, cc.getPort());
		setTextViewData(list_view, R.id.conn_server, cc.getServer());
		setTextViewData(list_view, R.id.conn_username, cc.getUsername());
		setTextViewData(list_view, R.id.conn_password, cc.getPassword());
		setTextViewData(list_view, R.id.conn_domain, cc.getDomain());
		setTextViewData(list_view, R.id.conn_resource, cc.getResource());

		setToggleButtonState(list_view, R.id.conn_encrypted, cc.getEncrypted());
		setToggleButtonState(list_view, R.id.conn_compressed,
				cc.getCompressed());
		setToggleButtonState(list_view, R.id.conn_sasl_authenticated,
				cc.getSaslauthenticated());

		DatabaseUtil.close();
	}

	private void setTextViewData(View parent, int view_id, String value) {
		TextView tv = (TextView) parent.findViewById(view_id);
		tv.setText(value);
	}

	private void setButtonListener(View parentView, int button_id,
			View.OnClickListener listener) {
		final Button button = (Button) parentView.findViewById(button_id);
		button.setOnClickListener(listener);
	}

	private void setToggleButtonState(View parentView, int button_id, boolean b) {
		ToggleButton tb = (ToggleButton) parentView.findViewById(button_id);
		tb.setChecked(b);
	}

	private void setButtons(final View parent) {

		setButtonListener(parent, R.id.conn_compressed,
				new View.OnClickListener() {
					public void onClick(View v) {
						ToggleButton tb = (ToggleButton) v;
						if (tb.isChecked()) {
							makeToast("connection will be compressed");
						} else {
							makeToast("connection will NOT be compressed");
						}
					}
				});
		setButtonListener(parent, R.id.conn_sasl_authenticated,
				new View.OnClickListener() {
					public void onClick(View v) {
						ToggleButton tb = (ToggleButton) v;
						if (tb.isChecked()) {
							makeToast("connection will be sasl_authenticated");
						} else {
							makeToast("connection will NOT be sasl_authenticated");
						}
					}
				});
		setButtonListener(parent, R.id.conn_encrypted,
				new View.OnClickListener() {
					public void onClick(View v) {
						ToggleButton tb = (ToggleButton) v;
						if (tb.isChecked()) {
							makeToast("connection will be encrypted");
							makeToast("Warning: encryption on this version of smack lib is most likely broken.");
						} else {
							makeToast("connection will NOT be encrypted");
						}
					}
				});

		setButtonListener(parent, R.id.conn_test_and_save,
				new View.OnClickListener() {
					public void onClick(View v) {
						ConnectionConfigurationEntity conn_conf = getConnectionDetails(parent);
						XMPPConnection connection = connectToServer(conn_conf);
						if (connection != null && connection.isConnected()
								&& connection.isAuthenticated()) {
							storeConnectionConfiguration(conn_conf);
							Intent restartConnectionOnService = new Intent(
									CRUDConnectionActivity.this,
									XMPPService.class);
							restartConnectionOnService.putExtra(
									RESTART_CONNECTION, conn_config_id);
							startService(restartConnectionOnService);
							finish();
						} else {
							createWarningConnectionBadDialog(getString(R.string.conn_bad_conn_conf));
						}

					}

				});
	}

	private void createWarningConnectionBadDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.create_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								View parent = LayoutInflater.from(
										CRUDConnectionActivity.this).inflate(
										R.layout.edit_connection, null, false);
								ConnectionConfigurationEntity conn_conf = getConnectionDetails(parent);
								storeConnectionConfiguration(conn_conf);
								makeToast("stored bad configuration");
								finish();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								makeToast("cancel store bad configuration");
							}
						});
		builder.create().show();
	}

	private XMPPConnection connectToServer(ConnectionConfigurationEntity cc) {
		ConnectionConfiguration xmpp_conn_config = new ConnectionConfiguration(
				cc.getServer(), Integer.valueOf(cc.getPort()), cc.getDomain());
		xmpp_conn_config.setCompressionEnabled(cc.getCompressed());
		xmpp_conn_config
				.setSASLAuthenticationEnabled(cc.getSaslauthenticated());
		// xmpp_conn_config.setReconnectionAllowed(true); // TODO
		XMPPConnection connection = new XMPPConnection(xmpp_conn_config);
		try {
			connection.connect();
			makeToast(cc.getLabel() + "is connected:"
					+ connection.isConnected());
			connection.login(cc.getUsername(), cc.getPassword(),
					cc.getResource());
			makeToast(cc.getLabel() + "is authenticated:"
					+ connection.isAuthenticated());
		} catch (XMPPException e) {
			connection = null;
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
		return connection;
	}

	private String extractStringFromTextView(final View parent, int view_id) {
		final TextView tv = (TextView) parent.findViewById(view_id);
		return tv.getText().toString();
	}

	private void storeConnectionConfiguration(
			ConnectionConfigurationEntity conn_config) {
		DaoSession daoSession = DatabaseUtil.getWriteableDatabaseSession(this);
		ConnectionConfigurationEntityDao conn_config_dao = daoSession
				.getConnectionConfigurationEntityDao();
		conn_config_dao.insertOrReplace(conn_config);
		DatabaseUtil.close();
	}

	private ConnectionConfigurationEntity getConnectionDetails(View parent) {
		ConnectionConfigurationEntity conn_config = new ConnectionConfigurationEntity();

		if (conn_config_id != 0)
			conn_config.setId(conn_config_id);

		conn_config.setConnection_success(0);

		ToggleButton tb = (ToggleButton) parent
				.findViewById(R.id.conn_sasl_authenticated);
		conn_config.setSaslauthenticated(tb.isChecked());

		tb = (ToggleButton) parent.findViewById(R.id.conn_compressed);
		conn_config.setCompressed(tb.isChecked());

		tb = (ToggleButton) parent.findViewById(R.id.conn_encrypted);
		conn_config.setEncrypted(tb.isChecked());

		conn_config
				.setLabel(extractStringFromTextView(parent, R.id.conn_label));

		conn_config.setPassword(extractStringFromTextView(parent,
				R.id.conn_password)); // TODO encrypt using user's master
										// password

		conn_config.setDomain(extractStringFromTextView(parent,
				R.id.conn_domain));

		conn_config.setPort(extractStringFromTextView(parent, R.id.conn_port));

		conn_config.setResource(extractStringFromTextView(parent,
				R.id.conn_resource));

		conn_config.setServer(extractStringFromTextView(parent,
				R.id.conn_server));

		conn_config.setUsername(extractStringFromTextView(parent,
				R.id.conn_username));
		return conn_config;
	}

	private boolean isExtantConnection() {
		String key_connection_index = ConnectionListActivity.KEY_CONNECTION_INDEX;
		return getIntent().hasExtra(key_connection_index);
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
