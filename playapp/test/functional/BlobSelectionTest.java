package functional;

import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.modules.mongo.MongoFixtures;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import utils.MongoDbUtils;

public class BlobSelectionTest extends FunctionalTest {

    @Before
    public void setUpData() {
        MongoFixtures.deleteDatabase();
        MongoDbUtils.dropFiles();
        MongoFixtures.loadModels("initial-assets.yml");
    }

    @Test
    public void testSearchFilename() {
        Response response = GET("/helper/BlobSelection/searchByFilename?query=mars");
        assertStatus(200, response);
        Logger.info("***" + getContent(response));
    }

}