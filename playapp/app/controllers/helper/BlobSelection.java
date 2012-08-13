package controllers.helper;

import controllers.callouts.Callouts;
import models.Asset;
import models.vo.SearchResult;
import play.Logger;
import play.mvc.Controller;
import play.templates.JavaExtensions;

/**
 * AJAX Controller serving as helper for the callout selection process.
 *
 * @author Niko Schmuck
 * @since 13.08.2012
 */
public class BlobSelection extends Controller {

    /**
     * Search for assets with the given query term as part of filename
     * and returns HTML fragment containing the resulting list.
     *
     * @param query the search term to lookup
     * @param page is one-based
      */
    public static void searchByFilename(String query, int page) {
        int pageSize = Callouts.getPageSize();
        if (page < 1) {
            page = 1;
        }
        int offset = (page-1) * pageSize;
        Logger.info("Querying for %s, page: %d ...", query, page);
        SearchResult result = Asset.findByFilename(query, false, offset, pageSize);
        int nrPages = JavaExtensions.page(result.totalCount, pageSize);

        renderTemplate("Callouts/helper/asset_list.html", query, result, page, nrPages);
    }

}
