package controllers;

import models.ContentNode;
import models.ContentType;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import utils.JsonUtils;

import java.io.IOException;
import java.util.List;

@With(Secure.class)
public class ContentTypesApi extends Controller {

    /**
     * Returns the JSON representation for this content type
     * to be used inside content creation / edit form.
     *
     * @param type the name of the content type
     * @param contentId (optional) if content already exists (edit mode)
     */
    public static void asFormDescriptor(String repository, String type, Long contentId) throws IOException {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(type, "Unknown type name: " + type);

        if (contentId == null) {
            Logger.info("JSON edit form for type [%s] filled with content ID %s", type, contentId);
            ContentNode contentNode = ContentNode.findById(contentId);
            notFoundIfNull(contentNode, "Unknown node ID: " + contentId);
            JsonNode jsonRepresentation = JsonUtils.enrich(contentType, contentNode);
            renderJSON(jsonRepresentation.toString());
        } else {
            Logger.info("Blank JSON edit form for type [%s]", type);
            renderJSON(contentType.jsonSchema);
        }
    }

    /**
     * Displays list of available content types.
     */
    public static void list(String repository) {
        List<ContentType> types = ContentType.findAll(); // TODO: use sort order and limit
        render(types);
    }

    /**
     * Displays form to allow to create/edit a content type
     */
    public static void edit(String repository, String type) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(type, "Unknown type name: " + type);

        render(contentType);
    }


    // ~~ REST API (for admins)

    @Check("admin")
    public static void create(String repository, String name, String displayName, String body) {
        Logger.info("Going to create type: %s ... ", name);

        ContentType contentType = new ContentType(name, displayName, body);
        contentType._save();
        response.status = Http.StatusCode.CREATED;
        response.setHeader("Location", request.getBase() + "/schema/" + contentType._key());
    }
    
    public static void get(String repository, String type) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(type, "Unknown type name: " + type);

        Logger.info("Retrieved: %s", type);
        renderJSON(contentType.jsonSchema);
    }

    @Check("admin")
    public static void update(String repository, String type, String name, String body) {
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(type, "Unknown type name: " + type);
        Logger.info("Going to update %s ...", type);
        // check that body contains valid JSON
        com.mongodb.util.JSON.parse(body); // TODO: improve validation
        contentType.jsonSchema = body;
        contentType.name = name;
        contentType._save();

        render(contentType);
    }

    @Check("admin")
    public static void delete(String repository, String type) {
        // TODO: this is a very critical operation, add check if still in use?
        ContentType contentType = ContentType.findByName(type);
        notFoundIfNull(type, "Unknown type name: " + type);
        Logger.info("Going to delete %s ...", type);
        contentType._delete();

        ok();
    }

}