package nl.sison.xmpp;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao;
import nl.sison.xmpp.dao.ConnectionConfigurationEntityDao.Properties;
import nl.sison.xmpp.dao.DaoSession;
import android.app.Activity;
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
	private final String TAG = "CRUDConnectionActivity";
	
	private Long conn_config_id = (long) 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View list_view = LayoutInflater.from(this).inflate(
				R.layout.edit_connection, null, false);
		if (isCreateConnection()) {
			// TODO - show custom dialog with preset values, set hints in
			// TextView for each provider (google talk, facebook, meebo etc.)
		} else {
			// TODO load settings from database and show them
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
//			setTextViewData(list_view, R.id.conn_jid, cc.getJid());
			setTextViewData(list_view, R.id.conn_resource, cc.getResource());

			setToggleButtonState(list_view, R.id.conn_encrypted, cc.getEncrypted());
			setToggleButtonState(list_view, R.id.conn_compressed, cc.getCompressed());
			setToggleButtonState(list_view, R.id.conn_sasl_authenticated, cc.getSaslauthenticated());

			DatabaseUtil.close();
		}
		setButtons(list_view);
		setContentView(list_view);
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
	
	private void setToggleButtonState(View parentView, int button_id, boolean b)
	{
		ToggleButton tb = (ToggleButton) parentView
				.findViewById(button_id);
		tb.setChecked(b);		
	}

	private void setButtons(final View parent) {

		setButtonListener(parent, R.id.conn_compressed, new View.OnClickListener() {
			public void onClick(View v) {
				ToggleButton tb = (ToggleButton) v;
				if (tb.isChecked()) {
					makeToast("connection will be compressed");
				} else {
					makeToast("connection will NOT be compressed");
				}
			}
		});
		setButtonListener(parent, R.id.conn_sasl_authenticated, new View.OnClickListener() {
			public void onClick(View v) {
				ToggleButton tb = (ToggleButton) v;
				if (tb.isChecked()) {
					makeToast("connection will be sasl_authenticated");
				} else {
					makeToast("connection will NOT be sasl_authenticated");
				}
			}
		});		
		setButtonListener(parent, R.id.conn_encrypted, new View.OnClickListener() {
			public void onClick(View v) {
				ToggleButton tb = (ToggleButton) v;
				if (tb.isChecked()) {
					makeToast("connection will be encrypted");
				} else {
					makeToast("connection will NOT be encrypted");
				}
			}
		});
		
		setButtonListener(parent, R.id.conn_test, new View.OnClickListener() {
			public void onClick(View v) {
				makeToast("testing connection");
			}
		}); // TODO - merge test and save
		
		setButtonListener(parent, R.id.conn_save, new View.OnClickListener() {
			public void onClick(View v) {
				storeConnectionConfiguration(parent);
				// TODO try catch and do some error checking here, e.g. unique
				// labels
				// setResult(); // TODO determine which results (putExtra?)
				finish();
			}

		}); // TODO - merge test and save

	}

	private String extractStringFromTextView(final View parent, int view_id) {
		final TextView tv = (TextView) parent.findViewById(view_id);
		return tv.getText().toString();
	}

	private void storeConnectionConfiguration(View parent) {
		DaoSession daoSession = DatabaseUtil.getWriteableDatabaseSession(this);
		ConnectionConfigurationEntity conn_config = new ConnectionConfigurationEntity();
		ConnectionConfigurationEntityDao conn_config_dao = daoSession
				.getConnectionConfigurationEntityDao();
		
		if(conn_config_id != 0)
			conn_config.setId(conn_config_id);

		conn_config.setConnection_success(0);

		ToggleButton tb = (ToggleButton) parent
				.findViewById(R.id.conn_sasl_authenticated);
		conn_config.setSaslauthenticated(tb.isChecked());
		
		tb = (ToggleButton) parent
				.findViewById(R.id.conn_compressed);
		conn_config.setCompressed(tb.isChecked());
		
		tb = (ToggleButton) parent
				.findViewById(R.id.conn_encrypted);
		conn_config.setEncrypted(tb.isChecked());		

//		conn_config.setJid(extractStringFromTextView(parent, R.id.conn_jid));

		conn_config
				.setLabel(extractStringFromTextView(parent, R.id.conn_label));

		conn_config.setPassword(extractStringFromTextView(parent,
				R.id.conn_password)); // TODO encrypt using user's master
										// password
		
		conn_config.setDomain(extractStringFromTextView(parent, R.id.conn_domain));

		conn_config.setPort(extractStringFromTextView(parent, R.id.conn_port));

		conn_config.setResource(extractStringFromTextView(parent,
				R.id.conn_resource));

		conn_config.setServer(extractStringFromTextView(parent,
				R.id.conn_server));

		conn_config.setUsername(extractStringFromTextView(parent,
				R.id.conn_username));

		// TODO - set record id in class field
		conn_config_dao.insertOrReplace(conn_config);

		DatabaseUtil.close();

	}

	private boolean isCreateConnection() {
		String key_connection_index = ConnectionListActivity.KEY_CONNECTION_INDEX;
		return !getIntent().hasExtra(key_connection_index);
	}

	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
