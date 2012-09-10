package unit;

import models.Asset;
import org.junit.Test;
import play.test.UnitTest;

public class AssetTest extends UnitTest {

    @Test
    public void testIsSupported() {
        assertTrue(Asset.isSupportedContentType("image/jpeg"));
        assertTrue(Asset.isSupportedContentType("image/png"));
        assertTrue(Asset.isSupportedContentType("video/anything"));
    }

    @Test
    public void testIsNotSupported() {
        assertFalse(Asset.isSupportedContentType("audio/mpeg4"));
        assertFalse(Asset.isSupportedContentType("img/foo"));
    }
}
