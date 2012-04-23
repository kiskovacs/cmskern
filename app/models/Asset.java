package models;

import com.mongodb.BasicDBObject;
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

    public static final int THUMBNAIL_WIDTH  = 75;
    public static final int THUMBNAIL_HEIGHT = 75;

    // ~~ Names of keys

    public static final String ID           = "_id";
    public static final String FILENAME     = "filename";
    public static final String UPLOAD_DATE  = "uploadDate";
    public static final String CONTENT_TYPE = "contentType";
    public static final String METADATA     = "metadata";

    /**
     * Signals, that this asset is a thumbnail
     */
    public static final String THUMBNAIL_FLAG = "is_thumbnail";

    /**
     * Name of the metadata key referring from the original to the related thumbnail
     */
    public static final String THUMB_REF = "thumb_ref";

    /**
     * Name of the metadata key Referring from the thumbnail to the original asset
     */
    public static final String ORIGINAL_REF = "orig_ref";

    // ~~

    public final String url;
    public final String thumbUrl;
    public final String filename;
    public final Date uploadDate;
    public final String contentType;

    // ~

    public Asset(String url, String thumbUrl, String filename, Date uploadDate, String contentType) {
        this.url = url;
        this.thumbUrl = thumbUrl;
        this.filename = filename;
        this.uploadDate = uploadDate;
        this.contentType = contentType;
    }

    // ~~

    public static Collection<Asset> findAllOriginals() {
        GridFS gfs = MongoDbUtils.getGridFS();
        DBCursor cursor = gfs.getFileList(new BasicDBObject(METADATA + '.' + THUMBNAIL_FLAG, new BasicDBObject("$exists", false)));
        Collection<Asset> assets = new ArrayList<Asset>();
        while (cursor.hasNext()) {
            assets.add(fromDBObject(cursor.next()));
        }
        return assets;
    }

    private static Asset fromDBObject(DBObject dbObj) {
        Map<String, Object> argMap = new HashMap<String, Object>(1);
        argMap.put("id", dbObj.get(ID));
        String url = Router.getFullUrl("Blobs.getOriginalById", argMap);

        DBObject metadata = (DBObject) dbObj.get(METADATA);
        String thumbUrl = null;
        if (metadata != null && metadata.get(THUMB_REF) != null) {
            argMap.put("id", metadata.get(THUMB_REF));
            thumbUrl = Router.getFullUrl("Blobs.getThumbById", argMap);
        }
        return new Asset(url, thumbUrl,
                         (String) dbObj.get(FILENAME), (Date) dbObj.get(UPLOAD_DATE), (String) dbObj.get(CONTENT_TYPE));
    }

}
