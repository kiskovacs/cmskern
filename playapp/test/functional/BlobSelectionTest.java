package functional;

import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.test.MorphiaFixtures;
import utils.MongoDbUtils;

public class BlobSelectionTest extends FunctionalTest {

    @Before
    public void setUpData() {
        MorphiaFixtures.deleteDatabase();
        MongoDbUtils.dropFiles();
        MorphiaFixtures.loadModels("initial-assets.yml");
    }

    @Test
    public void testSearchFilename() {
        Response response = GET("/helper/BlobSelection/searchByFilename?query=mars");
        assertStatus(200, response);
        Logger.info("***" + getContent(response));
    }

}