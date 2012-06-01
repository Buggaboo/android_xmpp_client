package nl.sison.xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * 
 * @author Jasm Sison
 *
 */
public class StartXMPPServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		Intent xmpp_service = new Intent(context, XMPPService.class);
		context.startService(xmpp_service);
	}

}
