package controllers;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import models.Asset;
import play.Logger;
import play.libs.MimeTypes;
import play.mvc.Controller;
import play.mvc.With;
import utils.MongoDbUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * Access to files stored in the Blob store (currently making use
 * of GridFS provided by MongoDB).
 *
 * @author Niko Schmuck
 * @since 27.02.2012
 */
@With(Secure.class)
public class Blobs extends Controller {

    @Check("editor,admin")
    public static void upload(String qqfile) throws IOException {
        Logger.info("Starting to upload %s ...", qqfile);

        GridFS gfs = MongoDbUtils.getGridFS();
        GridFSInputFile dbFile = gfs.createFile(request.body);
        dbFile.setFilename(qqfile);
        // guess content type from file name extension
        String contentType = MimeTypes.getContentType(qqfile);
        if (contentType == null || !contentType.startsWith("image/")) {
            // No valid content type (TODO: could verify against list of allowed content types)
            renderJSON("{\"error\":\"Invalid content type: " + contentType + " \"}");
        } else {
            // Valid content type: try to store it
            dbFile.setContentType(contentType);
            dbFile.save();
            renderJSON("{\"success\":true}");
        }
    }

    @Check("editor,admin")
    public static void list() {
        Logger.info("Listing all assets (JSON) ...");
        Collection<Asset> assets = Asset.findAll();
        renderJSON(assets);
    }

    @Check("editor,admin")
    public static void listAssets() {
        Logger.info("Listing assets ...");
        Collection<Asset> assets = Asset.findAll();
        render(assets);
    }

    @Check("editor,admin")
    public static void listAssetsForTinyMCE() {
        Logger.info("Listing assets (for TinyMCE) ...");
        Collection<Asset> assets = Asset.findAll();
        render(assets);
    }

    // TODO: rename
    public static void getFullsizeById(String id) {
        Logger.info("Lookup asset by ID: %s", id);
        GridFSDBFile dbFile = MongoDbUtils.getFileById(id);
        notFoundIfNull(dbFile, "Unable to retrieve GridFS file for asset "+ id);
        Logger.info("    ... return GridFS file: %s", dbFile.getFilename());

        response.contentType = dbFile.getContentType();
        renderBinary(dbFile.getInputStream());
    }

    public static void getByName(String name) {
        Logger.info("Lookup asset by name: %s", name);
        GridFSDBFile dbFile = MongoDbUtils.getFileByFilename(name);
        notFoundIfNull(dbFile, "Unable to retrieve GridFS file for name "+ name);
        Logger.info("    ... return GridFS file: %s", dbFile.getFilename());

        response.contentType = dbFile.getContentType();
        renderBinary(dbFile.getInputStream());
    }

}