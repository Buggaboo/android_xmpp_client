package nl.sison.xmpp;

import android.content.Intent;

public interface FragmentLoader {
	/**
	 * 
	 * This interface should be implemented by all FragmentLoaders (which are
	 * fragment themselves) The NullPointerException is thrown if an Intent to a
	 * Fragment is not defined
	 * 
	 * @param intent
	 * @throws NullPointerException
	 */
	public void loadFragment(Intent intent) throws NullPointerException;
	
	/**
	 * 
	 * Depending on layout type (e.g. single, double, triple) panels
	 * The swipe goes to the previous state?
	 * 
	 * @param intent
	 * @throws NullPointerException
	 */
	public void swipeToFragment(Intent intent) throws NullPointerException;

}
