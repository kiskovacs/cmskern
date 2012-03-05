package unit;

import models.ContentNode;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Niko Schmuck
 * @since 05.03.2012
 */
public class ContentNodeTest extends UnitTest {

    @Before
    public void setUpData() {
        // Already done by Bootstrap Job
        // ContentType.deleteAll();
        // Fixtures.load("bootstrap-data.yml");
    }

    @Test
    public void updateNode() {
        ContentNode node = new ContentNode("articles", "{\"titel\":\"foo\"}");
        node.create();
        assertNotNull(node.getId());

        ContentNode v1 = ContentNode.findById(node.getId());
        assertEquals("articles", v1.getType());
        assertThat(v1.getJsonContent(), containsString("foo"));
        assertEquals(Integer.valueOf(1), v1.getVersion());
        assertNotNull(v1.getModified());

        v1.update("{\"titel\":\"bar\"}");

        ContentNode v2 = ContentNode.findById(node.getId());
        assertEquals(Integer.valueOf(2), v2.getVersion());
        assertTrue(v2.getModified().after(v1.getModified()));
        assertEquals("Created date must stay constant", v1.getCreated(), v2.getCreated());
        assertThat(v2.getJsonContent(), containsString("bar"));
        assertThat(v2.getJsonContent(), not(containsString("foo")));
    }

}
