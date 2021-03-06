package nl.sison.xmpp;

import java.util.Date;
import java.util.List;
import java.util.Random;

import de.greenrobot.dao.QueryBuilder;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntity;
import nl.sison.xmpp.dao.MessageEntityDao.Properties;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class XMPPNotificationService extends Service {

	public static final String TAG = "XMPPNotificationService";

	private NotificationManager notificationManager;

	// I don't need the dynamic binding here
	private ServiceReceiver message_receiver;
	private RemoveNotificationReceiver remove_notification_receiver;

	private Random random;

	public static final String KEY_BUDDY_INDEX = "438,.rc";
	public static final String THREAD = "e(abcdefghi*&";
	public static final String JID = "@$(&";

	class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			long message_id = intent.getLongExtra(
					XMPPService.KEY_MESSAGE_INDEX, 0);
			try {
				// NOTE: dirty hack to prevent two services competing for the
				// database
				Long sleepytime = 1000 + random.nextLong() % 1000;
				Thread.sleep(sleepytime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			createAndShowNotification(context, message_id);

		}

		private void createAndShowNotification(Context context, long message_id) {
			DaoSession daoSession = DatabaseUtils.getReadOnlySession(context);
			MessageEntity msg = daoSession
					.load(MessageEntity.class, message_id);

			BuddyEntity buddy = msg.getBuddyEntity();

			if (buddy.getIsActive() != null && buddy.getIsActive()) {
				/**
				 * Don't send a notification is the buddy is already active
				 */
				DatabaseUtils.close();
				return;
			}

			Long buddy_id = buddy.getId();
			ConnectionConfigurationEntity connection = buddy
					.getConnectionConfigurationEntity();
			String own_jid = connection.getUsername() + "@"
					+ connection.getDomain();
			String thread = msg.getThread();

			DatabaseUtils.close();

			Intent intent = new Intent(XMPPNotificationService.this,
					XMPPFragmentActivity.class);

			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			intent.putExtra(THREAD, thread);
			intent.putExtra(JID, own_jid);
			intent.putExtra(KEY_BUDDY_INDEX, buddy_id);

			PendingIntent pendintIntent = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			// do not set flags in the PendingIntent, set it in the payload
			// Intent

			StringBuilder str_builder = new StringBuilder();
			String notify_ticker = str_builder
					.append(getNicknameIfAvailable(buddy)).append(" ")
					.append(getString(R.string.says)).append(" \"")
					.append(msg.getContent()).append("\"").toString();
			// TODO truncate message if longer than ...

			// TODO change the -ing icon
			Notification.Builder builder = new Notification.Builder(context)
					.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true)
					.setTicker(notify_ticker).setContentText(msg.getContent())
					.setContentTitle(getNicknameIfAvailable(buddy))
					.setContentIntent(pendintIntent).setWhen(new Date().getTime());
			// TODO truncate msg.getContent if longer than...

			Notification notification = builder.getNotification();
			// notification.sound() // TODO create sounds on notification
			notificationManager.notify(safeLongToInt(message_id), notification);
		}

		private String getNicknameIfAvailable(BuddyEntity buddy) {
			String buddy_nickname = buddy.getNickname();
			if (buddy_nickname == null || buddy_nickname.isEmpty()) {
				return buddy.getPartial_jid();
			} else {
				return buddy_nickname;
			}
		}
	}

	/**
	 * I know this doesn't belong here. TODO - refactor elsewhere: e.g. static
	 * class TypeConversionUtils
	 * 
	 * @param l
	 * @return
	 */
	public static int safeLongToInt(final long l) {
		return (int) Math.min(Integer.MAX_VALUE, l);
	}

	class RemoveNotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				// NOTE: dirty hack to prevent two services competing for the
				// database
				Long sleepytime = 1000 + random.nextLong() % 1000;
				Thread.sleep(sleepytime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			long buddy_id = intent
					.getLongExtra(ChatFragment.KEY_BUDDY_INDEX, 0);
			DaoSession daoSession = DatabaseUtils.getReadOnlySession(context);

			QueryBuilder<MessageEntity> qb = daoSession
					.queryBuilder(MessageEntity.class);
			qb.where(Properties.BuddyId.eq(buddy_id))
					.orderDesc(Properties.Received_date).limit(10);
			// remove the last 10 messages
			// TODO - find a way to get the intersection of notification ids and
			// the message ids by a buddy

			List<MessageEntity> messages = qb.list();

			DatabaseUtils.close();

			for (MessageEntity msg : messages) {
				cancelNotification(safeLongToInt(msg.getId()));
			}
		}

		public void cancelNotification(int notifyId) {
			notificationManager.cancel(notifyId);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		random = new Random(1337331337);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		IntentFilter actionFilter1 = new IntentFilter();
		actionFilter1.addAction(XMPPService.ACTION_MESSAGE_INCOMING);
		message_receiver = new ServiceReceiver();
		registerReceiver(message_receiver, actionFilter1);

		IntentFilter actionFilter2 = new IntentFilter();
		remove_notification_receiver = new RemoveNotificationReceiver();
		actionFilter2
				.addAction(ChatFragment.ACTION_REQUEST_REMOVE_NOTIFICATIONS);
		registerReceiver(remove_notification_receiver, actionFilter2);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(message_receiver);
		unregisterReceiver(remove_notification_receiver);
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
