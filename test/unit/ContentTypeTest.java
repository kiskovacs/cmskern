package unit;

import models.ContentType;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Niko Schmuck
 * @since 26.01.2012
 */
public class ContentTypeTest extends UnitTest {

    @Before
    public void setUpData() {
        // Already done by Bootstrap Job
        // ContentType.deleteAll();
        // Fixtures.load("bootstrap-data.yml");
    }

    @Test
    public void count() {
        assertEquals(2, ContentType.count());
    }

    @Test
    public void getByName() {
        ContentType contentType = ContentType.findByName("articles");
        assertNotNull(contentType);
        assertEquals("Article", contentType.displayName);
        assertThat(contentType.jsonForm, containsString("\"name\": \"titel\", \"label\": \"Titel\""));
    }

}
