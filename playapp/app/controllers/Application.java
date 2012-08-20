package controllers;

import models.ContentType;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;

import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point to cmskern web application.
 */
@With(Secure.class)
@Check("guest,editor,admin")
public class Application extends Controller {

    public static int getPageSize() {
        return Integer.parseInt(Play.configuration.getProperty("cmskern.editorial.pagesize", "50"));
    }

    // ~~

    @Before
    static void addDefaults() {
        // prepare content types and make them available for the navigation bar
        renderArgs.put("editorialTypes", ContentType.findByGroup("editorial"));
        renderArgs.put("siteTypes", ContentType.findByGroup("site"));
    }

    // Redirect to article list
    public static void index() {
        Map<String, Object> argMap = new HashMap<String, Object>(1);
        argMap.put("type", "article");
        String url = Router.getFullUrl("ContentNodes.list", argMap);
        redirect(url);
    }

}