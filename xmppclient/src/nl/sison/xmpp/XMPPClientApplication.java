package nl.sison.xmpp;

import android.app.Application;

public class XMPPClientApplication extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		ConfigureProviderManager.configureProviderManager(); // TODO figure out why this is absolutely (not) necessary
	}
}
