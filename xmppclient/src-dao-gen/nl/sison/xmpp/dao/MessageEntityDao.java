package nl.sison.xmpp.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoConfig;
import de.greenrobot.dao.Property;

import nl.sison.xmpp.dao.MessageEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table MESSAGE_ENTITY.
*/
public class MessageEntityDao extends AbstractDao<MessageEntity, Long> {

    public static final String TABLENAME = "MESSAGE_ENTITY";

    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Sender_jid = new Property(1, String.class, "sender_jid", false, "SENDER_JID");
        public final static Property Receiver_jid = new Property(2, String.class, "receiver_jid", false, "RECEIVER_JID");
        public final static Property Content = new Property(3, String.class, "content", false, "CONTENT");
        public final static Property Received_date = new Property(4, java.util.Date.class, "received_date", false, "RECEIVED_DATE");
        public final static Property Delivered = new Property(5, Boolean.class, "delivered", false, "DELIVERED");
        public final static Property Processed = new Property(6, Boolean.class, "processed", false, "PROCESSED");
    };


    public MessageEntityDao(DaoConfig config) {
        super(config);
    }
    
    public MessageEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String sql = "CREATE TABLE " + (ifNotExists? "IF NOT EXISTS ": "") + "'MESSAGE_ENTITY' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'SENDER_JID' TEXT NOT NULL ," + // 1: sender_jid
                "'RECEIVER_JID' TEXT NOT NULL ," + // 2: receiver_jid
                "'CONTENT' TEXT NOT NULL ," + // 3: content
                "'RECEIVED_DATE' INTEGER," + // 4: received_date
                "'DELIVERED' INTEGER," + // 5: delivered
                "'PROCESSED' INTEGER);"; // 6: processed
        db.execSQL(sql);
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'MESSAGE_ENTITY'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, MessageEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getSender_jid());
        stmt.bindString(3, entity.getReceiver_jid());
        stmt.bindString(4, entity.getContent());
 
        java.util.Date received_date = entity.getReceived_date();
        if (received_date != null) {
            stmt.bindLong(5, received_date.getTime());
        }
 
        Boolean delivered = entity.getDelivered();
        if (delivered != null) {
            stmt.bindLong(6, delivered ? 1l: 0l);
        }
 
        Boolean processed = entity.getProcessed();
        if (processed != null) {
            stmt.bindLong(7, processed ? 1l: 0l);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public MessageEntity readEntity(Cursor cursor, int offset) {
        MessageEntity entity = new MessageEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // sender_jid
            cursor.getString(offset + 2), // receiver_jid
            cursor.getString(offset + 3), // content
            cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)), // received_date
            cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0, // delivered
            cursor.isNull(offset + 6) ? null : cursor.getShort(offset + 6) != 0 // processed
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, MessageEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setSender_jid(cursor.getString(offset + 1));
        entity.setReceiver_jid(cursor.getString(offset + 2));
        entity.setContent(cursor.getString(offset + 3));
        entity.setReceived_date(cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)));
        entity.setDelivered(cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0);
        entity.setProcessed(cursor.isNull(offset + 6) ? null : cursor.getShort(offset + 6) != 0);
     }
    
    @Override
    protected Long updateKeyAfterInsert(MessageEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(MessageEntity entity) {
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
