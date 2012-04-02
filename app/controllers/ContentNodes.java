package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.Restrictions;
import controllers.deadbolt.RoleHolderPresent;
import models.ContentNode;
import models.ContentType;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * @author Niko Schmuck
 * @since 23.01.2012
 */
@With(Deadbolt.class)
@RoleHolderPresent
public class ContentNodes extends Controller {

    @Restrictions({@Restrict("editor"), @Restrict("admin")})
    public static void blank(String type) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);

        render(contentType);
    }

    @Restrictions({@Restrict("editor"), @Restrict("admin")})
    public static void edit(String type, String id) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);

        render(contentNode, contentType);
    }

    @Restrict({"admin"})
    public static void delete(String type, String id) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(contentType, "Unknown type: " + type);
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);

        contentNode.delete();
        redirect("Application.index");
    }

    public static void showVersions(String type, String id) {
        ContentNode contentNode = ContentNode.findById(id);
        notFoundIfNull(contentNode, "Unknown node ID: " + id);
        List<ContentNode> versions = ContentNode.findVersionsForId(id);
        render(contentNode, versions);
    }


    public static void list(String type) {
        List<ContentNode> nodes = ContentNode.findByType(type, 50);
        render(nodes);
    }

}
