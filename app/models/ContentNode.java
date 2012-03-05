package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import play.Logger;
import utils.MongoDbUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Capturing a content node which basically consists of a rough JSON document
 * and some additional metadata.
 *
 * @author Niko Schmuck
 * @since 21.01.2012
 */
public class ContentNode {

    public static final String COLLECTION_NAME = "content";

    public static final String ATTR_ID         = "_id";
    public static final String ATTR_TYPE       = "_type";
    public static final String ATTR_CREATED    = "_created";
    public static final String ATTR_MODIFIED   = "_modified";
    public static final String ATTR_VERSION    = "_version";
    public static final String ATTR_DATA       = "data";

    private ObjectId id;
    private Long modified;
    private Long created;
    private Integer version;
    private String type;
    private String jsonContent;
    

    public ContentNode(String type, String jsonContent) {
        this.type = type;
        this.jsonContent = jsonContent;
    }
    
    public void create() {
        DBObject dbObj = new BasicDBObject();
        DBObject contentObj = MongoDbUtils.convert(jsonContent);
        dbObj.put(ATTR_DATA, contentObj);
        // add some metadata
        dbObj.put(ATTR_TYPE, type);
        dbObj.put(ATTR_VERSION, 1);
        dbObj.put(ATTR_CREATED, created = System.currentTimeMillis());
        dbObj.put(ATTR_MODIFIED, modified = System.currentTimeMillis());
        MongoDbUtils.create(COLLECTION_NAME, dbObj);
        this.id = (ObjectId) dbObj.get(ATTR_ID);
    }

    public void update(String jsonContent) {
        DBObject contentData = MongoDbUtils.convert(jsonContent);
        MongoDbUtils.updateWithMetadata(COLLECTION_NAME, getId(), contentData);   // TODO <-----

    }

    public void delete() {
        MongoDbUtils.delete(COLLECTION_NAME, getId());
    }

    // ~~

    public static ContentNode findById(String id) {
        DBObject dbObj = null;
        try {
            dbObj = MongoDbUtils.getById(COLLECTION_NAME, id);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid ID specified: %s", e.getMessage());
        }
        return (dbObj != null ? convert(dbObj) : null);  // TODO: weg mit dem Doppel-konvertieren
    }

    public static DBObject findByIdAsNative(String id) {
        DBObject dbObj = null;
        try {
            dbObj = MongoDbUtils.getById(COLLECTION_NAME, id);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid ID specified: %s", e.getMessage());
        }
        return (dbObj != null ? dbObj : null);
    }

    public static List<ContentNode> findByType(String type) {
        List<ContentNode> nodes = new ArrayList<ContentNode>();
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);
        DBCursor dbCur = dbColl.find(new BasicDBObject(ATTR_TYPE, type)).sort(new BasicDBObject(ATTR_MODIFIED, -1)).limit(100);
        while (dbCur.hasNext()) {
            DBObject dbObj = dbCur.next();
            nodes.add(convert(dbObj));
        }
        return nodes;
    }

    // ~~

    private static ContentNode convert(DBObject dbObj) {
        ObjectId id = (ObjectId) dbObj.get(ATTR_ID);
        //dbObj.removeField("_id");
        String type = (String) dbObj.get(ATTR_TYPE);
        //dbObj.removeField("_type");
        Integer version = (Integer) dbObj.get(ATTR_VERSION);
        Long created = (Long) dbObj.get(ATTR_CREATED);
        //dbObj.removeField("_created");
        Long modified = (Long) dbObj.get(ATTR_MODIFIED);
        //dbObj.removeField("_modified");
        String jsonContent = dbObj.get(ATTR_DATA).toString();
        // ~~
        ContentNode node = new ContentNode(type, jsonContent);
        node.id = id;
        node.type = type;
        node.version = version;
        node.modified = modified;
        node.created = created;
        return node;
    }
    

    // ~~

    /**
     * Returns the pure JSON body without the metadata.
     */
    public String getJsonContent() {
        return jsonContent;
    }

    public String getId() {
        return id.toString();
    }

    public Date getModified() {
        return new Date(modified);
    }

    public Date getCreated() {
        return new Date(created);
    }

    public String getType() {
        return type;
    }

    public Integer getVersion() {
        return version;
    }
}
