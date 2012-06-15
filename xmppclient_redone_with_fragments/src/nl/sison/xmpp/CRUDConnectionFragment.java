package nl.sison.xmpp;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao;
import nl.sison.xmpp.dao.DaoSession;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * 
 * @author Jasm Sison
 * 
 */

public class CRUDConnectionFragment extends Fragment {
	/**
	 * TODO set default value for resource
	 */

	public static final String RESTART_CONNECTION = "asnehnaoseu";

	public static final String ACTION_REQUEST_POPULATE_BUDDYLIST = "euthaose1!@##$";

	public static final String KEY_CONNECTION_INDEX = "4433&&*";

	private final String TAG = "CRUDConnectionFragment";

	private Long conn_config_id = (long) 0; // default value: isExtantConnection
											// fails if default

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View list_view = inflater.inflate(
				R.layout.edit_connection_layout, null, false);
		if (isExtantConnection()) {
			showValuesFromDatabase(list_view);
		} else {
			createHintPrefixDialog(list_view).show();
		}
		setButtons(list_view);
		return list_view;		
	}

	private AlertDialog createHintPrefixDialog(final View list_view) {
		final String[] prefix_items = getResources().getStringArray(
				R.array.hint_prefixes);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.conn_pick_a_provider));
		builder.setItems(prefix_items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int prefix_index) {
				setHints(list_view, prefix_items[prefix_index]);
			}
		});
		return builder.create();
	}

	private void setHints(View parent, String hint_prefix) {
		if (!hint_prefix.endsWith("_"))
			hint_prefix += "_"; // TODO move this one level higher

		int id_hint_label = getResourceIdentifierByPrefix(hint_prefix,
				"hint_label");
		setTextViewHint(parent, R.id.conn_label, id_hint_label).setText(
				id_hint_label);

		int id_hint_server = getResourceIdentifierByPrefix(hint_prefix,
				"hint_server");
		setTextViewHint(parent, R.id.conn_server, id_hint_server).setText(
				id_hint_server);

		int id_hint_domain = getResourceIdentifierByPrefix(hint_prefix,
				"hint_domain");
		setTextViewHint(parent, R.id.conn_domain, id_hint_domain).setText(
				id_hint_domain);

		int id_hint_port = getResourceIdentifierByPrefix(hint_prefix,
				"hint_port");
		setTextViewHint(parent, R.id.conn_port, id_hint_port).setText(
				id_hint_port);

		setTextViewHint(parent, R.id.conn_username,
				getResourceIdentifierByPrefix(hint_prefix, "hint_username"));

		setTextViewHint(parent, R.id.conn_resource,
				getResourceIdentifierByPrefix(hint_prefix, "hint_resource"));

		// TODO fix & test toggle boolean
		setToggleButtonDefault(parent, R.id.conn_compressed,
				Boolean.valueOf(getString(getResourceIdentifierByPrefix(
						hint_prefix, "default_compressed"))));
		setToggleButtonDefault(parent, R.id.conn_encrypted,
				Boolean.valueOf(getString(getResourceIdentifierByPrefix(
						hint_prefix, "default_encrypted"))));
		setToggleButtonDefault(parent, R.id.conn_sasl_authenticated,
				Boolean.valueOf(getString(getResourceIdentifierByPrefix(
						hint_prefix, "default_sasl_authenticated"))));
	}

	private TextView setTextViewHint(View parent, int view_id, int res_id) {
		TextView tv = (TextView) parent.findViewById(view_id);
		tv.setHint(res_id);
		return tv;
	}

	private void setToggleButtonDefault(View parent, int view_id, boolean state) {
		ToggleButton tb = (ToggleButton) parent.findViewById(view_id);
		tb.setChecked(state);
	}

	private int getResourceIdentifierByPrefix(String prefix, String value) {
		return getResources().getIdentifier(prefix + value, "string",
				"nl.sison.xmpp");
	}

	private void showValuesFromDatabase(View list_view) {
		long ccid = getArguments().getLong(
				ConnectionListFragment.KEY_CONNECTION_INDEX);

		conn_config_id = ccid;

		ConnectionConfigurationEntity cc = DatabaseUtils
				.getReadOnlySession(getActivity()).load(
						ConnectionConfigurationEntity.class, ccid);

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

		DatabaseUtils.close();
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
							// makeToast("connection will be compressed");
						} else {
							// makeToast("connection will NOT be compressed");
						}
					}
				});
		setButtonListener(parent, R.id.conn_sasl_authenticated,
				new View.OnClickListener() {
					public void onClick(View v) {
						ToggleButton tb = (ToggleButton) v;
						if (tb.isChecked()) {
							// makeToast("connection will be sasl_authenticated");
						} else {
							// makeToast("connection will NOT be sasl_authenticated");
						}
					}
				});
		setButtonListener(parent, R.id.conn_encrypted,
				new View.OnClickListener() {
					public void onClick(View v) {
						ToggleButton tb = (ToggleButton) v;
						if (tb.isChecked()) {
							// makeToast("connection will be encrypted");
							makeToast("Warning: encryption on this version of smack lib is most likely broken.");
						} else {
							// makeToast("connection will NOT be encrypted");
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
							long cc_id = storeConnectionConfiguration(conn_conf);
							DatabaseUtils.close();
							Intent restartConnectionOnService = new Intent(
									getActivity(),
									XMPPService.class);
							restartConnectionOnService.putExtra(
									RESTART_CONNECTION, conn_config_id);
							connection.disconnect();
							// tell the service to connect with this new
							// connection
							Intent intent = new Intent(
									ACTION_REQUEST_POPULATE_BUDDYLIST);
							intent.putExtra(KEY_CONNECTION_INDEX, cc_id);
							getActivity().sendBroadcast(intent);
							getFragmentManager().popBackStack();
						} else {
							createWarningConnectionBadDialog(getString(R.string.conn_bad_conn_conf));
						}
					}
				});
	}

	private void createWarningConnectionBadDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.create_connection,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								View parent = LayoutInflater
										.from(getActivity())
										.inflate(
												R.layout.edit_connection_layout,
												null, false);
								ConnectionConfigurationEntity conn_conf = getConnectionDetails(parent);
								storeConnectionConfiguration(conn_conf);
								getFragmentManager().popBackStack();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// makeToast("cancel store bad configuration");
							}
						});
		builder.create().show();
	}

	/**
	 * 
	 * This code is copy-pasted from the service code, baaaaad. But it works
	 * purrrrrrfectly.
	 * 
	 * @param cc
	 * @return
	 */
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
			// makeToast(cc.getLabel() + "is connected:"
			// + connection.isConnected());
			connection.login(cc.getUsername(), cc.getPassword(),
					cc.getResource());
			// makeToast(cc.getLabel() + "is authenticated:"
			// + connection.isAuthenticated());
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

	private long storeConnectionConfiguration(
			ConnectionConfigurationEntity conn_config) {
		DaoSession daoSession = DatabaseUtils
				.getWriteableSession(getActivity()
						.getApplicationContext());
		ConnectionConfigurationEntityDao conn_config_dao = daoSession
				.getConnectionConfigurationEntityDao();
		return conn_config_dao.insertOrReplace(conn_config);
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
		String connection_index = ConnectionListFragment.KEY_CONNECTION_INDEX;
		if (getArguments() != null
				&& getArguments().containsKey(connection_index))
			return getArguments().getLong(connection_index) > 0;
		return false;
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast
				.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
