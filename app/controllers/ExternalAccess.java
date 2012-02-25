package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import play.Logger;
import play.libs.WS;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niko Schmuck
 * @since 20.02.2012
 */
public class ExternalAccess extends Controller {

    public static void searchFlickr(String query) {
        // Alternative:
        // https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=query

        String baseUrl = "http://api.flickr.com/services/rest/";
        String api_key = "614389d986e3e62952b0891d0c2e3aa1";
        String format = "json";
        String method = "flickr.photos.search";
        WS.WSRequest url = WS.url(baseUrl + "?api_key=%s&format=%s&method=%s&text=%s&per_page=5", api_key, format, method, query);
        Logger.info("Going to request: " + url.url);
        WS.HttpResponse result = url.get();
        // Remove outer function name
        String pureResponse = result.getString().replaceAll("jsonFlickrApi\\(([^<]*)\\)", "$1");
        JsonParser parser = new JsonParser();
        JsonElement jsonRoot = parser.parse(pureResponse);
        JsonObject photosRoot = jsonRoot.getAsJsonObject().get("photos").getAsJsonObject();
        JsonArray photoList = photosRoot.getAsJsonArray("photo");

        List<Photo> photos = new ArrayList<Photo>();
        for (JsonElement photo : photoList) {
            JsonObject p = photo.getAsJsonObject();
            Logger.info(" * " + photo);
            String flickrUrl = String.format("http://static.flickr.com/%s/%s_%s_s.jpg", p.get("server").getAsString(),
                    p.get("id").getAsString(), p.get("secret").getAsString());
            photos.add(new Photo(p.get("id").getAsString(), p.get("title").getAsString(), flickrUrl));
        }
        
        renderJSON(photos);
    }


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
