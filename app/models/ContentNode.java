package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import play.Logger;
import utils.JsonUtils;
import utils.MongoDbUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Capturing a content node which basically consists of a JSON sub-document
 * and some additional metadata capturing user and time information.
 *
 * <p>Since the content representation as a JSON sub-document does not really
 * fit to the Morphia programming model, we manage instances by our own.</p>
 *
 * <p>The second reason to not use Morphia for this class is the way
 * we move older instances over to a very own <code>versions</code> collection.</p>
 *
 * @author Niko Schmuck
 * @since 21.01.2012
 */
public class ContentNode {

    /** Name of MongoDB collection for content */
    public static final String COLLECTION_NAME = "content";

    /** Name of MongoDB collection for archived content (aka versions) */
    public static final String VERSION_COLLECTION_NAME = "versions";

    // Metadata
    public static final String ATTR_ID         = "_id";
    public static final String ATTR_TYPE       = "_type";
    public static final String ATTR_CREATED    = "_created";
    public static final String ATTR_MODIFIED   = "_modified";
    public static final String ATTR_VERSION    = "_version";
    public static final String ATTR_IDREF      = "_ref";

    // Sub-document holding the manually edited "real" content
    public static final String ATTR_DATA       = "data";

    // ~
    private ObjectId id;
    private Long modified;
    private Long created;
    private Integer version = 1;
    private String type;
    private String jsonContent;
    // TODO add user creator and modifier

    // ~~

    public ContentNode(String type, String jsonContent) {
        this.type = type;
        this.jsonContent = jsonContent;
    }

    public static void createIndexes() {
        MongoDbUtils.ensureIndexes(COLLECTION_NAME, ATTR_TYPE);
        // Also indexes for the version collection
        MongoDbUtils.ensureIndexes(VERSION_COLLECTION_NAME, ATTR_IDREF);
        // create also compound key
        MongoDbUtils.ensureIndexes(VERSION_COLLECTION_NAME, ATTR_IDREF, ATTR_VERSION);
    }

    // ~~

    public void create() {
        DBObject dbObj = new BasicDBObject();
        // Logger.info(".... going to create new content node with: %s", jsonContent);
        DBObject contentObj = MongoDbUtils.convert(jsonContent);
        dbObj.put(ATTR_DATA, contentObj);
        // add metadata
        dbObj.put(ATTR_TYPE, type);
        dbObj.put(ATTR_VERSION, version);
        dbObj.put(ATTR_CREATED, created = System.currentTimeMillis());
        dbObj.put(ATTR_MODIFIED, modified = System.currentTimeMillis());
        MongoDbUtils.create(COLLECTION_NAME, dbObj);
        this.id = (ObjectId) dbObj.get(ATTR_ID);
    }

    public void update(String jsonContent) {
        DBObject contentData = MongoDbUtils.convert(jsonContent);
        MongoDbUtils.updateWithMetadata(COLLECTION_NAME, VERSION_COLLECTION_NAME, getId(), contentData);

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

    public static DBObject rawFindById(String id) {
        DBObject dbObj = null;
        try {
            dbObj = MongoDbUtils.getById(COLLECTION_NAME, id);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid ID specified: %s", e.getMessage());
        }
        return (dbObj != null ? dbObj : null);
    }

    /**
     * Returns lately modified content nodes of the speicified type.
     */
    public static List<ContentNode> findByType(String type, int max) {
        List<ContentNode> nodes = new ArrayList<ContentNode>();
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);
        DBCursor dbCur = dbColl.find(new BasicDBObject(ATTR_TYPE, type)).sort(new BasicDBObject(ATTR_MODIFIED, -1)).limit(max);
        while (dbCur.hasNext()) {
            DBObject dbObj = dbCur.next();
            nodes.add(convert(dbObj));
        }
        return nodes;
    }

    // TODO: Temporary to figure out if this is the right access
    public static List<DBObject> findByTypeRaw(String type, int max) {
        List<DBObject> nodes = new ArrayList<DBObject>();
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);
        DBCursor dbCur = dbColl.find(new BasicDBObject(ATTR_TYPE, type)).sort(new BasicDBObject(ATTR_MODIFIED, -1)).limit(max);
        while (dbCur.hasNext()) {
            DBObject dbObj = dbCur.next();
            nodes.add(dbObj);
        }
        return nodes;
    }

    /**
     * Returns the most recent revisions related to this origin content node.
     */
    public static List<ContentNode> findVersionsForId(String id) {
        List<ContentNode> nodes = new ArrayList<ContentNode>();
        DBCollection dbColl = MongoDbUtils.getDBCollection(VERSION_COLLECTION_NAME);
        DBCursor dbCur = dbColl.find(new BasicDBObject(ATTR_IDREF, id)).sort(new BasicDBObject(ATTR_MODIFIED, -1)).limit(20);
        while (dbCur.hasNext()) {
            DBObject dbObj = dbCur.next();
            nodes.add(convert(dbObj));
        }
        return nodes;
    }

    // ~~

    public static List<Map<String, Object>> convertToMap(List<ContentNode> nodes) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(nodes.size());
        for (ContentNode node : nodes) {
            result.add(JsonUtils.convertToMap(node.getJsonContent()));
        }
        return result;
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
     * Returns pure JSON body without the metadata.
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
        return version != null ? version : 1;
    }

}
