package controllers;

import models.ContentNode;
import models.ContentType;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main entry point to cmskern web application.
 */
public class Application extends Controller {

    public static void index() {
        // TODO: only for demo purposes get out content nodes grouped by type

        List<ContentType> types = ContentType.findAll();

        Map<ContentType, List<ContentNode>> content = new HashMap<ContentType, List<ContentNode>>();
        for (ContentType type : types) {
            List<ContentNode> nodes = ContentNode.findByType(type.slug, 50);
            content.put(type, nodes);
        }
        
        render(content);
    }

    public static void test() {
        render();
    }

}