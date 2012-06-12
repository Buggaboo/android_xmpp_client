package nl.antonius.zorgdashboardwidget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class XMPPService extends Service {
	
	private int NOTIFICATION = R.string.are_you_sure_you_want_to_exit;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}