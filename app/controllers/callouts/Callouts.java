package controllers.callouts;

import models.ContentNode;
import play.Logger;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple controller to render "call-out" template
 * as specified by the content type definition.
 * Might be a external call-out as well in future releases.
 *
 * @author Niko Schmuck
 * @since 20.02.2012
 */
public class Callouts extends Controller {
    
    public static void get(String name) {
        Map<String, Object> model = new HashMap<String, Object>();
        // ~~
        if (name.equalsIgnoreCase("internal/article_reference")) {
            // ~~ Convert to ContentNode and to generic Map structure
            //List<ContentNode> articles = ContentNode.findByType("article", 20);
            //model.put("articles", ContentNode.convertToMap(articles));
            // ~~ RAW access
            model.put("articles", ContentNode.findByTypeRaw("article", 20));
        }
        // ~~
        String templateName = "Callouts/" + name + ".html";
        Logger.info("Going to render %s ...", templateName);
        renderTemplate(templateName, model);
    }
    
}
