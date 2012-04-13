package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import jobs.Thumbnailer;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.MimeTypes;
import play.mvc.Controller;
import utils.MongoDbUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Access to files stored in the Blob store (currently making use
 * of GridFS provided by MongoDB).
 *
 * @author Niko Schmuck
 * @since 27.02.2012
 */
public class Blobs extends Controller {
    
    public static void upload(String qqfile) {
        Logger.info("Starting to upload %s ...", qqfile);

        GridFS gfs = MongoDbUtils.getGridFS();
        GridFSInputFile dbFile = gfs.createFile(request.body);
        dbFile.setFilename(qqfile);
        // guess content type from file name extension
        String contentType = MimeTypes.getContentType(qqfile);
        if (contentType == null || !contentType.startsWith("image/")) {
            // No valid content type
            renderJSON("{\"error\":\"Invalid content type: " + contentType + " \"}");
        } else {
            // Valid content type: try to store it
            dbFile.setContentType(contentType);
            dbFile.save();
            // Start to generate thumbnailing job asynchronously
            new Thumbnailer((ObjectId) dbFile.getId()).now();
            renderJSON("{\"success\":true}");
        }
    }

    public static void list() {
        Logger.info("Listing assets ...");

        GridFS gfs = MongoDbUtils.getGridFS();
        DBCursor cursor = gfs.getFileList();
        Collection<String> fileList = new ArrayList<String>();
        while (cursor.hasNext()) {
            DBObject dbObj = cursor.next();
            Logger.info(" * %s", dbObj);
            fileList.add((String) dbObj.get("filename"));
        }
        renderText(fileList);
    }

    public static void listAssets() {
        Logger.info("Listing assets ...");

        GridFS gfs = MongoDbUtils.getGridFS();
        DBCursor cursor = gfs.getFileList(new BasicDBObject("metadata.thumb_ref", new BasicDBObject("$exists", 1)));
        Collection<Asset> assets = new ArrayList<Asset>();
        while (cursor.hasNext()) {
            DBObject dbObj = cursor.next();
            Logger.info(" * found %s", dbObj);
            DBObject metadata = (DBObject) dbObj.get("metadata");
            String thumbUrl = "/blobs/" + metadata.get("thumb_ref");
            assets.add(new Asset((ObjectId) dbObj.get("_id"), thumbUrl, (String) dbObj.get("filename")));
        }
        render(assets);
    }

    public static void getByName(String name) {
        GridFS gfs = MongoDbUtils.getGridFS();
        GridFSDBFile dbFile = gfs.findOne(name);
        notFoundIfNull(dbFile);
        Logger.info("Deliver GridFS file: %s", dbFile.getFilename());

        response.contentType = dbFile.getContentType();
        renderBinary(dbFile.getInputStream());
    }

    public static void getById(String id) {
        GridFS gfs = MongoDbUtils.getGridFS();
        GridFSDBFile dbFile = gfs.findOne(new ObjectId(id));
        notFoundIfNull(dbFile);
        Logger.info("Deliver GridFS file: %s", dbFile.getFilename());

        response.contentType = dbFile.getContentType();
        renderBinary(dbFile.getInputStream());
    }

    // ~~ 

    private static class Asset {
        
        public final ObjectId id;
        public final String thumbUrl;
        public final String filename;
        
        public Asset(ObjectId id, String thumbUrl, String filename) {
            this.id = id;
            this.thumbUrl = thumbUrl;
            this.filename = filename;
        }
    }
}
