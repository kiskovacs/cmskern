package utils;

import models.ContentNode;
import models.ContentType;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Niko Schmuck
 * @since 29.01.2012
 */
public class JsonUtils {

    public static JsonNode enrich(ContentType contentType, ContentNode contentNode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.readValue(contentType.jsonSchema, ObjectNode.class);

        Map<String, Object> values = mapper.readValue(contentNode.getJsonContent(), Map.class);

        enrichFormWithValues(root, 0, values);
        return root;
    }

    /**
     * Traverse over the given JSON root node and if a form field
     * is found add a value as specified by the given value map.
     */
    public static void enrichFormWithValues(JsonNode node, int level, final Map<String, Object> values) {
        Iterator<JsonNode> iter = node.getElements();
        while (iter.hasNext()) {
            JsonNode current = iter.next();
            if (current.has("placeholder")) {
                String key = current.get("name").asText();
                if (values != null && values.get(key) != null) {
                    String value = values.get(key).toString();
                    Logger.info(" *** (Level %d) %s -> fill '%s' in placeholder", level, key, value);
                    ((ObjectNode) current).put("value", value);
                }
            }
            enrichFormWithValues(current, level + 1, values);
        }
    }

}
