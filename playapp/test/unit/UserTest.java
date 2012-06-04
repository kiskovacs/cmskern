package unit;

import models.Role;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.modules.morphia.Model;
import play.test.MorphiaFixtures;
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
        MorphiaFixtures.delete(User.class);
        MorphiaFixtures.delete(Role.class);
        MorphiaFixtures.loadModels("initial-users_and_roles.yml");
    }

    @Test
    public void count() {
        assertEquals(4, User.count());
    }

    @Test
    public void userHasRoleSet() {
        User admin = User.findByUserName("admin");
        assertEquals(1, admin.getRoles().size());
        assertEquals("admin", admin.getRoles().get(0).name);
    }

    @Test
    public void create() {
        User u = new User("horst", "Horst Mayer", Role.findByName("guest"));
        Model horst = u.save();
        assertNotNull(horst);
        User retrieved = User.findByUserName("horst");
        assertEquals("Horst Mayer", retrieved.fullName);
        assertEquals("guest", retrieved.getRoles().get(0).name);
    }

}
