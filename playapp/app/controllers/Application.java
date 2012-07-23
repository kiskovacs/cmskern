package controllers;

import controllers.callouts.Callouts;
import models.ContentNode;
import models.ContentType;
import models.vo.SearchResult;
import play.Logger;
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

    public static void index() {
        List<ContentType> types = ContentType.findAll();
        // get some content lately modified to start with
        Map<ContentType, SearchResult<ContentNode>> content = new HashMap<ContentType, SearchResult<ContentNode>>();
        for (ContentType type : types) {
            SearchResult<ContentNode> nodes = ContentNode.findByType(type.slug, 0, getPageSize());
            content.put(type, nodes);
        }
        render(content);
    }

    public static void list(String typeName, int page) {
        ContentType type = ContentType.findByName(typeName);
        notFoundIfNull(type);

        if (page <= 0) {
            page = 1;
            Logger.info("Page number set to default");
        }
        int limit = Callouts.getPageSize();
        int offset = (page-1) * limit;
        SearchResult<ContentNode> contents = ContentNode.findByType(type.slug, offset, limit);
        render(type, contents, page);
    }

}