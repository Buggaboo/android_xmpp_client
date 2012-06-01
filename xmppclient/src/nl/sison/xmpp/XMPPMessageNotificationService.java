package nl.sison.xmpp;

import java.util.Date;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class XMPPMessageNotificationService extends Service {

	private static final int SERVICE_ID = 0;
	private NotificationManager notificationManager;
	private ServiceReceiver receiver;

	class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			long message_id = intent.getLongExtra(
					XMPPService.KEY_MESSAGE_INDEX, 0);
			createAndShowNotification(message_id);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		IntentFilter actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_INCOMING);
		receiver = new ServiceReceiver();
		registerReceiver(receiver, actionFilter);
		// createAndShowNotification(); // test // TODO this should be in the
		// ServiceReceiver
	}

	private void createAndShowNotification(long message_id) {
		DaoSession daoSession = DatabaseUtils.getReadOnlyDatabaseSession(this);
		MessageEntity msg = daoSession.getMessageEntityDao().load(message_id);
		DatabaseUtils.close();

		BuddyEntity buddy = msg.getBuddyEntity();
		ConnectionConfigurationEntity connection = buddy.getConnectionConfigurationEntity();
		String own_jid = connection.getUsername() + "@" + connection.getDomain();
		String thread = msg.getThread();
		
		// TODO synchronize bundle keys across activities

		Intent intent = new Intent(XMPPMessageNotificationService.this,
				ChatActivity.class);
		PendingIntent p_intent = PendingIntent.getActivity(this, 0, intent,
				BIND_AUTO_CREATE);
		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setTicker(
						buddy.getPartial_jid() + " "
								+ getString(R.string.notify_new_msg))
				.setContentText(msg.getContent()).setContentIntent(p_intent)
				.setWhen(new Date().getTime());

		Notification notification = builder.getNotification();
		// notification.sound() // TODO
		// notification.vibrate() // TODO - morse code
		notificationManager.notify((int) message_id, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

}
