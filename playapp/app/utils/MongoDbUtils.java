package utils;

import com.google.code.morphia.Datastore;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import play.Logger;
import play.Play;
import play.modules.morphia.MorphiaPlugin;

import java.util.Arrays;

/**
 * Simple operations on the MongoDB,
 * using the morphia play module (only for the time being,
 * since the mongo play module seems to be not any longer maintained).
 *
 * @author Niko Schmuck
 * @since 20.01.2012
 */
public final class MongoDbUtils {

    // ~~ Basics

    public static boolean isValidId(String id) {
        return ObjectId.isValid(id);
    }

    public static String getDBServers() {
        return StringUtils.join(MorphiaPlugin.ds().getDB().getMongo().getAllAddress(), ",");
    }

    public static Datastore getDatastore() {
        return MorphiaPlugin.ds();
    }

    public static String getDBName() {
        return MorphiaPlugin.ds().getDB().getName();
    }

    public static DBCollection getDBCollection(final String collectionName) {
        Datastore datastore = MorphiaPlugin.ds();
        DB db = datastore.getDB();
        return db.getCollection(collectionName);
    }

    public static void ensureIndexes(final String collectionName, String ... columns) {
        Logger.info("~~ Ensure MongoDB index for %s on %s", Arrays.asList(columns), collectionName);
        DBCollection dbColl = getDBCollection(collectionName);
        BasicDBObject keys = new BasicDBObject();
        for (String column : columns) {
            keys.append(column, 1); // assume ascending index
        }
        dbColl.createIndex(keys);
    }

    public static void drop(final String collectionName) {
        getDBCollection(collectionName).drop();
    }


    // ~~ Operations on single documents

    public static DBObject convert(final String jsonRepresentation) {
        Object o = com.mongodb.util.JSON.parse(jsonRepresentation);
        return (DBObject) o;
    }
    
    public static void create(final String collectionName, DBObject object) {
        object.removeField(""); // TODO: fix earlier in call chain
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.insert(object);
    }

    public static void update(final String collectionName, final Number id, DBObject object) {
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.update(queryById(id), object);
    }

    public static void delete(final String collectionName, final Number id) {
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.remove(queryById(id));
    }

    public static DBObject getById(final String collectionName, final Number id) {
        DBCollection dbColl = getDBCollection(collectionName);
        return dbColl.findOne(queryById(id));
    }

    public static DBObject queryById(final Number id) {
        return new BasicDBObject("_id", id);
    }

    public static long count(String collectionName) {
        DBCollection dbColl = getDBCollection(collectionName);
        return dbColl.count();
    }

    // ~~ GridFS

    public static GridFS getGridFS() {
        Datastore datastore = MorphiaPlugin.ds();
        DB db = datastore.getDB();
        // name of collection to store assets in
        String collectionName = Play.configuration.getProperty("morphia.collection.upload", "uploads");
        return new GridFS(db, collectionName);
    }

    public static GridFSDBFile getFileByFilename(String filename) {
        GridFS gfs = MongoDbUtils.getGridFS();
        return gfs.findOne(filename);
    }

    public static GridFSDBFile getFileById(String id) {
        GridFS gfs = MongoDbUtils.getGridFS();
        return gfs.findOne(new ObjectId(id));
    }

    public static void dropFiles() {
        GridFS gfs = MongoDbUtils.getGridFS();
        gfs.remove(new BasicDBObject());
    }

}
