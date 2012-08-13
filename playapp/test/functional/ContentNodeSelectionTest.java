package functional;

import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.test.MorphiaFixtures;

public class ContentNodeSelectionTest extends FunctionalTest {

    @Before
    public void setUpData() {
        MorphiaFixtures.deleteDatabase();
        MorphiaFixtures.loadModels("initial-content.yml");
    }


    @Test
    public void testSearchByTitleNoMatch() {
        Response response = GET("/helper/ContentNodeSelection/searchByTitle?query=test&type=article");
        assertStatus(200, response);
        assertContentMatch("No results found for <em>test</em>", response);
    }

    @Test
    public void testSearchByTitle() {
        Response response = GET("/helper/ContentNodeSelection/searchByTitle?query=af&type=article");
        assertStatus(200, response);
        Logger.info("-->" + getContent(response));
        assertContentMatch("Menschenaffen in Europa", response);
    }

}