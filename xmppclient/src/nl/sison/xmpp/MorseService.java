package nl.sison.xmpp;

import java.util.ArrayList;
import java.util.Random;

import nl.sison.xmpp.dao.BuddyEntity;
import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class MorseService extends Service {

	// TODO - humanize rhythm of gaps?
	private long dot; // Length of a Morse Code "dot" in milliseconds
	private long dash; // Length of a Morse Code "dash" in milliseconds
	private long dotdash_gap; // Length of Gap Between dots/dashes
	private long letter_gap; // Length of Gap Between Letters
	private long word_gap; // Length of Gap Between Words
	private long message_pause; // Length of pause between messages, at least

	public static final String TAG = "MorseService";

	// I don't need the dynamic binding here
	private ServiceReceiver message_receiver;

	private Random random;

	private Vibrator vibrator;

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

			String message = getMessage(context, message_id);

			if (message.isEmpty())
				return;

			String[] msg_arr = message.split(" ");
			int[][][] raw_morse_message_pattern = new int[msg_arr.length][][];
			for (int word_position = 0; word_position < msg_arr.length; word_position++) {
				String word = msg_arr[word_position];
				int wordlen = word.length();
				int[][] raw_morse_word_pattern = new int[wordlen][];
				for (int letter_position = 0; letter_position < wordlen; letter_position++) {
					int[] morse_pattern = getResources().getIntArray(
							getResourceIdentifierByPrefix("morse_",
									word.charAt(letter_position))); // int[]
					raw_morse_word_pattern[letter_position] = morse_pattern;
				}
				raw_morse_message_pattern[word_position] = raw_morse_word_pattern;
			}

			ArrayList<Long> morse_pattern_list = new ArrayList<Long>();
			morse_pattern_list.add(message_pause); // wait between messages

			for (int[][] word : raw_morse_message_pattern) {
				for (int[] letter_pattern : word) {
					for (int dotOrDash : letter_pattern) {
						if (isDot(dotOrDash)) {
							morse_pattern_list.add(dot);
						} else {
							morse_pattern_list.add(dash);
						}
						morse_pattern_list.add(dotdash_gap);
					}
					morse_pattern_list.add(letter_gap);
				}
				morse_pattern_list.add(word_gap);
			}

			long[] complete_morse_pattern = new long[morse_pattern_list.size()];
			for (int i = 0; i < morse_pattern_list.size(); i++) {
				complete_morse_pattern[i] = morse_pattern_list.get(i);
				// NxN time, because of the list, but first rule: KISS: make it
				// work first. // TODO - optimize memory consumption
			}

			if (!vibrator.hasVibrator())
				return;
			vibrator.vibrate(complete_morse_pattern, -1); // once and
			// once only
		}

		private boolean isDot(int dotOrDash) {
			return dotOrDash == 0;
		}

		private int getResourceIdentifierByPrefix(String prefix, char value) {
			// makeToast(prefix + value);

			int res_id = translateSymbols(value);
			if (res_id != -1) {
				return res_id;
			}

			try {
				return getResources().getIdentifier(prefix + value, "array",
						"nl.sison.xmpp");
			} catch (NotFoundException ex) {
				ex.printStackTrace();
				return R.array.morse_period; // if you can't find the symbol
												// just put a period there.

			}
		}

		private int translateSymbols(char value) {

			if (value == '.') {
				return R.array.morse_period;
			} else if (value == '@') {
				return R.array.morse_at;
			} else if (value == '.') {
				return R.array.morse_period;
			} else if (value == ',') {
				return R.array.morse_comma;
			} else if (value == '?') {
				return R.array.morse_question_mark;
			} else if (value == '\'') {
				return R.array.morse_hyphen;
			} else if (value == '!') {
				return R.array.morse_exclamation_mark;
			} else if (value == '\\') {
				return R.array.morse_slash;
			} else if (value == '-') {
				return R.array.morse_hyphen;
			} else if (value == '/') {
				return R.array.morse_fraction_bar;
			} else if (value == ')' || value == '(') {
				return R.array.morse_parentheses;
			} else if (value == '"') {
				return R.array.morse_quotation_mark;
			}
			return -1;
		}

		private String getMessage(Context context, long message_id) {
			DaoSession daoSession = DatabaseUtils
					.getReadOnlyDatabaseSession(context);
			MessageEntity msg = daoSession.getMessageEntityDao().load(
					message_id);

			BuddyEntity buddy = msg.getBuddyEntity();

			if (buddy.getConnectionConfigurationEntity().getVibrate() == null)
				return "";
			boolean vibrate = buddy.getConnectionConfigurationEntity()
					.getVibrate();
			if (!vibrate)
				return "";

			DatabaseUtils.close();

			StringBuilder str_builder = new StringBuilder();
			return str_builder.append(getNicknameIfAvailable(buddy))
					.append(" ").append(getString(R.string.says)).append(" \"")
					.append(msg.getContent()).append("\"").toString();
			// TODO truncate if longer than ...

		}

		private String getNicknameIfAvailable(BuddyEntity buddy) {
			String buddy_nickname = buddy.getNickname();
			if (buddy_nickname != null && !buddy_nickname.isEmpty()) {
				return buddy_nickname;
			} else {
				return buddy.getPartial_jid();
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		random = new Random(1337331335);
		int[] arr = getResources().getIntArray(R.array.morse_settings);
		dot = arr[0];// = 200; // Length of a Morse Code "dot" in milliseconds
		dash = arr[1];// = 500; // Length of a Morse Code "dash" in milliseconds
		dotdash_gap = arr[2];// = 200; // Length of Gap Between dots/dashes
		letter_gap = arr[3];// = 500; // Length of Gap Between Letters
		word_gap = arr[4];// = 1000; // Length of Gap Between Words
		message_pause = arr[5];

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		IntentFilter actionFilter = new IntentFilter();
		actionFilter.addAction(XMPPService.ACTION_MESSAGE_INCOMING);
		message_receiver = new ServiceReceiver();
		registerReceiver(message_receiver, actionFilter);
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
