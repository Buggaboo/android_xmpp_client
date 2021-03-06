package nl.sison.xmpp.dao;

import nl.sison.xmpp.dao.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table BUDDY_ENTITY.
 */
public class BuddyEntity {

    private Long id;
    /** Not-null value. */
    private String partial_jid;
    private String last_seen_resource;
    private String nickname;
    private Boolean vibrate;
    private String presence_status;
    private String presence_mode;
    private String presence_type;
    private java.util.Date last_chat_date;
    private java.util.Date last_seen_online_date;
    private Boolean isAvailable;
    private Boolean isAway;
    private Boolean isActive;
    private long connectionId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient BuddyEntityDao myDao;

    private ConnectionConfigurationEntity connectionConfigurationEntity;
    private Long connectionConfigurationEntity__resolvedKey;


    public BuddyEntity() {
    }

    public BuddyEntity(Long id) {
        this.id = id;
    }

    public BuddyEntity(Long id, String partial_jid, String last_seen_resource, String nickname, Boolean vibrate, String presence_status, String presence_mode, String presence_type, java.util.Date last_chat_date, java.util.Date last_seen_online_date, Boolean isAvailable, Boolean isAway, Boolean isActive, long connectionId) {
        this.id = id;
        this.partial_jid = partial_jid;
        this.last_seen_resource = last_seen_resource;
        this.nickname = nickname;
        this.vibrate = vibrate;
        this.presence_status = presence_status;
        this.presence_mode = presence_mode;
        this.presence_type = presence_type;
        this.last_chat_date = last_chat_date;
        this.last_seen_online_date = last_seen_online_date;
        this.isAvailable = isAvailable;
        this.isAway = isAway;
        this.isActive = isActive;
        this.connectionId = connectionId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBuddyEntityDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getPartial_jid() {
        return partial_jid;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPartial_jid(String partial_jid) {
        this.partial_jid = partial_jid;
    }

    public String getLast_seen_resource() {
        return last_seen_resource;
    }

    public void setLast_seen_resource(String last_seen_resource) {
        this.last_seen_resource = last_seen_resource;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getVibrate() {
        return vibrate;
    }

    public void setVibrate(Boolean vibrate) {
        this.vibrate = vibrate;
    }

    public String getPresence_status() {
        return presence_status;
    }

    public void setPresence_status(String presence_status) {
        this.presence_status = presence_status;
    }

    public String getPresence_mode() {
        return presence_mode;
    }

    public void setPresence_mode(String presence_mode) {
        this.presence_mode = presence_mode;
    }

    public String getPresence_type() {
        return presence_type;
    }

    public void setPresence_type(String presence_type) {
        this.presence_type = presence_type;
    }

    public java.util.Date getLast_chat_date() {
        return last_chat_date;
    }

    public void setLast_chat_date(java.util.Date last_chat_date) {
        this.last_chat_date = last_chat_date;
    }

    public java.util.Date getLast_seen_online_date() {
        return last_seen_online_date;
    }

    public void setLast_seen_online_date(java.util.Date last_seen_online_date) {
        this.last_seen_online_date = last_seen_online_date;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Boolean getIsAway() {
        return isAway;
    }

    public void setIsAway(Boolean isAway) {
        this.isAway = isAway;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    /** To-one relationship, resolved on first access. */
    public ConnectionConfigurationEntity getConnectionConfigurationEntity() {
        if (connectionConfigurationEntity__resolvedKey == null || !connectionConfigurationEntity__resolvedKey.equals(connectionId)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ConnectionConfigurationEntityDao targetDao = daoSession.getConnectionConfigurationEntityDao();
            connectionConfigurationEntity = targetDao.load(connectionId);
            connectionConfigurationEntity__resolvedKey = connectionId;
        }
        return connectionConfigurationEntity;
    }

    public void setConnectionConfigurationEntity(ConnectionConfigurationEntity connectionConfigurationEntity) {
        if (connectionConfigurationEntity == null) {
            throw new DaoException("To-one property 'connectionId' has not-null constraint; cannot set to-one to null");
        }
        this.connectionConfigurationEntity = connectionConfigurationEntity;
        connectionId = connectionConfigurationEntity.getId();
        connectionConfigurationEntity__resolvedKey = connectionId;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
