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

        // Find object types to refer to (TODO: this could also be handled by schema definition)
        if (name.equalsIgnoreCase("internal/article_reference")) {
            // ~~ Convert to ContentNode and to generic Map structure
            //List<ContentNode> articles = ContentNode.findByType("article", 20);
            //model.put("articles", ContentNode.convertToMap(articles));
            // ~~ RAW access
            model.put("articles", ContentNode.findByTypeRaw("article", 20));  // TODO: improve by using paging
        }

        // Add parameters given to model, introduce simple name map
        for (Map.Entry<String, String> param : params.allSimple().entrySet()) {
            String key = param.getKey();
            if (key.startsWith("contentNode")) {
                String simpleName = key.substring(key.lastIndexOf('.') + 1);
                Map<String, String> map = new HashMap<String, String>();
                map.put("fieldname", key);
                map.put("value", param.getValue());
                model.put(simpleName, map);
            }
        }

        // Figure out proper template as defined in schema
        String templateName = "Callouts/" + name + ".html";
        Logger.info("Going to render %s with params: %s ...", templateName, params.allSimple());

        renderTemplate(templateName, model);
    }
    
}
