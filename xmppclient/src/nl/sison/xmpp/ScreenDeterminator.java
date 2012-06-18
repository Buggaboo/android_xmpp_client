package nl.sison.xmpp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ScreenDeterminator extends Activity {

	private static final String TAG = "ScreenDeterminator";

	/**
	 * if (getResources().getConfiguration().orientation ==
	 * Configuration.ORIENTATION_LANDSCAPE) { // If the screen is now in
	 * landscape mode, we can show the // dialog in-line with the list so we
	 * don't need this activity. finish(); return; }
	 */

	/**
	 * TODO - use this to detect the screen size WindowManager wm =
	 * (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE); Display
	 * display = wm.getDefaultDisplay();
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// screen size detection // TODO - use this info to deploy fragments
		int layout_size = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
		if (layout_size == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			makeToast("SCREENLAYOUT_SIZE_SMALL");
		} else if (layout_size == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			makeToast("SCREENLAYOUT_SIZE_NORMAL");
		} else if (layout_size == Configuration.SCREENLAYOUT_SIZE_LARGE) {
			makeToast("SCREENLAYOUT_SIZE_LARGE");
		} else if (layout_size == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			makeToast("SCREENLAYOUT_SIZE_XLARGE");
		} else if (layout_size == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
			makeToast("SCREENLAYOUT_SIZE_UNDEFINED");
		}

		/*
		 * // TODO - test and implement if small screen etc. otherwise, //
		 * DoublePanel... TriplePanel, combine the screen size detection // TODO
		 * - determine a faster way to pass on the extras
		 */

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
