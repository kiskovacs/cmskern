package controllers;

import com.google.gson.Gson;
import com.mongodb.gridfs.GridFSDBFile;
import models.Asset;
import models.ContentNode;
import models.ContentType;
import models.vo.SearchResult;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import utils.MongoDbUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    public static void upload(String repository, String filename) throws IOException {
        Logger.info("Starting to upload %s ...", filename);
        try {
            String username = Security.connected();
            Asset asset = Asset.create(filename, username, request.body);
            renderJSON("{\"success\":true, \"id\": \"" + asset.id + "\"}");
        } catch (Exception e) {
            renderJSON("{\"success\":false, \"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Check("editor,admin")
    public static void uploadAndCreateContent(String repository, String filename, String type, String title, String fieldName) throws IOException {
        Logger.info("Starting to upload %s ...", filename);
        try {
            // (A) create asset
            String username = Security.connected();
            Asset asset = Asset.create(filename, username, request.body);
            // (B) check if content type exists
            ContentType contentType = ContentType.findByName(type);
            notFoundIfNull(contentType, "Unknown content type: " + type);
            // (C) create content node ... and associate asset
            Gson gson = new Gson();
            Map<String, String> vals = new HashMap<String, String>();
            vals.put("title", title);
            vals.put(fieldName, asset.id);
            ContentNode contentNode = new ContentNode(repository, contentType.name, gson.toJson(vals));
            contentNode.create(username);
            Logger.info("CREATED: " + contentNode.getJsonContent());

            renderJSON("{\"success\":true, \"id\": \"" + asset.id + "\"}");
        } catch (Exception e) {
            renderJSON("{\"success\":false, \"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Not in direct use, please better make use of
     */
    @Deprecated
    @Check("editor,admin")
    public static void listAssets(String repository, int page) {
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
    public static void listAssetsForTinyMCE(String repository) {  // TODO: integrate to listAssets with format=tinyMCE
        Logger.info("Listing assets (for TinyMCE) ...");
        SearchResult<Asset> assets = Asset.findAll(0, Application.getPageSize());
        render(assets);
    }

    public static void getBinaryById(String repository, String id) {
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

    public static void getByName(String repository, String name) {
        Logger.info("Lookup asset by name: %s", name);
        GridFSDBFile dbFile = MongoDbUtils.getFileByFilename(name);
        notFoundIfNull(dbFile, "Unable to retrieve GridFS file for name "+ name);
        Logger.info("    ... return GridFS file: %s", dbFile.getFilename());

        response.contentType = dbFile.getContentType();
        renderBinary(dbFile.getInputStream());
    }

}