package controllers.callouts;

import models.ContentNode;
import models.vo.RefValue;
import play.Logger;
import play.mvc.Controller;

import java.util.Arrays;
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
            List nodes = ContentNode.findByTypeRaw("node", 20);
            model.put("nodes", nodes);  // TODO: improve by using paging
        }

        // Prepare names of properties which should be updated by this callout
        String[] srcPropNames    = params.getAll("src_properties[]");
        String[] types           = params.getAll("src_types[]");
        String[] values          = params.getAll("values[]");
        String[] targetPropNames = params.getAll("update_fields[]");

        // values kann leer sein
        if (values == null) {
            values = new String[srcPropNames.length];
        }
        Logger.info("types: %s", Arrays.asList(types));
        Logger.info("values: %s", Arrays.asList(values));
        Logger.info("targetPropNames: %s", Arrays.asList(targetPropNames));
        Logger.info("srcPropNames: %s", Arrays.asList(srcPropNames));
        // TODO: war model.put("fieldnames", targetPropNames);

        // build field map to allow referencing from template
        Map<String, RefValue> fields = new HashMap<String, RefValue>();
        for (int i = 0; i < srcPropNames.length; i++) {
            fields.put(srcPropNames[i], new RefValue(targetPropNames[i], types[i], values[i]));
        }
        model.put("fields", fields);
        Logger.info("    fields: " + fields);

        // Figure out proper template as defined in schema
        String templateName = "Callouts/" + name + ".html";
        Logger.info("Going to render %s ...", templateName);

        renderTemplate(templateName, model);
    }
    
}
