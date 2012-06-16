package nl.sison.xmpp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * TODO rename this class to XMPPActivity
 */

/**
 * TODO design: to determine screen size etc. i.e. startActivityForResult
 */

/** 
 * TODO design: use a behavioural pattern (strategy), to control the Fragments  
 */

/**
 * @author Jasm Sison
 */
public class XMPPFragmentActivity extends Activity implements FragmentLoader {
	private static final String TAG = "XMPPFragmentActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_single_fragment_layout);

		// just in case
		startService(new Intent(this, XMPPService.class));

		// TODO refactor setContentView away in a strategy
		Intent fragmentIntent = new Intent(this, ConnectionListFragment.class);
		loadFragment(fragmentIntent);
	}

	/**
	 * This function should react to calls from Notification to start a new chat
	 * screen
	 */
	@Override
	protected void onNewIntent(Intent _intent) {
		super.onNewIntent(_intent);
		// TODO - determine if issue is relevant:
		// http://code.google.com/p/android/issues/detail?id=17137
		makeToast("onNewIntent");
		Intent fragmentIntent;
		String key_buddy_index = XMPPNotificationService.KEY_BUDDY_INDEX;
		if (_intent != null && _intent.hasExtra(key_buddy_index)) {
			String key_own_jid = XMPPNotificationService.JID;
			fragmentIntent = new Intent(this, ChatFragment.class);
			fragmentIntent.putExtra(key_buddy_index,
					_intent.getLongExtra(key_buddy_index, 0));
			fragmentIntent.putExtra(key_own_jid,
					_intent.getStringExtra(key_own_jid));
			loadFragment(fragmentIntent);
			// TODO implement strategy for loadFragment
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO in the class to be called, call setResult to push back an int to
		// this function
	}

	/**
	 * 
	 * After clicking on an ListItem. Load a new fragment and put previous
	 * fragment on the backstack
	 * 
	 * @param intent
	 */
	@Override
	public void loadFragment(Intent intent) {
		String className;
		className = intent.getComponent().getClassName();
		Fragment fragment;
		try {
			makeToast("loading fragment: " + className);
			fragment = (Fragment) Class.forName(className).newInstance(); // Reflection
			fragment.setArguments(intent.getExtras());
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction().replace(R.id.single_panel_1, fragment);
			transaction.addToBackStack("identifier"); // TODO - use the same
														// "identifier" for the
														// tab?
			transaction.commit();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} // new java 7: catch (Exception1 e1 | Exception2 e2 | Exception e3 ...

	}

	@Deprecated
	private void makeToast(String message) {
		if (!BuildConfig.DEBUG)
			return;
		Log.i(TAG, message);
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
