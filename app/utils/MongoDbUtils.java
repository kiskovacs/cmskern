package utils;

import com.google.code.morphia.Datastore;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import models.ContentNode;
import org.bson.types.ObjectId;
import play.Logger;
import play.Play;
import play.modules.morphia.MorphiaPlugin;

/**
 * Simple operations on the MongoDB,
 * using the morphia play module (only for the time being,
 * since the mongo play module seems to be not any longer maintained).
 *
 * @author Niko Schmuck
 * @since 20.01.2012
 */
public final class MongoDbUtils {

    public static DBObject convert(String jsonRepresentation) {
        Object o = com.mongodb.util.JSON.parse(jsonRepresentation);
        return (DBObject) o;
    }
    
    public static void create(String collectionName, DBObject object) {
        object.removeField(""); // TODO: schon höher lösen
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.insert(object);
    }

    public static void update(String collectionName, String id, DBObject object) {
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.update(queryById(id), object);
    }

    public static void updateWithMetadata(String collectionName, String id, DBObject contentData) {
        contentData.removeField(""); // TODO: schon höher lösen
        // Logger.debug("~~ Update mongoDB with values: %s", contentData.toString());
        DBCollection dbColl = getDBCollection(collectionName);

        // ~~ Get current version
        DBObject verObj = dbColl.findOne(queryById(id));
        verObj.removeField("_id");
        verObj.put("_ref", id);
        getDBCollection("versions").save(verObj);

        // ~~ Update existing object
        WriteResult res = dbColl.update(queryById(id), new BasicDBObject("$set", new BasicDBObject(ContentNode.ATTR_DATA, contentData)), true, false);
        Logger.info("~~ Update values, result %s", res.getLastError());
        // Update last modified date
        dbColl.update(queryById(id), new BasicDBObject("$set", new BasicDBObject(ContentNode.ATTR_MODIFIED, System.currentTimeMillis())));
        // Increment version number
        dbColl.update(queryById(id), new BasicDBObject("$inc", new BasicDBObject(ContentNode.ATTR_VERSION, 1)));
    }

    public static void delete(String collectionName, String id) {
        DBCollection dbColl = getDBCollection(collectionName);
        dbColl.remove(queryById(id));
    }

    public static DBObject getById(String collectionName, String id) {
        DBCollection dbColl = getDBCollection(collectionName);
        return dbColl.findOne(queryById(id));
    }


    // ~~

    public static DBObject queryById(String id) {
        return new BasicDBObject("_id", new ObjectId(id));
    }
    
    public static DBCollection getDBCollection(String collectionName) {
        Datastore datastore = MorphiaPlugin.ds();
        DB db = datastore.getDB();
        return db.getCollection(collectionName);
    }

    public static GridFS getGridFS() {
        Datastore datastore = MorphiaPlugin.ds();
        DB db = datastore.getDB();
        // name of bucket to store assets in
        String collectionName = Play.configuration.getProperty("morphia.collection.upload", "uploads");
        return new GridFS(db, collectionName);
    }
    
}
