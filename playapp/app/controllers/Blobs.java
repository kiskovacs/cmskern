package controllers;

import com.mongodb.gridfs.GridFSDBFile;
import models.Asset;
import models.vo.SearchResult;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import utils.MongoDbUtils;

import java.io.IOException;

/**
 * Access to files stored in the Blob store (in this implementation making use
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
        try {
            Asset asset = Asset.create(qqfile, request.body);
            renderJSON("{\"success\":true, \"id\": \"" + asset.id + "\"}");
        } catch (Exception e) {
            renderJSON("{\"success\":false, \"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Check("editor,admin")
    public static void listAssets(int page) {
        Logger.info("Listing assets ...");
        int pageSize = Application.getPageSize();
        if (page <= 0) {
            page = 1;
            Logger.debug("Page number set to default: %d", page);
        }
        int offset = (page-1) * pageSize;
        SearchResult<Asset> assets = Asset.findAll(offset, pageSize);
        String format = params.get("format");
        if (format != null && format.equalsIgnoreCase("JSON")) {
            renderJSON(assets);
        } else {
            int totalCount = assets.totalCount;
            render(assets, page, pageSize, totalCount);
        }
    }

    @Check("editor,admin")
    public static void listAssetsForTinyMCE() {  // TODO: integrate to listAssets with format=tinyMCE
        Logger.info("Listing assets (for TinyMCE) ...");
        SearchResult<Asset> assets = Asset.findAll(0, Application.getPageSize());
        render(assets);
    }

    public static void getBinaryById(String id) {
        Logger.info("Lookup asset by ID: %s", id);
        if (!MongoDbUtils.isValidId(id)) {
            notFound("Binary could not be found, invalid ID " + id);
        }
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