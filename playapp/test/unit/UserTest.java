package unit;

import models.User;
import org.junit.Before;
import org.junit.Test;
import play.modules.mongo.MongoFixtures;
import play.test.UnitTest;

/**
 * Unit tests for the {@link User} entity.
 *
 * @author Niko Schmuck
 * @since 18.04.2012
 */
public class UserTest extends UnitTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setUpData() {
        MongoFixtures.delete(User.class);
        MongoFixtures.loadModels("initial-users_and_roles.yml");
    }

    @Test
    public void count() {
        assertEquals(4, User.count());
    }

    @Test
    public void userHasRoleSet() {
        User admin = User.findByUserName("admin");
        assertEquals("admin", admin.role);
    }

    @Test
    public void create() {
        User u = new User("horst", "Horst Mayer", "guest");
        u._save();
        User horst = User.findByUserName("horst");
        assertNotNull(horst);
        User retrieved = User.findByUserName("horst");
        assertEquals("Horst Mayer", retrieved.fullName);
        assertEquals("guest", retrieved.role);
    }

}
