package unit;

import models.ContentType;
import org.junit.Before;
import org.junit.Test;
import play.test.MorphiaFixtures;
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
        MorphiaFixtures.deleteDatabase();
        MorphiaFixtures.loadModels("bootstrap-contenttypes.yml");
    }

    @Test
    public void count() {
        assertEquals(7, ContentType.count());
    }

    @Test
    public void getByName() {
        ContentType contentType = ContentType.findByName("article");
        assertNotNull(contentType);
        assertEquals("Article", contentType.displayName);
        assertThat(contentType.jsonForm, containsString("\"name\": \"title\", \"label\": \"Title\""));
    }

    @Test
    public void getByGroup() {
        List<ContentType> contentTypes = ContentType.findByGroup("editorial");
        assertEquals(4, contentTypes.size());
        assertEquals("Article", contentTypes.get(0).displayName);
        assertEquals("Article Collection", contentTypes.get(1).displayName);
        assertEquals("Image", contentTypes.get(2).displayName);
        assertEquals("Image Gallery", contentTypes.get(3).displayName);
    }

}
