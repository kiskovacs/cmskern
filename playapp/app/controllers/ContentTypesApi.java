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
     * @param name the name of the content type
     * @param contentId (optional) if content already exists (edit mode)
     */
    public static void asFormDescriptor(String name, Long contentId) throws IOException {
        ContentType type = ContentType.findByName(name);
        notFoundIfNull(type, "Unknown type name: " + name);

        if (contentId == null) {
            Logger.info("JSON edit form for type [%s] filled with content ID %s", name, contentId);
            ContentNode contentNode = ContentNode.findById(contentId);
            notFoundIfNull(contentNode, "Unknown node ID: " + contentId);
            JsonNode jsonRepresentation = JsonUtils.enrich(type, contentNode);
            renderJSON(jsonRepresentation.toString());
        } else {
            Logger.info("Blank JSON edit form for type [%s]", name);
            renderJSON(type.jsonSchema);
        }
    }

    /**
     * Displays list of available content types.
     */
    public static void list() {
        List<ContentType> types = ContentType.findAll();
        // TODO: use sort order
        render(types);
    }

    /**
     * Displays form to allow to create/edit a content type
     */
    public static void edit(String id) {
        ContentType type = ContentType.findById(id);
        notFoundIfNull(type, "Unknown type ID: " + id);

        render(type);
    }


    // ~~ REST API (for admins)

    @Check("admin")
    public static void create(String name, String displayName, String body) {
        Logger.info("Going to create type: %s ... ", name);

        ContentType type = new ContentType(name, displayName, body);
        type.save();
        response.status = Http.StatusCode.CREATED;
        response.setHeader("Location", request.getBase() + "/schema/" + type.getId());
    }
    
    public static void get(String id) {
        ContentType type = ContentType.findById(id);
        notFoundIfNull(type, "Unknown type ID: " + id);

        Logger.info("Retrieved: %s", type.name);
        renderJSON(type.jsonSchema);
    }

    @Check("admin")
    public static void update(String id, String name, String body) {
        ContentType type = ContentType.findById(id);
        notFoundIfNull(type, "Unknown type ID: " + id);
        Logger.info("Going to update %s ...", id);
        // check that body contains valid JSON
        com.mongodb.util.JSON.parse(body); // TODO: improve validation
        type.jsonSchema = body;
        type.name = name;
        type.save();
        // TODO: return HTTP status
        render(type);
    }

    @Check("admin")
    public static void delete(String id) {
        // TODO: this is a very critical operation, add more serious checks?
        ContentType type = ContentType.findById(id);
        notFoundIfNull(type, "Unknown type ID: " + id);
        Logger.info("Going to delete %s ...", id);
        type.delete();
        // TODO: return HTTP status
    }

}