package nl.sison.xmpp;

import android.content.Intent;

public interface FragmentLoader {
	/**
	 * 
	 * This interface should be implemented by all FragmentLoader (FragmentActivity)
	 * 
	 * @param intent
	 * @throws NullPointerException
	 */
	public void loadFragment(Intent intent);
}
