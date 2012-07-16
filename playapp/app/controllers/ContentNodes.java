package controllers;

import models.ContentNode;
import models.ContentType;
import models.vo.IdTitle;
import play.mvc.With;

import java.util.List;

/**
 * Access to content nodes: the central entities of cmskern.
 *
 * @author Niko Schmuck
 * @since 23.01.2012
 */
@With(Secure.class)
public class ContentNodes extends Application {

    public static void test() {
        render();
    }

    @Check("editor,admin")
    public static void blank(String type) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);

        render(contentType);
    }

    @Check("editor,admin")
    public static void edit(String type, String id) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);

        render(contentNode, contentType);
    }

    @Check("admin")
    public static void delete(String type, String id) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);

        contentNode.delete();
        redirect("Application.index");
    }

    @Check("editor,admin")
    public static void versions(String type, String id) {
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);
        List<ContentNode> versions = ContentNode.findVersionsForId(id);
        render(contentNode, versions);
    }

    public static void list(String type) {
        List<ContentNode> nodes = ContentNode.findByType(type, 50);
        render(nodes);
    }

    /**
     * Returns JSON with simple data structure (id and title) of
     * content nodes which titles do match with the specified query string.
     */
    public static void search(String type, String q, int limit) {
        List<IdTitle> nodes = ContentNode.findByTypeAndTitleMinimal(type, q, false, limit);
        renderJSON(nodes);
    }

}
