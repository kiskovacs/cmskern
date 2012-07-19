package controllers.helper;

import controllers.callouts.Callouts;
import models.ContentNode;
import models.vo.SearchResult;
import play.mvc.Controller;

/**
 * AJAX Controller serving as helper for the callout selection process.
 *
 * @author Niko Schmuck
 * @since 16.07.2012
 */
public class ContentNodeSelection extends Controller {

    // page is one-based
    public static void searchArticles(String query, int page) {
        int limit = Callouts.getPageSize();
        int offset = (page-1) * limit;
        SearchResult articles = ContentNode.findByTypeAndTitleRaw("article", query, false, offset, limit);
        renderTemplate("Callouts/helper/article_list.html", articles, page);
    }

}
