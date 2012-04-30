package controllers;

import models.ContentNode;
import models.ContentType;
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

    public static void index() {
        // TODO: only for the beginning: get out content nodes grouped by type

        List<ContentType> types = ContentType.findAll();

        Map<ContentType, List<ContentNode>> content = new HashMap<ContentType, List<ContentNode>>();
        for (ContentType type : types) {
            List<ContentNode> nodes = ContentNode.findByType(type.slug, 50);
            content.put(type, nodes);
        }
        
        render(content);
    }

}