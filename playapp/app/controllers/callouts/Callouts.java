package controllers.callouts;

import models.ContentNode;
import play.Logger;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.List;
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

        // Find object types to refer to
        // TODO: this could also be handled by schema definition or made a bit more generic
        if (name.contains("internal/article_")
                || name.contains("internal/test_")) {
            // ~~ Convert to ContentNode and to generic Map structure
            //List<ContentNode> articles = ContentNode.findByType("article", 20);
            //model.put("articles", ContentNode.convertToMap(articles));
            // ~~ RAW access
            model.put("articles", ContentNode.findByTypeRaw("article", 20));  // TODO: improve by using paging
        }
        else if (name.contains("/image_")) {
            model.put("images", ContentNode.findByTypeRaw("image", 20));  // TODO: improve by using paging
        }
        else if (name.contains("/imageGallery_")) {
            model.put("imageGalleries", ContentNode.findByTypeRaw("imageGallery", 20));  // TODO: improve by using paging
        }
        else if (name.contains("/sidebar_")) {
            model.put("sidebars", ContentNode.findByTypeRaw("sidebar", 20));  // TODO: improve by using paging
        }
        else if (name.contains("/node_")) {
            List dl = ContentNode.findByTypeRaw("node", 20);
            model.put("nodes",dl );  // TODO: improve by using paging
        }

        String[] fieldnames = params.getAll("update_fields[]");
        Logger.info("fieldnames: %s" , fieldnames);

        model.put("fieldnames", fieldnames);
        // Add parameters given to model, introduce simple name map
        for (Map.Entry<String, String> param : params.allSimple().entrySet()) {

            String key = param.getKey();
            if (key.startsWith("src_prop_")) {
                Logger.info("Add to model: %s -> %s", key, param.getValue());
                model.put(key, param.getValue());
            }
        }

        // Figure out proper template as defined in schema
        String templateName = "Callouts/" + name + ".html";
        Logger.info("Going to render %s with params: %s ...", templateName, params.allSimple());

        renderTemplate(templateName, model);
    }
    
}
