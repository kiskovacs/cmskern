package controllers;

import models.ContentNode;
import models.ContentType;
import play.mvc.Controller;

import java.util.List;

/**
 * @author Niko Schmuck
 * @since 23.01.2012
 */
public class ContentNodes extends Controller {

    public static void blank(String type) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);

        render(contentType);
    }

    public static void edit(String type, String id) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);

        render(contentNode, contentType);
    }

    public static void delete(String type, String id) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);

        contentNode.delete();
        redirect("Application.index");
    }

    public static void list(String type) {
        List<ContentNode> nodes = ContentNode.findByType(type);
        render(nodes);
    }

}
