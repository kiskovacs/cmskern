package unit;

import org.junit.Test;
import play.test.UnitTest;
import utils.PlayExtensions;

public class PlayExtensionsTest extends UnitTest {

    @Test
    public void abbreviate() {
        String out = PlayExtensions.abbreviate("A very long text which will not fit into a small table cell", 20);
        assertEquals(20, out.length());
        assertEquals("A very long text ...", out);
    }

}
