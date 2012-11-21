package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import models.vo.SearchResult;
import play.Logger;
import play.Play;
import play.db.Model;
import play.libs.MimeTypes;
import play.mvc.Router;
import utils.MongoDbUtils;

import java.io.*;
import java.util.*;

/**
 * Store metadata about an media asset (e.g. image, video, etc).
 *
 * @author Niko Schmuck
 * @since 23.04.2012
 */
public class Asset implements Model {

    public static final String[] SUPPORTED_CONTENT_TYPES = Play.configuration.getProperty("cmskern.assets.contenttypes", "image/.*").split(",");

    // ~~ Names of attribute keys

    public static final String ATTR_ID           = "_id";
    public static final String ATTR_FILENAME     = "filename";
    public static final String ATTR_LENGTH       = "length";
    public static final String ATTR_UPLOAD_DATE  = "uploadDate";
    public static final String ATTR_UPLOADER     = "uploadUser";
    public static final String ATTR_CONTENT_TYPE = "contentType";
    public static final String ATTR_METADATA     = "metadata";

    // ~~

    public final String id;
    public final String filename;
    public final Date uploadDate;
    public final String uploader;
    public final String contentType;
    public final long length;

    // ~

    private Asset(String id, String filename, String uploader, Date uploadDate, String contentType, long length) {
        this.id = id;
        this.filename = filename;
        this.uploader = uploader;
        this.uploadDate = uploadDate;
        this.contentType = contentType;
        this.length = length;
    }

    public static Asset create(String filename, String uploader, InputStream content) {
        GridFS gfs = MongoDbUtils.getGridFS();
        GridFSInputFile dbFile = gfs.createFile(content);
        dbFile.setFilename(filename);
        // guess content type from file name extension
        String contentType = MimeTypes.getContentType(filename);
        if (contentType == null || !isSupportedContentType(contentType))  {
            throw new IllegalArgumentException("Content type: " + contentType + " is not supported.");
        } else {
            dbFile.setMetaData(new BasicDBObject(ATTR_UPLOADER, uploader));
            dbFile.setContentType(contentType);
            dbFile.save();
        }
        return new Asset(dbFile.getId().toString(), filename, uploader, dbFile.getUploadDate(), contentType, dbFile.getLength());
    }

    private static Asset convert(DBObject dbObj) {
        DBObject metadata = (DBObject) dbObj.get(ATTR_METADATA);
        return new Asset("" + dbObj.get(ATTR_ID),
                (String) dbObj.get(ATTR_FILENAME),
                metadata != null ? (String) metadata.get(ATTR_UPLOADER) : "N/A",
                (Date) dbObj.get(ATTR_UPLOAD_DATE),
                (String) dbObj.get(ATTR_CONTENT_TYPE),
                (Long) dbObj.get(ATTR_LENGTH));
    }

    // ~~

    public static long count() {
        GridFS gfs = MongoDbUtils.getGridFS();
        DBCursor cursor = gfs.getFileList();
        return cursor.count();
    }

    public String getUrl() {
        Map<String, Object> argMap = new HashMap<String, Object>(1);
        argMap.put("id", id);
        return Router.getFullUrl("Blobs.getBinaryById", argMap);
    }

    public static boolean isSupportedContentType(String contentType) {
        boolean supported = false;
        for (String allowedContentType : SUPPORTED_CONTENT_TYPES) {
            if (contentType.matches(allowedContentType)) {
                supported = true;
                break;
            }
        }
        return supported;
    }

    /**
     * ONLY Used by initial data setup by means of YAML definition as part of fixture.
     */
    public void setFile(String filename) throws FileNotFoundException {
        File file = Play.getFile(filename);
        create(filename, "admin", new FileInputStream(file)); // TODO: smells: to create inside setter
    }

    // ~~

    /**
     * Returns all available assets, freshest uploaded first.
     */
    public static SearchResult<Asset> findAll(int offset, int max) {
        GridFS gfs = MongoDbUtils.getGridFS();
        DBCursor cursor = gfs.getFileList().sort(new BasicDBObject(ATTR_UPLOAD_DATE, -1)).skip(offset).limit(max);

        List<Asset> assets = new ArrayList<Asset>();
        while (cursor.hasNext()) {
            assets.add(convert(cursor.next()));
        }
        Logger.info("Returning %d files, offset: %d", assets.size(), offset);
        return new SearchResult<Asset>(assets, cursor.count());
    }

    /**
     * Returns all assets matching to the given filename (search as regular expression), freshest uploaded first.
     */
    public static SearchResult<Asset> findByFilename(String filename, boolean matchCase, int offset, int max) {
        GridFS gfs = MongoDbUtils.getGridFS();

        DBObject q = createQueryByFilename(filename, matchCase);
        DBCursor cursor = gfs.getFileList(q).sort(new BasicDBObject(ATTR_UPLOAD_DATE, -1)).skip(offset).limit(max);

        List<Asset> assets = new ArrayList<Asset>();
        while (cursor.hasNext()) {
            assets.add(convert(cursor.next()));
        }
        Logger.info("Query for '%s' returned %d matching files", filename, cursor.count());
        return new SearchResult<Asset>(assets, cursor.count());
    }

    // ~~ private helper methods

    private static DBObject createQueryByFilename(String filename, boolean matchCase) {
        BasicDBObject q;
        if (!matchCase) {
            q = new BasicDBObject(ATTR_FILENAME, new BasicDBObject("$regex", filename).append("$options", "i"));
        } else {
            q = new BasicDBObject(ATTR_FILENAME, new BasicDBObject("$regex", filename));
        }
        return q;
    }

    // ~~

    @Override
    public String toString() {
        return this.filename;
    }

    @Override
    public void _save() {
        // TODO: implement   create(filename, uploader, content ???);
    }

    @Override
    public void _delete() {
        GridFS gfs = MongoDbUtils.getGridFS();
        gfs.remove(id);
    }

    @Override
    public Object _key() {
        return id;
    }

}
