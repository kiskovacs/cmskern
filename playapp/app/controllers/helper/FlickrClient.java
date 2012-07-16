package controllers.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.WS;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper functionality to query Flickr, used as an example from call-out dialog boxes.
 *
 * @author Niko Schmuck
 * @since 20.02.2012
 */

// Alternative to Flickr:
// https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=query

public class FlickrClient extends Controller {

    public final static String BASE_URL   = "http://api.flickr.com/services/rest/";
    public final static String API_KEY    = "614389d986e3e62952b0891d0c2e3aa1";
    public final static String FORMAT     = "json";
    public final static String METHOD     = "flickr.photos.search";
    public final static int    MAX_PHOTOS = 10;


    public static void search(String query) {
        if (StringUtils.isEmpty(query)) {
            error(406, "No query parameter specified");
        }

        Logger.info("Going to search for %s on flickr ...", query);
        WS.WSRequest url = WS.url(BASE_URL + "?api_key=%s&format=%s&method=%s&text=%s&per_page=%s", API_KEY,
                                  FORMAT, METHOD, query, ""+ MAX_PHOTOS);
        Logger.info("Going to request: %s", url.url);
        WS.HttpResponse result = url.get();

        // Remove outer JavaScript Function name
        String pureResponse = result.getString().replaceAll("jsonFlickrApi\\(([^<]*)\\)", "$1");
        JsonParser parser = new JsonParser();
        JsonElement jsonRoot = parser.parse(pureResponse);
        JsonObject photosRoot = jsonRoot.getAsJsonObject().get("photos").getAsJsonObject();
        JsonArray photoList = photosRoot.getAsJsonArray("photo");

        List<Photo> photos = new ArrayList<Photo>();
        for (JsonElement photo : photoList) {
            JsonObject p = photo.getAsJsonObject();
            // Logger.info(" * " + photo);
            String flickrUrl = String.format("http://static.flickr.com/%s/%s_%s_s.jpg", p.get("server").getAsString(),
                    p.get("id").getAsString(), p.get("secret").getAsString());
            photos.add(new Photo(p.get("id").getAsString(), p.get("title").getAsString(), flickrUrl));
        }
        Logger.info("Retrieved results from flickr and turned into %d photos", photos.size());
        
        renderTemplate("Callouts/helper/flickr_photo_list.html", photos);
    }


    /**
     * Minimal representation of a flickr photo.
     */
    static class Photo {
        String id;
        String title;
        String url;

        public Photo(String id, String title, String flickrUrl) {
            this.id = id;
            this.title = title;
            this.url = flickrUrl;
        }
    }

}
