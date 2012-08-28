package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import models.vo.SearchResult;
import play.Logger;
import play.Play;
import play.libs.MimeTypes;
import play.mvc.Router;
import utils.MongoDbUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Store metadata about an media asset (e.g. image, video, etc).
 *
 * @author Niko Schmuck
 * @since 23.04.2012
 */
public class Asset {

    // ~~ Names of keys

    public static final String ID           = "_id";
    public static final String FILENAME     = "filename";
    public static final String UPLOAD_DATE  = "uploadDate";
    public static final String CONTENT_TYPE = "contentType";
    public static final String METADATA     = "metadata";

    // ~~

    public final String id;
    public final String filename;
    public final Date uploadDate;
    public final String contentType;
    // TODO: add creator

    // ~

    public Asset(String id, String filename, Date uploadDate, String contentType) {
        this.id = id;
        this.filename = filename;
        this.uploadDate = uploadDate;
        this.contentType = contentType;
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

    // ~~

    /**
     * ONLY Used by initial data setup by means of YAML definition.
     */
    public void setFile(String filename) throws FileNotFoundException {
        File file = Play.getFile(filename);
        create(filename, new FileInputStream(file));
    }

    public static SearchResult<Asset> findAll(int offset, int max) {
        GridFS gfs = MongoDbUtils.getGridFS();
        DBCursor cursor = gfs.getFileList().sort(new BasicDBObject(FILENAME, 1)).skip(offset).limit(max);

        List<Asset> assets = new ArrayList<Asset>();
        while (cursor.hasNext()) {
            assets.add(fromDBObject(cursor.next()));
        }
        Logger.info("Returning %d files, offset: %d", assets.size(), offset);
        return new SearchResult<Asset>(assets, cursor.count());
    }

    public static SearchResult<Asset> findByFilename(String filename, boolean matchCase, int offset, int max) {
        GridFS gfs = MongoDbUtils.getGridFS();

        DBObject q = createQueryByFilename(filename, matchCase);
        DBCursor cursor = gfs.getFileList(q).sort(new BasicDBObject(FILENAME, 1)).skip(offset).limit(max);

        List<Asset> assets = new ArrayList<Asset>();
        while (cursor.hasNext()) {
            assets.add(fromDBObject(cursor.next()));
        }
        Logger.info("Query for '%s' returned %d matching files", filename, cursor.count());
        return new SearchResult<Asset>(assets, cursor.count());
    }

    public static Asset create(String filename, InputStream content) {
        GridFS gfs = MongoDbUtils.getGridFS();
        GridFSInputFile dbFile = gfs.createFile(content);
        dbFile.setFilename(filename);
        // guess content type from file name extension
        String contentType = MimeTypes.getContentType(filename);
        if (contentType == null || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
            // No valid content type (TODO: could verify against list of allowed content types)
            throw new IllegalArgumentException("Invalid content type: " + contentType + ", currently only image and video supported");
        } else {
            dbFile.setContentType(contentType);
            dbFile.save();
        }
        return new Asset(dbFile.getId().toString(), filename, dbFile.getUploadDate(), contentType);
    }

    // ~~ private helper methods

    private static DBObject createQueryByFilename(String filename, boolean matchCase) {
        BasicDBObject q;
        if (!matchCase) {
            q = new BasicDBObject(FILENAME, new BasicDBObject("$regex", filename).append("$options", "i"));
        } else {
            q = new BasicDBObject(FILENAME, new BasicDBObject("$regex", filename));
        }
        return q;
    }

    private static Asset fromDBObject(DBObject dbObj) {
        String _id = "" + dbObj.get(ID);

        DBObject metadata = (DBObject) dbObj.get(METADATA);
        return new Asset(_id, (String) dbObj.get(FILENAME),
                         (Date) dbObj.get(UPLOAD_DATE), (String) dbObj.get(CONTENT_TYPE));
    }

    // ~~

    @Override
    public String toString() {
        return this.filename;
    }

}
