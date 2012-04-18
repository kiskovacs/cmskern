package utils;

import com.google.code.morphia.Datastore;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import models.ContentNode;
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

    public static DBObject convert(final String jsonRepresentation) {
        Object o = com.mongodb.util.JSON.parse(jsonRepresentation);
        return (DBObject) o;
    }
    
    public static void create(final String collectionName, DBObject object) {
        object.removeField(""); // TODO: schon höher lösen
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.insert(object);
    }

    public static void update(final String collectionName, final String id, DBObject object) {
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.update(queryById(id), object);
    }

    public static void updateWithMetadata(final String collectionName, final String versionCollectionName,
                                          final String id, DBObject contentData) {
        contentData.removeField(""); // TODO: schon höher lösen
        // Logger.debug("~~ Update MongoDB with values: %s", contentData.toString());
        DBCollection dbColl = getDBCollection(collectionName);

        // ~~ Get current version
        DBObject verObj = dbColl.findOne(queryById(id));
        verObj.removeField("_id");
        verObj.put("_ref", id);
        getDBCollection(versionCollectionName).save(verObj);

        // ~~ Update existing object
        WriteResult res = dbColl.update(queryById(id), new BasicDBObject("$set", new BasicDBObject(ContentNode.ATTR_DATA, contentData)), true, false);
        Logger.info("~~ Update values, result %s", res.getLastError());
        // Update last modified date
        res = dbColl.update(queryById(id), new BasicDBObject("$set", new BasicDBObject(ContentNode.ATTR_MODIFIED, System.currentTimeMillis())));
        Logger.debug("~~ Update last modified date, result %s", res.getLastError());
        // Increment version number
        res = dbColl.update(queryById(id), new BasicDBObject("$inc", new BasicDBObject(ContentNode.ATTR_VERSION, 1)));
        Logger.debug("~~ Incremented version, result %s", res.getLastError());
    }

    public static void delete(final String collectionName, final String id) {
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.remove(queryById(id));
    }

    public static DBObject getById(final String collectionName, final String id) {
        DBCollection dbColl = getDBCollection(collectionName);
        return dbColl.findOne(queryById(id));
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
    

    // ~~

    public static DBObject queryById(final String id) {
        return new BasicDBObject("_id", new ObjectId(id));
    }
    
    public static DBCollection getDBCollection(final String collectionName) {
        Datastore datastore = MorphiaPlugin.ds();
        DB db = datastore.getDB();
        return db.getCollection(collectionName);
    }

    public static String getDBName() {
        return MorphiaPlugin.ds().getDB().getName();
    }

    public static String getDBServers() {
        return StringUtils.join(MorphiaPlugin.ds().getDB().getMongo().getAllAddress(), ",");
    }

    public static GridFS getGridFS() {
        Datastore datastore = MorphiaPlugin.ds();
        DB db = datastore.getDB();
        // name of collection to store assets in
        String collectionName = Play.configuration.getProperty("morphia.collection.upload", "uploads");
        return new GridFS(db, collectionName);
    }
    
}
