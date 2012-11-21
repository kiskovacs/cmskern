package unit;

import models.ContentType;
import org.junit.Before;
import org.junit.Test;
import play.modules.mongo.MongoFixtures;
import play.test.UnitTest;

import java.util.List;

import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Unit tests for the {@link models.ContentType} entity.
 *
 * @author Niko Schmuck
 * @since 26.01.2012
 */
public class ContentTypeTest extends UnitTest {

    @Before
    public void setUpData() throws InterruptedException {
        MongoFixtures.deleteDatabase();
        MongoFixtures.loadModels("initial-contenttypes.yml");
    }

    @Test
    public void count() {
        assertEquals(9, ContentType.count());
    }

    @Test
    public void getArticleByName() {
        ContentType contentType = ContentType.findByName("article");
        assertNotNull(contentType);
        assertEquals("Article", contentType.displayName);
        assertThat(contentType.jsonSchema, containsString("Weiterführende Artikel"));
    }

    @Test
    public void getImageByName() {
        ContentType contentType = ContentType.findByName("image");
        assertNotNull(contentType);
        assertEquals("Image", contentType.displayName);
    }

    @Test
    public void getByGroup() {
        List<ContentType> contentTypes = ContentType.findByGroup("editorial");
        assertEquals(5, contentTypes.size());
        assertEquals("Article", contentTypes.get(0).displayName);
        //assertEquals("Article Collection", contentTypes.get(1).displayName);
        //assertEquals("Image", contentTypes.get(2).displayName);
        //assertEquals("Image Gallery", contentTypes.get(3).displayName);
    }

}
