package nl.sison.xmpp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import nl.sison.xmpp.dao.DaoMaster;
import nl.sison.xmpp.dao.DaoMaster.DevOpenHelper;
import nl.sison.xmpp.dao.DaoSession;

/**
 * 
 * @author Jasm Sison
 * 
 */
public class DatabaseUtils {
	public static final String XMPP_CLIENT_DATABASE = "xmpp_client_database.db";
	private static DevOpenHelper helper;

	static public DaoSession getWriteableDatabaseSession(Context context) {
		if (helper == null)
			helper = new DevOpenHelper(context, XMPP_CLIENT_DATABASE, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}

	static public DaoSession getReadOnlyDatabaseSession(Context context) {
		if (helper == null)
			helper = new DevOpenHelper(context, XMPP_CLIENT_DATABASE, null);
		SQLiteDatabase db = helper.getReadableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}

	static public void close() {
		if (helper != null) {
			helper.close();
		}
	}
	
	/**
	 * I know this doesn't belong here. TODO - refactor elsewhere
	 * @param l
	 * @return
	 */
	public static int safeLongToInt(final long l) {
	    return (int) Math.min(Integer.MAX_VALUE, l);
	}	
}
