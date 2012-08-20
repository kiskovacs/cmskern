package controllers;

import models.ContentNode;
import models.ContentType;
import models.vo.SearchResult;
import play.Play;
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

    public static int getPageSize() {
        return Integer.parseInt(Play.configuration.getProperty("cmskern.editorial.pagesize", "50"));
    }

    // ~~

    @Before
    static void addDefaults() {
        renderArgs.put("editorialTypes", ContentType.findByGroup("editorial"));
        renderArgs.put("siteTypes", ContentType.findByGroup("site"));
    }

    // TODO: only for the time being
    public static void index() {
        List<ContentType> types = ContentType.findAll();
        // get some content lately modified to start with
        Map<ContentType, SearchResult<ContentNode>> content = new HashMap<ContentType, SearchResult<ContentNode>>();
        for (ContentType type : types) {
            SearchResult<ContentNode> nodes = ContentNode.findByType(type.name, 0, getPageSize());
            content.put(type, nodes);
        }
        render(content);
    }

}