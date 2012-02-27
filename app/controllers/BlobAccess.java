package controllers;

import com.google.code.morphia.Datastore;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import play.Logger;
import play.Play;
import play.libs.MimeTypes;
import play.modules.morphia.MorphiaPlugin;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Access to files stored in the Blob store (currently making use
 * of GridFS provided by MongoDB).
 *
 * @author Niko Schmuck
 * @since 27.02.2012
 */
public class BlobAccess extends Controller {
    
    public static void upload(String qqfile) {
        Logger.info("Starting to upload %s ...", qqfile);

        GridFS gfs = getGridFS();
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
            renderJSON("{\"success\":true}");
        }
    }

    public static void list() {
        Logger.info("Listing assets ...");

        GridFS gfs = getGridFS();
        DBCursor cursor = gfs.getFileList();
        Collection<String> fileList = new ArrayList<String>();
        while (cursor.hasNext()) {
            DBObject dbObj = cursor.next();
            Logger.info(" * %s", dbObj);
            fileList.add((String) dbObj.get("filename"));
        }
        renderText(fileList);
    }
    
    public static void getByName(String name) {
        GridFS gfs = getGridFS();
        GridFSDBFile dbFile = gfs.findOne(name);
        notFoundIfNull(dbFile);
        Logger.info("Deliver GridFS file: %s", dbFile.getFilename());

        response.contentType = dbFile.getContentType();
        renderBinary(dbFile.getInputStream());
    }

    // TODO: we probably need also a getById retriever ...

    // ~~ 
    
    private static GridFS getGridFS() {
        Datastore datastore = MorphiaPlugin.ds();
        DB db = datastore.getDB();
        // name of bucket to store assets in
        String collectionName = Play.configuration.getProperty("morphia.collection.upload", "uploads");
        return new GridFS(db, collectionName);
    }
    
}
