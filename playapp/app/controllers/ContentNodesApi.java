package controllers;

import com.mongodb.DBObject;
import models.ContentNode;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

@With(Secure.class)
public class ContentNodesApi extends Controller {

    public static void getBody(String type, Long id) {
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown content ID: " + id);
        Logger.info("Deliver body with ID: %s (%s)", contentNode.getId(), type);
        renderJSON(contentNode.getJsonContent());
    }

    public static void getFull(String type, Long id) {
        DBObject obj = ContentNode.findByIdRaw(id);
        notFoundIfNull(obj, "Unknown content ID: " + id);
        Logger.info("Deliver raw %s for ID: %s ...", type, id);
        renderJSON(obj.toString());
    }

    @Check("editor,admin")
    public static void create(String type, String body) {
        Logger.debug("Going to create: %s ... ", body);
        ContentNode contentNode = new ContentNode(type, body);
        String username = Security.connected();
        contentNode.create(username);
        // deliver back location of new content resource
        Logger.info("Created new content node with ID: %s", contentNode.getId());
        response.status = Http.StatusCode.CREATED;
        response.setHeader("Location", request.getBase() + '/' + type + '/' + contentNode.getId());
        renderJSON("{\"id\": \"" + contentNode.getId() + "\"}");
    }

    @Check("editor,admin")
    public static void update(String type, Long id, String body) {
        // TODO: filter also by type?  Check if already exists?
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown content ID: " + id);
        Logger.info("Going to update %s with ID %s ...", type, id);
        String username = Security.connected();
        contentNode.update(username, body);
        renderJSON("{\"id\": \"" + contentNode.getId() + "\"}");
    }

    @Check("admin")
    public static void delete(String type, Long id) {
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown content ID: " + id);
        Logger.info("Going to delete %s with id %s ...", type, id);
        contentNode.delete();
    }

}