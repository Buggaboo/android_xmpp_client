package nl.sison.xmpp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import nl.sison.xmpp.dao.DaoMaster;
import nl.sison.xmpp.dao.DaoMaster.DevOpenHelper;
import nl.sison.xmpp.dao.DaoSession;

public class DatabaseUtil {
	public static final String XMPP_CLIENT_DATABASE = "xmpp_client_database.db";
	private static DevOpenHelper helper;

	static public SQLiteDatabase createDatabase(Context context) {
		SQLiteDatabase db = context.openOrCreateDatabase(XMPP_CLIENT_DATABASE,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);

		// DaoMaster.createAllTables(db, true);
		return db;
	}

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
		if (helper != null)
			helper.close();
	}

	// static public void dropTables(Context context) {
	// DevOpenHelper helper = new DevOpenHelper(context, XMPP_CLIENT_DATABASE,
	// null);
	// SQLiteDatabase db = helper.getWritableDatabase();
	// DaoMaster.dropAllTables(db, false);
	// }

}
