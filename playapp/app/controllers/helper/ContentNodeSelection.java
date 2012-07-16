package controllers.helper;

import com.mongodb.DBObject;
import models.ContentNode;
import play.mvc.Controller;

import java.util.List;

/**
 * @author Niko Schmuck
 * @since 16.07.2012
 */
public class ContentNodeSelection extends Controller {

    public static void searchArticles(String query, int offset, int limit) {
        List<DBObject> articles = ContentNode.findByTypeAndTitleRaw("article", query, false, offset, limit);
        renderTemplate("Callouts/helper/article_list.html", articles);
    }

}
