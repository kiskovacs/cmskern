package unit;

import models.ContentNode;
import org.junit.Before;
import org.junit.Test;
import play.test.MorphiaFixtures;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Unit tests for the {@link models.ContentNode} entity.
 *
 * @author Niko Schmuck
 * @since 05.03.2012
 */
public class ContentNodeTest extends UnitTest {

    @Before
    public void setUpData() {
        MorphiaFixtures.deleteDatabase(); // since content nodes to not relate to a Morphia based model
        MorphiaFixtures.loadModels("bootstrap-contenttypes.yml");
    }

    @Test
    public void updateNode() {
        ContentNode node = new ContentNode("article", "{\"titel\":\"foo\"}");
        node.create();
        assertNotNull(node.getId());

        ContentNode v1 = ContentNode.findById(node.getId());
        assertEquals("article", v1.getType());
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
