package models;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.utils.LongIdEntity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.*;
import models.vo.IdTitle;
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
    public static final String ATTR_CREATOR    = "_creator";
    public static final String ATTR_MODIFIED   = "_modified";
    public static final String ATTR_MODIFIER   = "_modifier";
    public static final String ATTR_VERSION    = "_version";

    /**
     * If the content node is archived in the version collection, this attribute
     * will link back to the original node.
     */
    public static final String ATTR_IDREF      = "_idref";

    /**
     * Sub-document (JSON) holding the manually edited "real" content,
     */
    public static final String ATTR_DATA       = "data";

    /**
     * The sub-document has to have one element named title holding
     * a human-readable string describing this content node.
     */
    public static final String ATTR_TITLE      = "title";
    public static final String Q_ATTR_TITLE    = ATTR_DATA + '.' + ATTR_TITLE;

    // ~

    private Long id;
    private Integer version = 1;

    private Long created;
    private String creator;

    private Long modified;
    private String modifier;

    private String type;
    private String jsonContent;

    // ~~

    public ContentNode(String type, String jsonContent) {
        this.type = type;
        this.jsonContent = jsonContent;
    }

    public static void createIndexes() {
        MongoDbUtils.ensureIndexes(COLLECTION_NAME, ATTR_TYPE);
        MongoDbUtils.ensureIndexes(COLLECTION_NAME, Q_ATTR_TITLE);
        // Also indexes for the version collection
        MongoDbUtils.ensureIndexes(VERSION_COLLECTION_NAME, ATTR_IDREF);
        // create also compound key
        MongoDbUtils.ensureIndexes(VERSION_COLLECTION_NAME, ATTR_IDREF, ATTR_VERSION);
    }

    // ~~

    public void create(String creatorUsername) {
        DBObject dbObj = new BasicDBObject();
        // Logger.info(".... going to create new content node with: %s", jsonContent);
        DBObject contentObj = MongoDbUtils.convert(jsonContent);
        dbObj.put(ATTR_DATA, contentObj);
        // add metadata
        dbObj.put(ATTR_TYPE, type);
        dbObj.put(ATTR_VERSION, version = 1);
        dbObj.put(ATTR_CREATED, created = System.currentTimeMillis());
        dbObj.put(ATTR_CREATOR, creator = creatorUsername);
        dbObj.put(ATTR_MODIFIED, modified = System.currentTimeMillis());
        dbObj.put(ATTR_MODIFIER, modifier = creatorUsername);
        dbObj.put(ATTR_ID, generateLongId(COLLECTION_NAME));
        Logger.debug("Going to create new (%s) content node '%s' ...", type, dbObj.get(ATTR_ID));
        MongoDbUtils.create(COLLECTION_NAME, dbObj);
        this.id = (Long) dbObj.get(ATTR_ID);
    }

    public void update(String username, String jsonContent) {
        DBObject contentData = MongoDbUtils.convert(jsonContent);
        this.modifier = username;
        this.modified = System.currentTimeMillis();
        updateWithMetadata(modifier, modified, getId(), contentData);
    }

    public static long count() {
        return MongoDbUtils.count(COLLECTION_NAME);
    }

    public void delete() {
        MongoDbUtils.delete(COLLECTION_NAME, getId());
    }

    public static void deleteAll() {
        MongoDbUtils.drop(COLLECTION_NAME);
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

    public static DBObject findByIdRaw(String id) {
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
     * Search all instances of the specified content type and query string in the document title.
     * By default the result is sorted by the title alphabetically.
     */
    public static List<IdTitle> findByTypeAndTitleMinimal(String type, String query, boolean matchCase, int max) {
        List<IdTitle> nodes = new ArrayList<IdTitle>();
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);

        BasicDBObject q = new BasicDBObject(ATTR_TYPE, type);
        if (matchCase) {
            q.append(Q_ATTR_TITLE, new BasicDBObject("$regex", query));
        } else {
            q.append(Q_ATTR_TITLE, new BasicDBObject("$regex", query).append("$options", "i"));
        }

        // Only fetch title (and implictly ID)
        DBCursor dbCur = dbColl.find(q, new BasicDBObject(Q_ATTR_TITLE, 1)).sort(new BasicDBObject(Q_ATTR_TITLE, 1)).limit(max);
        Logger.info("Query for %s", query);
        while (dbCur.hasNext()) {
            DBObject dbObj = dbCur.next();
            nodes.add(new IdTitle((Long) dbObj.get(ATTR_ID), (String) ((DBObject) dbObj.get(ATTR_DATA)).get(ATTR_TITLE)));
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
        Long id = (Long) dbObj.get(ATTR_ID);
        //dbObj.removeField("_id");
        String type = (String) dbObj.get(ATTR_TYPE);
        //dbObj.removeField("_type");
        Integer version = (Integer) dbObj.get(ATTR_VERSION);
        Long created = (Long) dbObj.get(ATTR_CREATED);
        String creator = (String) dbObj.get(ATTR_CREATOR);
        //dbObj.removeField("_created");
        Long modified = (Long) dbObj.get(ATTR_MODIFIED);
        String modifier = (String) dbObj.get(ATTR_MODIFIER);
        //dbObj.removeField("_modified");
        String jsonContent = dbObj.get(ATTR_DATA).toString();
        // ~~
        ContentNode node = new ContentNode(type, jsonContent);
        node.id = id;
        node.type = type;
        node.version = version;
        node.created = created;
        node.creator = creator;
        node.modified = modified;
        node.modifier = modifier;
        return node;
    }

    private static void updateWithMetadata(final String username, long timestamp,
                                           final String id, DBObject contentData) {
        contentData.removeField(""); // TODO: fix earlier in call chain
        // Logger.debug("~~ Update MongoDB with values: %s", contentData.toString());
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);

        // ~~ Get current version and save it to archive
        DBObject verObj = dbColl.findOne(MongoDbUtils.queryById(id));
        verObj.put(ATTR_ID, generateLongId(VERSION_COLLECTION_NAME));
        verObj.put(ATTR_IDREF, id);
        MongoDbUtils.getDBCollection(VERSION_COLLECTION_NAME).save(verObj);

        // ~~ Update existing object
        WriteResult res = dbColl.update(MongoDbUtils.queryById(id),
                new BasicDBObject("$set", new BasicDBObject()
                        .append(ContentNode.ATTR_DATA, contentData)
                        .append(ContentNode.ATTR_MODIFIED, timestamp)
                        .append(ContentNode.ATTR_MODIFIER, username)
                ), true, false);
        Logger.info("~~ Update content, result: %s", res.getLastError());

        // Increment version number
        res = dbColl.update(MongoDbUtils.queryById(id), new BasicDBObject("$inc", new BasicDBObject(ContentNode.ATTR_VERSION, 1)));
        Logger.debug("~~ Incremented version, result %s", res.getLastError());
    }

    // ~~

    /**
     * Used by Bootstraper to setup initial content by means of YAML definition.
     */
    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
        create(this.creator);
    }

    /**
     * Returns pure JSON body without the metadata.
     */
    public String getJsonContent() {
        return jsonContent;
    }

    public String getId() {
        return id.toString();
    }

    public Date getCreated() {
        return new Date(created);
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public Date getModified() {
        return new Date(modified);
    }

    public String getModifier() {
        return modifier;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        JsonParser parser = new JsonParser();
        JsonObject jsonRoot = parser.parse(jsonContent).getAsJsonObject();
        return jsonRoot.has(ATTR_TITLE) ? jsonRoot.get(ATTR_TITLE).getAsString() : null;
    }

    public Integer getVersion() {
        return version != null ? version : 1;
    }

    // ~~

    private static Long generateLongId(String collName) {
        Datastore ds = MongoDbUtils.getDatastore();
        Query<LongIdEntity.StoredId> q = ds.find(LongIdEntity.StoredId.class, ATTR_ID, collName);
        UpdateOperations<LongIdEntity.StoredId> uOps = ds.createUpdateOperations(LongIdEntity.StoredId.class).inc("value");
        LongIdEntity.StoredId newId = ds.findAndModify(q, uOps);
        if (newId == null) {
            newId = new LongIdEntity.StoredId(collName);
            ds.save(newId);
        }
        return newId.getValue();
    }

}
