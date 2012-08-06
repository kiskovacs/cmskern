package controllers.callouts;

import models.ContentNode;
import models.vo.RefValue;
import play.Logger;
import play.Play;
import play.mvc.Controller;

import java.util.Arrays;
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

    public static int getPageSize() {
        return Integer.parseInt(Play.configuration.getProperty("cmskern.callout.pagesize", "10"));
    }

    // ~~

    public static void get(String name) {
        Map<String, Object> model = new HashMap<String, Object>();

        // Find object types to refer to
        // TODO: this could also be handled by schema definition or made a bit more generic
        if (   name.contains("internal/article_")
            || name.contains("internal/test_")) {
            // ~~ RAW access
            model.put("articles", ContentNode.findByTypeRaw("article", getPageSize()));  // TODO: improve by using paging
        }
        else if (name.contains("/image_")) {
            model.put("images", ContentNode.findByTypeRaw("image", getPageSize()));  // TODO: improve by using paging
        }
        else if (name.contains("/imageGallery_")) {
            model.put("imageGalleries", ContentNode.findByTypeRaw("imageGallery", getPageSize()));  // TODO: improve by using paging
        }
        else if (name.contains("/sidebar_")) {
            model.put("sidebars", ContentNode.findByTypeRaw("sidebar", getPageSize()));  // TODO: improve by using paging
        }
        else if (name.contains("/node_")) {
            model.put("nodes", ContentNode.findByTypeRaw("node", getPageSize()));  // TODO: improve by using paging
        }

        // Prepare names of properties which should be updated by this callout
        String[] srcPropNames    = params.getAll("src_properties[]");
        String[] values          = params.getAll("values[]");

        Logger.debug("values %s", values);
        if (values == null ) {
            values  = new String[]{};
        }

        // TODO: @syk wozu das?
        String[] tmpValues = new String[srcPropNames.length];
        /*
        for (int i =0; i < tmpValues.length; i++) {
            tmpValues[i] = "";
        }
        */
        System.arraycopy(values, 0, tmpValues, 0, values.length);
        values = tmpValues;

        String[] targetPropNames = params.getAll("target_properties[]");

        Logger.info("  srcPropNames:    %s", Arrays.asList(srcPropNames));
        Logger.info("  targetPropNames: %s", Arrays.asList(targetPropNames));
        Logger.info("  values:          %s", Arrays.asList(values));

        // build field map to allow referencing from template
        Map<String, RefValue> fields = new HashMap<String, RefValue>();
        for (int i = 0; i < srcPropNames.length; i++) {
            fields.put(srcPropNames[i], new RefValue(targetPropNames[i], values[i]));
        }
        model.put("fields", fields);
        Logger.info("  --> fields:      %s", fields);

        // Figure out proper template as defined in schema
        String templateName = "Callouts/" + name + ".html";
        Logger.info("Going to render %s ...", templateName);

        renderTemplate(templateName, model);
    }
    
}
