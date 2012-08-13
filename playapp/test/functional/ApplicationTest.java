package functional;

import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

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

    @Test
    public void testInvalidLogin() {
        Response response = GET("/login");
        assertIsOk(response);

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", "axel");
        params.put("password", "ACHSEL");
        response = POST("/login", params);
        assertStatus(302, response);
        assertHeaderEquals("Location", "/login", response);
    }

    @Test
    public void testValidLogin() {
        Response response = GET("/login");
        assertIsOk(response);

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", "axel");
        params.put("password", "axel");
        response = POST("/login", params);
        assertStatus(302, response);
        assertHeaderEquals("Location", "/", response);

        response = GET("/");
        assertStatus(200, response);

        // Logger.info("***" + getContent(response));
        assertContentMatch("Content Overview", response);
    }
}