package controllers.helper;

import controllers.callouts.Callouts;
import models.ContentNode;
import models.vo.SearchResult;
import play.mvc.Controller;
import play.templates.JavaExtensions;

/**
 * AJAX Controller serving as helper for the callout selection process.
 *
 * @author Niko Schmuck
 * @since 16.07.2012
 */
public class ContentNodeSelection extends Controller {

    /**
     * Search for content nodes with the given query term as part of title
     * and returns HTML fragment containing the resulting list.
     *
     * @param type the name of the content type to search for
     * @param query the search term to lookup
     * @param page is one-based
      */
    public static void searchByTitle(String type, String query, int page) {
        int pageSize = Callouts.getPageSize();
        int offset = (page-1) * pageSize;
        int nrPages = JavaExtensions.page(page, pageSize);
        SearchResult result = ContentNode.findByTypeAndTitleRaw(type, query, false, offset, pageSize);

        renderTemplate(String.format("Callouts/helper/%s_list.html", type), query, result, page, nrPages);
    }

}
