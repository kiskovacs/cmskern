package controllers;

import models.ContentNode;
import models.ContentType;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main entry point to cmskern web application.
 */
@With(Secure.class)
@Check("guest,editor,admin")
public class Application extends Controller {

    @Before
    static void addDefaults() {
        renderArgs.put("editorialTypes", ContentType.findByGroup("editorial"));
        renderArgs.put("siteTypes", ContentType.findByGroup("site"));
    }

    public static void index() {
        List<ContentType> types = ContentType.findAll();

        Map<ContentType, List<ContentNode>> content = new HashMap<ContentType, List<ContentNode>>();
        for (ContentType type : types) {
            List<ContentNode> nodes = ContentNode.findByType(type.slug, 50);
            content.put(type, nodes);
        }
        render(content);

    }

    public static void list(String typeName) {
        if (typeName != null) {
            List<ContentNode> contents = null;
            ContentType type = ContentType.findByName(typeName);
            contents = ContentNode.findByType(type.slug, 50);
            render(type, contents);
        } else {
            index();
        }
    }
}