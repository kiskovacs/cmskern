package controllers;

import models.ContentNode;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;

public class ContentApi extends Controller {

    public static void get(String type, String id) {
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown content ID: " + id);
        Logger.info("Retrieved %s with ID: %s", type, contentNode.getId());
        renderJSON(contentNode.getAsJson());
    }

    public static void create(String type, String body) {
        Logger.info("Going to create %s ... ", body);
        ContentNode contentNode = new ContentNode(type, body);
        contentNode.create();
        // deliver back location of new content resource
        Logger.info("Created new content node with ID: %s", contentNode.getId());
        response.status = Http.StatusCode.CREATED;
        response.setHeader("Location", request.getBase() + '/' + type + '/' + contentNode.getId());
        renderJSON("{\"id\": \"" + contentNode.getId() + "\"}");
    }

    public static void update(String type, String id, String body) {
        // TODO: filter also by type?  Check if already exists?
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown content ID: " + id);
        Logger.info("Going to update %s with ID %s ...", type, id);
        contentNode.update(body);
    }

    public static void delete(String type, String id) {
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown content ID: " + id);
        Logger.info("Going to delete %s ...", id);
        contentNode.delete();
    }

}