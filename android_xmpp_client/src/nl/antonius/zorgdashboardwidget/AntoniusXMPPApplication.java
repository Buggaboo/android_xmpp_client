package nl.antonius.zorgdashboardwidget;

import android.app.Application;

public class AntoniusXMPPApplication extends Application {
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		ConfigureProviderManager.configureProviderManager();
	}

}
