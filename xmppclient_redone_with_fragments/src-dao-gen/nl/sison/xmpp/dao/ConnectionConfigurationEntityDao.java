package nl.sison.xmpp.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoConfig;
import de.greenrobot.dao.Property;

import nl.sison.xmpp.dao.ConnectionConfigurationEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table CONNECTION_CONFIGURATION_ENTITY.
*/
public class ConnectionConfigurationEntityDao extends AbstractDao<ConnectionConfigurationEntity, Long> {

    public static final String TABLENAME = "CONNECTION_CONFIGURATION_ENTITY";

    /**
     * Properties of entity ConnectionConfigurationEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Label = new Property(1, String.class, "label", false, "LABEL");
        public final static Property Port = new Property(2, String.class, "port", false, "PORT");
        public final static Property Server = new Property(3, String.class, "server", false, "SERVER");
        public final static Property Domain = new Property(4, String.class, "domain", false, "DOMAIN");
        public final static Property Username = new Property(5, String.class, "username", false, "USERNAME");
        public final static Property Password = new Property(6, String.class, "password", false, "PASSWORD");
        public final static Property Resource = new Property(7, String.class, "resource", false, "RESOURCE");
        public final static Property Encrypted = new Property(8, boolean.class, "encrypted", false, "ENCRYPTED");
        public final static Property Compressed = new Property(9, boolean.class, "compressed", false, "COMPRESSED");
        public final static Property Saslauthenticated = new Property(10, boolean.class, "saslauthenticated", false, "SASLAUTHENTICATED");
        public final static Property Connection_success = new Property(11, int.class, "connection_success", false, "CONNECTION_SUCCESS");
    };


    public ConnectionConfigurationEntityDao(DaoConfig config) {
        super(config);
    }
    
    public ConnectionConfigurationEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'CONNECTION_CONFIGURATION_ENTITY' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'LABEL' TEXT NOT NULL UNIQUE ," + // 1: label
                "'PORT' TEXT NOT NULL ," + // 2: port
                "'SERVER' TEXT NOT NULL ," + // 3: server
                "'DOMAIN' TEXT," + // 4: domain
                "'USERNAME' TEXT NOT NULL ," + // 5: username
                "'PASSWORD' TEXT NOT NULL ," + // 6: password
                "'RESOURCE' TEXT NOT NULL ," + // 7: resource
                "'ENCRYPTED' INTEGER NOT NULL ," + // 8: encrypted
                "'COMPRESSED' INTEGER NOT NULL ," + // 9: compressed
                "'SASLAUTHENTICATED' INTEGER NOT NULL ," + // 10: saslauthenticated
                "'CONNECTION_SUCCESS' INTEGER NOT NULL );"); // 11: connection_success
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'CONNECTION_CONFIGURATION_ENTITY'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, ConnectionConfigurationEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getLabel());
        stmt.bindString(3, entity.getPort());
        stmt.bindString(4, entity.getServer());
 
        String domain = entity.getDomain();
        if (domain != null) {
            stmt.bindString(5, domain);
        }
        stmt.bindString(6, entity.getUsername());
        stmt.bindString(7, entity.getPassword());
        stmt.bindString(8, entity.getResource());
        stmt.bindLong(9, entity.getEncrypted() ? 1l: 0l);
        stmt.bindLong(10, entity.getCompressed() ? 1l: 0l);
        stmt.bindLong(11, entity.getSaslauthenticated() ? 1l: 0l);
        stmt.bindLong(12, entity.getConnection_success());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public ConnectionConfigurationEntity readEntity(Cursor cursor, int offset) {
        ConnectionConfigurationEntity entity = new ConnectionConfigurationEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // label
            cursor.getString(offset + 2), // port
            cursor.getString(offset + 3), // server
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // domain
            cursor.getString(offset + 5), // username
            cursor.getString(offset + 6), // password
            cursor.getString(offset + 7), // resource
            cursor.getShort(offset + 8) != 0, // encrypted
            cursor.getShort(offset + 9) != 0, // compressed
            cursor.getShort(offset + 10) != 0, // saslauthenticated
            cursor.getInt(offset + 11) // connection_success
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, ConnectionConfigurationEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setLabel(cursor.getString(offset + 1));
        entity.setPort(cursor.getString(offset + 2));
        entity.setServer(cursor.getString(offset + 3));
        entity.setDomain(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setUsername(cursor.getString(offset + 5));
        entity.setPassword(cursor.getString(offset + 6));
        entity.setResource(cursor.getString(offset + 7));
        entity.setEncrypted(cursor.getShort(offset + 8) != 0);
        entity.setCompressed(cursor.getShort(offset + 9) != 0);
        entity.setSaslauthenticated(cursor.getShort(offset + 10) != 0);
        entity.setConnection_success(cursor.getInt(offset + 11));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(ConnectionConfigurationEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(ConnectionConfigurationEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
