package nl.sison.xmpp;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.DaoSession;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

/**
 * TODO rename this class to XMPPActivity
 */

/**
 * TODO design: to determine screen size etc. 
 * i.e. startActivityForResult(new Intent(this, ScreenDeterminator.class))
 */

/** 
 * TODO design: use a behavioural pattern (strategy), to control the Fragments  
 */

/**
 * @author Jasm Sison
 */
public class XMPPFragmentActivity extends FragmentActivity implements FragmentLoader {
	private static final String TAG = "XMPPFragmentActivity";
	private OnBackStackChangedListener backStackChangedListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO refactor setContentView away in a strategy
		setContentView(R.layout.tabbed_single_fragment_layout);

		// just in case
		startService(new Intent(this, XMPPService.class));

		loadFragment(new Intent(this, ConnectionListFragment.class));
		loadBuddyIfCorrectIntent(getIntent());
		loadChatIfCorrectIntent(getIntent());

		setFragmentBackStackListeners(); // don't accidentally kill the
											// application
	}

	private void setFragmentBackStackListeners() {
		backStackChangedListener = new OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				int count = getSupportFragmentManager().getBackStackEntryCount();
				if (count == 0)
					finish();
			}
		};
		getSupportFragmentManager().addOnBackStackChangedListener(
				backStackChangedListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getSupportFragmentManager().removeOnBackStackChangedListener(
				backStackChangedListener);
	}

	private void loadBuddyIfCorrectIntent(Intent intent) {
		String key_buddy_index = XMPPNotificationService.KEY_BUDDY_INDEX;
		if (intent != null && intent.hasExtra(key_buddy_index)) {
			DaoSession session = DatabaseUtils.getReadOnlySession(this);
			BuddyEntity buddy = session.getBuddyEntityDao().load(
					intent.getLongExtra(key_buddy_index, 0));
			Intent fragmentIntent = new Intent(this, BuddyListFragment.class);
			fragmentIntent.putExtra(
					ConnectionListFragment.KEY_CONNECTION_INDEX,
					buddy.getConnectionId());
			loadFragment(fragmentIntent);
		}
	}

	/**
	 * This function should react to calls from Notification to start a new chat
	 * screen
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// TODO - determine if issue is relevant:
		// http://code.google.com/p/android/issues/detail?id=17137
		loadChatIfCorrectIntent(intent);
	}

	private void loadChatIfCorrectIntent(Intent _intent) {
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
			// for different layouts
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO in the class to be called to calculate the screen size
		// (ScreenDeterminator)orientation etc., call
		// ScreenDeterminator.setResult to push back an int to this function
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
			fragment = (Fragment) Class.forName(className).newInstance(); // Reflection
			fragment.setArguments(intent.getExtras());
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction().replace(R.id.single_panel_1, fragment);
			transaction.addToBackStack(null);

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
