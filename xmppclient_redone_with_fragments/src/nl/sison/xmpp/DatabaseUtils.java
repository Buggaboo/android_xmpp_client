package nl.sison.xmpp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import nl.sison.xmpp.dao.BuddyEntityDao;
import nl.sison.xmpp.dao.DaoMaster;
import nl.sison.xmpp.dao.DaoMaster.DevOpenHelper;
import nl.sison.xmpp.dao.DaoSession;
import nl.sison.xmpp.dao.MessageEntityDao;

/**
 * 
 * @author Jasm Sison
 * 
 */
public class DatabaseUtils {
	public static final String XMPP_CLIENT_DATABASE = "xmpp_client_database.db";
	private static DevOpenHelper helper;

	static public DaoSession getWriteableSession(Context context) {
		if (helper == null)
			helper = new DevOpenHelper(context, XMPP_CLIENT_DATABASE, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}

	static public DaoSession getReadOnlySession(Context context) {
		if (helper == null)
			helper = new DevOpenHelper(context, XMPP_CLIENT_DATABASE, null);
		SQLiteDatabase db = helper.getReadableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}
	
	static public void destroyDatabase(Context context)
	{
		BuddyEntityDao.dropTable(helper.getWritableDatabase(), true);
		MessageEntityDao.dropTable(helper.getWritableDatabase(), true);
		close();
	}

	static public void close() {
		if (helper != null) {
			helper.close();
		}
	}
	
	/**
	 * I know this doesn't belong here. TODO - refactor elsewhere: e.g. static class TypeConversionUtils
	 * @param l
	 * @return
	 */
	public static int safeLongToInt(final long l) {
	    return (int) Math.min(Integer.MAX_VALUE, l);
	}	
}
