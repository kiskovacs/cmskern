package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Required;
import play.db.Model;
import play.libs.Codec;
import utils.MongoDbUtils;

import java.util.Date;

/**
 * Representing a single user accessing the system.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
public class User implements Model {

    /** Name of MongoDB collection for content */
    public static final String COLLECTION_NAME = "users";

    // Name of field names in the collection
    public static final String ATTR_ID              = "_id";
    public static final String ATTR_USER_NAME       = "userName";
    public static final String ATTR_FULL_NAME       = "fullName";
    public static final String ATTR_EMAIL           = "email";
    public static final String ATTR_PASSWD_HASH     = "passwd_hash";
    public static final String ATTR_ROLE            = "role";
    public static final String ATTR_LAST_LOGIN_DATE = "lastLoginDate";
    public static final String ATTR_CREATED         = "_created";
    public static final String ATTR_MODIFIED        = "_modified";

    // ~~

    private Number id;

    @Required
    // TODO @Indexed(unique = true)
    public String userName;

    public String fullName;

    @Email
    @Required
    public String email;

    /**
     * Holds the SHA1 value of the password (40 byte long hexadecimal string).
     */
    public String passwd_hash;

    @Required
    // TODO @Reference
    public String role;

    private Long modified;
    private Long created;
    public Date lastLoginAt;

    // ~~

    public User(String userName, String fullName, String role) {
        this.userName = userName;
        this.fullName = fullName;
        this.role = role;
    }

    public static void createIndexes() {
        MongoDbUtils.ensureIndexes(COLLECTION_NAME, ATTR_USER_NAME);
        MongoDbUtils.ensureIndexes(COLLECTION_NAME, ATTR_ROLE);
    }

    // ~~

    public static User findByUserName(String userName) {
        DBObject dbObj = null;
        try {
            dbObj = MongoDbUtils.findByKeyValue(COLLECTION_NAME, ATTR_USER_NAME, userName);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid ID specified: %s", e.getMessage());
        }
        return convert(dbObj);
    }

    public static long count() {
        return MongoDbUtils.count(COLLECTION_NAME);
    }

    // ~~

    private static User convert(DBObject dbObj) {
        Number id = (Number) dbObj.get(ATTR_ID);
        String userName = (String) dbObj.get(ATTR_USER_NAME);
        String fullName = (String) dbObj.get(ATTR_FULL_NAME);
        String role = (String) dbObj.get(ATTR_ROLE);
        String email = (String) dbObj.get(ATTR_EMAIL);
        String passwd_hash = (String) dbObj.get(ATTR_PASSWD_HASH);
        Long created = (Long) dbObj.get(ATTR_CREATED);
        Long modified = (Long) dbObj.get(ATTR_MODIFIED);
        Date lastLogin = (Date) dbObj.get(ATTR_LAST_LOGIN_DATE);
        // ~~
        User user = new User(userName, fullName, role);
        user.id = id;
        user.email = email;
        user.passwd_hash = passwd_hash;
        user.created = created;
        user.modified = modified;
        user.lastLoginAt = lastLogin;
        return user;
    }

    // ~~

    // TODO: überflüssig
    public Date getCreated() {
        return new Date(created);
    }

    // TODO: überflüssig
    public Date getModified() {
        return new Date(modified);
    }

    public void setPasswd(String passwd) {
        if (passwd != null && passwd.trim().length() > 0) {
            // calculate hash from the given plain-text password
            this.passwd_hash = hashedPw(passwd);
        }
    }

    public static User authenticate(String userName, String password) {
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);
        DBObject q = new BasicDBObject(ATTR_USER_NAME, userName).append(ATTR_PASSWD_HASH, hashedPw(password));
        DBObject update = new BasicDBObject("$set", new BasicDBObject(ATTR_LAST_LOGIN_DATE, new Date()));
        DBObject dbObj = dbColl.findAndModify(q, update);

        return (dbObj != null) ? convert(dbObj) : null;
    }

    private static String hashedPw(String clear) {
        return Codec.hexSHA1(clear);
    }

    // ~~

    @Override
    public String toString() {
        return this.userName;
    }

    @Override
    public void _save() {
        // TODO: create(...) ???
    }

    @Override
    public void _delete() {
        MongoDbUtils.delete(COLLECTION_NAME, id);
    }

    @Override
    public Object _key() {
        return id;
    }
}
