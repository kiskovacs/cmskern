package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.io.IOUtils;
import play.Logger;
import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.Model;
import utils.MongoDbUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The type model class which stores one content type definition
 * in a JSON blueprint which is used to construct the form to
 * create and update concrete content nodes.
 *
 * @author Niko Schmuck
 * @since 21.01.2012
 */
public class ContentType implements Model {

    /** Name of MongoDB collection for content */
    public static final String COLLECTION_NAME = "contentTypes";

    // Name of field names in the collection
    public static final String ATTR_ID           = "_id";
    public static final String ATTR_NAME         = "name";
    public static final String ATTR_DISPLAY_NAME = "displayName";
    public static final String ATTR_GROUP        = "group";
    public static final String ATTR_SORTKEY      = "sortkey";
    public static final String ATTR_DESCRIPTION  = "description";
    public static final String ATTR_JSON_SCHEMA  = "jsonSchema";
    public static final String ATTR_CREATED         = "_created";
    public static final String ATTR_MODIFIED        = "_modified";

    // ~~

    private Number id;

    @Required
    // TODO @Indexed(unique = true)   --> ensureIndex
    public String name;

    @Required
    public String displayName;

    /**
     * Allows to group multiple content types together
     * (for example to distinguish between content and site related types).
     */
    public String group;

    /**
     * The sort key is only used for sorting in the same group,
     * i.e. when displaying content types in the back-office.
     */
    public String sortkey;

    /**
     * A short description about what this content type should be used for.
     */
    public String description;

    /**
     * The field definition of the content type as JSON schema
     * specified by: http://json-schema.org/draft-03/schema
     */
    @Required
    @MaxSize(20000)
    public String jsonSchema;

    public Date modified;
    public Date created;


    // ~~

    public ContentType(String name, String displayName, String jsonSchema) {
        this.name = name;
        this.displayName = displayName;
        this.jsonSchema = jsonSchema;
    }

    private static ContentType convert(DBObject dbObj) {
        Number id = (Number) dbObj.get(ATTR_ID);
        String name = (String) dbObj.get(ATTR_NAME);
        String displayName = (String) dbObj.get(ATTR_DISPLAY_NAME);
        String jsonSchema = (String) dbObj.get(ATTR_JSON_SCHEMA);
        String description = (String) dbObj.get(ATTR_DESCRIPTION);
        String group = (String) dbObj.get(ATTR_GROUP);
        String sortkey = (String) dbObj.get(ATTR_SORTKEY);
        Date created = (Date) dbObj.get(ATTR_CREATED);
        Date modified = (Date) dbObj.get(ATTR_MODIFIED);
        // ~~
        ContentType type = new ContentType(name, displayName, jsonSchema);
        type.id = id;
        type.description = description;
        type.group = group;
        type.sortkey = sortkey;
        type.created = created;
        type.modified = modified;
        return type;
    }

    // ~~

    public static long count() {
        return MongoDbUtils.count(COLLECTION_NAME);
    }

    public static ContentType findById(Number id) {
        DBObject dbObj = null;
        try {
            dbObj = MongoDbUtils.findById(COLLECTION_NAME, id);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid ID specified: %s", e.getMessage());
        }

        return (dbObj != null ? convert(dbObj) : null);
    }


    public static ContentType findByName(final String name) {
        DBObject dbObj = null;
        try {
            dbObj = MongoDbUtils.findByKeyValue(COLLECTION_NAME, ATTR_NAME, name);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid ID specified: %s", e.getMessage());
        }

        return (dbObj != null ? convert(dbObj) : null);
    }

    public static List<ContentType> findAll() {
        List<ContentType> types = new ArrayList<ContentType>();
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);
        DBCursor dbCur = dbColl.find().sort(new BasicDBObject(ATTR_SORTKEY, 1)); // .skip(offset).limit(max);
        while (dbCur.hasNext()) {
            DBObject dbObj = dbCur.next();
            types.add(convert(dbObj));
        }
        return types;
    }

    public static List<ContentType> findByGroup(final String group) {
        List<ContentType> types = new ArrayList<ContentType>();
        DBCollection dbColl = MongoDbUtils.getDBCollection(COLLECTION_NAME);
        BasicDBObject q = new BasicDBObject(ATTR_GROUP, group);
        DBCursor dbCur = dbColl.find(q).sort(new BasicDBObject(ATTR_SORTKEY, 1)); // .skip(offset).limit(max);
        while (dbCur.hasNext()) {
            DBObject dbObj = dbCur.next();
            types.add(convert(dbObj));
        }
        return types;
    }


    private boolean validateJson() {
        return false; // TODO implement schema validation
    }

    /**
     * Called by Bootstrap definition.
     */
    public void setJsonSchemaFromFile(String filename) throws IOException {
        File inputFile = Play.getFile(filename);
        Logger.info("   ~ read schema definition from %s", inputFile.getAbsolutePath());
        jsonSchema = IOUtils.toString(new FileReader(inputFile));
    }

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public void _save() {
        // TODO: create();
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
