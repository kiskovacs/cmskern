package models;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import play.mvc.Router;
import utils.MongoDbUtils;

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
    public final String url;
    public final String filename;
    public final Date uploadDate;
    public final String contentType;

    // ~

    public Asset(String id, String url, String filename, Date uploadDate, String contentType) {
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.uploadDate = uploadDate;
        this.contentType = contentType;
    }

    // ~~

    public static Collection<Asset> findAll() {
        GridFS gfs = MongoDbUtils.getGridFS();
        DBCursor cursor = gfs.getFileList();  // TODO: add criteria or sorting
        Collection<Asset> assets = new ArrayList<Asset>();
        while (cursor.hasNext()) {
            assets.add(fromDBObject(cursor.next()));
        }
        return assets;
    }

    private static Asset fromDBObject(DBObject dbObj) {
        Map<String, Object> argMap = new HashMap<String, Object>(1);
        String _id = "" + dbObj.get(ID);
        argMap.put("id", _id);
        String url = Router.getFullUrl("Blobs.getBinaryById", argMap);

        DBObject metadata = (DBObject) dbObj.get(METADATA);
        return new Asset(_id, url, (String) dbObj.get(FILENAME),
                         (Date) dbObj.get(UPLOAD_DATE), (String) dbObj.get(CONTENT_TYPE));
    }

}
