package unit;

import com.google.gson.Gson;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;
import utils.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Niko Schmuck
 * @since 24.04.2012
 */
public class JSONDeserializeTest extends UnitTest {

    @Test
    public void simpleMap() {
        // prepare test object
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key1", "value1");
        Map<String, Object> subMap = new LinkedHashMap<String, Object>();
        subMap.put("subKeyA", "valueA");
        map.put("key2", subMap);
        map.put("key3", "value3");

        Gson gson = new Gson();

        // Serialize to JSON
        String json = gson.toJson(map);
        Logger.info("Serialized to: %s", json);
        assertThat(json, containsString("\"key2\":{\"subKeyA\":\"valueA\"}"));

        // Deserialize JSON to Map
        Map<String, Object> map2 = JsonUtils.convertToMap(json);
        Logger.info("Deserialized to: %s", map2);
        assertNotNull(map2.get("key2"));
        Map<String, Object> subMap2 = (Map<String, Object>) map2.get("key2");
        assertEquals("valueA", subMap2.get("subKeyA"));
    }

}