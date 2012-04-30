package functional;

import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testRedirectToLogin() {
        Response response = GET("/");
        assertStatus(302, response);
        assertHeaderEquals("Location", "/login", response);

        //assertIsOk(response);
        //assertContentType("text/html", response);
        //assertCharset(play.Play.defaultWebEncoding, response);
    }
    
}