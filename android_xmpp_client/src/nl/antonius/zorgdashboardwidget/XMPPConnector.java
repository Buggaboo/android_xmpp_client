package nl.antonius.zorgdashboardwidget;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Presence;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class XMPPConnector
{
	private final static String TAG = "XMPPConnector";
	private static XMPPConnection connection;
	
	
	private static void addConnectionListener(XMPPConnection connection)
	{
			ConnectionListener conn_listener = new ConnectionListener()
			{
				public void connectionClosed()
				{
					Log.i(TAG,"Connection closed.");
				}

				public void connectionClosedOnError(Exception ex)
				{
					Log.i(TAG,"Connection closed on error.");
					ex.printStackTrace();
				}

				public void reconnectingIn(int i)
				{
					Log.i(TAG,"Reconnecting in " + i);
				}

				public void reconnectionFailed(Exception ex)
				{
					Log.i(TAG,"Reconnecting failed.");
				}

				public void reconnectionSuccessful()
				{
					Log.i(TAG,"Reconnected.");
				}
			};
		connection.addConnectionListener(conn_listener);
	}

	public static boolean isConnected()
	{
		return connection != null && connection.isConnected();
	}

	public static boolean isAuthenticated()
	{
		return connection != null && connection.isAuthenticated();
	}

	public static void connect(String server, String username, String password,
			String resource)
	{
		if (isConnected() && isAuthenticated())
		{
			return;
		}

		ConnectionConfiguration cc = new ConnectionConfiguration(server, 5222);
		connection = new XMPPConnection(cc);
		try
		{
			connection.connect();
			connection.login(username, password, resource);
			addConnectionListener(connection);
		} catch (XMPPException e)
		{
			connection = null;
			e.printStackTrace();
		}

		// SASLAuthentication.supportSASLMechanism("PLAIN", 0); // TODO -
		// required?
	}

	public static void disconnect()
	{
		if (connection == null)
			return;
		connection.disconnect();
		Log.i(TAG, "Disconnected from XMPP server.");
		connection = null;
	}

	static ChatManager getChatManager()
	{
		return connection.getChatManager();
	}

	static AccountManager getAccountManager()
	{
		return connection.getAccountManager();
	}

	public static Roster getRoster()
	{
		return connection.getRoster();
	}
	
	private static RosterEntry getEntry(int buddy_index)
	{
		return (RosterEntry) getRoster().getEntries().toArray()[buddy_index];
	}
	
	public static Presence getPresence(int buddy_index)
	{
		RosterEntry entry = getEntry(buddy_index);
		return getRoster().getPresence(entry.getUser());		
	}
	
	public static String getFullJID(int buddy_index)
	{
		RosterEntry entry = getEntry(buddy_index);
		return entry.getUser();
	}
	
	public static void addPacketListener(PacketListener pl, PacketFilter pf)
	{
		connection.addPacketListener(pl, pf);
	}
	
	/**
	 * Checks if the device has Internet connection.
	 * 
	 * @return <code>true</code> if the phone is connected to the Internet.
	 */
	static public boolean hasNoConnectivity(Application application)
	{
		// TODO - refactor to XMPPConnector
		ConnectivityManager cm = (ConnectivityManager) application
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected())
		{
			 Log.i(TAG,"Detected wireless network.");
			
			return false;
		}

		NetworkInfo mobileNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected())
		{
			 Log.i(TAG,"Detected mobile network.");
			return false;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected())
		{
			 Log.i(TAG,"Detected network.");
			return false;
		}

		return true;
	}	

}
