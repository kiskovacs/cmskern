package unit;

import com.google.gson.Gson;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;

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
        // Results in {"key1":"value1","key2":{"subKeyA":"valueA"},"key3":"value3"}
        assertThat(json, containsString("\"key2\":{\"subKeyA\":\"valueA\"}"));
    }

}