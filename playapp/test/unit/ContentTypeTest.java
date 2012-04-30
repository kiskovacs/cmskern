package unit;

import models.ContentType;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.test.MorphiaFixtures;
import play.test.UnitTest;

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
        Logger.info("Going to setup ...");
        MorphiaFixtures.deleteDatabase();
        MorphiaFixtures.loadModels("bootstrap-contenttypes.yml");
    }

    @Test
    public void count() {
        assertEquals(4, ContentType.count());
    }

    @Test
    public void getByName() {
        ContentType contentType = ContentType.findByName("article");
        assertNotNull(contentType);
        assertEquals("Article", contentType.displayName);
        assertThat(contentType.jsonForm, containsString("\"name\": \"titel\", \"label\": \"Titel\""));
    }

}
