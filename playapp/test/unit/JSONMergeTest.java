package unit;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;
import utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JSONMergeTest extends UnitTest {

    @Test
    public void mergeResultWithForm() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // (A) read in from definition
        File formFile = new File("test/data/form-definition.json");
        ObjectNode root = mapper.readValue(formFile, ObjectNode.class);

        // (B) read in form values
        File valFile = new File("test/data/form-values.json");
        //JsonNode valRoot = mapper.readValue(valFile, JsonNode.class);
        Map<String, Object> values = mapper.readValue(valFile, Map.class);

        // loop over form and put values them into form
        JsonUtils.enrichFormWithValues(root, 0, values);
        Logger.info("*** result %s", root);
        assertEquals("foo", root.path("elements").get(1).get("value").getTextValue());
        assertEquals("bar", root.path("elements").get(2).get("value").getTextValue());
    }

    /* Alternative:
    Map<String, String>[] maps = mapper.readValue(new File("input.json"), Map[].class);
    for (Map<String, String> map : maps)
    {
      for (Map.Entry<String, String> entry : map.entrySet())
      {
        System.out.println(entry.getValue());
      }
    }
    */
    
}
