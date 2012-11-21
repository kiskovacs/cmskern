package unit;

import models.ContentNode;
import models.vo.IdTitle;
import models.vo.SearchResult;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
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
        ContentNode.deleteAll();
    }

    @Test
    public void updateNode() throws InterruptedException {
        ContentNode node = new ContentNode("default", "article", "{\"title\":\"foo\"}");
        // creates version 1
        node.create("UnitTester");
        assertNotNull(node.getId());

        ContentNode v1 = ContentNode.findById(node.getId());
        assertEquals("article", v1.getType());
        assertThat(v1.getJsonContent(), containsString("foo"));
        assertEquals(Integer.valueOf(1), v1.getVersion());
        assertEquals("UnitTester", v1.getCreator());
        assertEquals("UnitTester", v1.getModifier());
        assertNotNull(v1.getModifier());
        Date v1_timestamp = (Date) v1.getModified().clone();

        // this will create a second version
        Thread.sleep(10);
        v1.update("TanteEmma", "{\"title\":\"bar\"}");

        ContentNode v2 = ContentNode.findById(node.getId());
        assertEquals(Integer.valueOf(2), v2.getVersion());
        assertTrue(v2.getModified().after(v1_timestamp));
        assertEquals("Created date must stay constant", v1.getCreated(), v2.getCreated());
        assertThat(v2.getJsonContent(), containsString("bar"));
        assertThat(v2.getTitle(), is("bar"));
        assertThat(v2.getJsonContent(), not(containsString("foo")));
    }

    @Test
    public void searchNodes() throws InterruptedException {
        createContentNode("article", "{\"title\":\"Blumenwiese (Nr. 1)\"}");
        Thread.sleep(200); // to make sure modification order is safe for later comparision
        createContentNode("article", "{\"title\":\"Blumenthal (Schnauss)\"}");
        Thread.sleep(200); // to make sure modification order is safe for later comparision
        createContentNode("article", "{\"title\":\"Völlig ohne Bl... \"}");

        List<IdTitle> result = ContentNode.findByTypeAndTitleMinimal("default", "article", "blume", false, 0, 20);
        assertEquals(2, result.size());
        assertEquals("Blumenthal (Schnauss)", result.get(0).title); // latest matching first
    }

    @Test
    public void searchNodesWithOffset() throws InterruptedException {
        createContentNode("article", "{\"title\":\"Laugenstange\"}");
        Thread.sleep(200); // to make sure modification order is safe for later comparision
        createContentNode("article", "{\"title\":\"Brezel\"}");
        Thread.sleep(200); // to make sure modification order is safe for later comparision
        createContentNode("article", "{\"title\":\"Baguette\"}");

        SearchResult<ContentNode> result = ContentNode.findByType("default", "article", 1, 1);
        assertEquals(3, result.totalCount);
        assertEquals(1, result.objects.size());
        assertEquals("Brezel", result.objects.get(0).getTitle());
    }


    private void createContentNode(String type, String data) {
        ContentNode node = new ContentNode("default", type, data);
        node.create("UnitTester");
    }

}