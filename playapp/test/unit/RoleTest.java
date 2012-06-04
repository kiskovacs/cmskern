package unit;

import models.Role;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.modules.morphia.Model;
import play.test.MorphiaFixtures;
import play.test.UnitTest;

/**
 * Unit tests for the {@link Role} entity.
 *
 * @author Niko Schmuck
 * @since 18.04.2012
 */
public class RoleTest extends UnitTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setUpData() {
        MorphiaFixtures.delete(User.class);
        MorphiaFixtures.delete(Role.class);
        MorphiaFixtures.loadModels("initial-users_and_roles.yml");
    }

    @Test
    public void count() {
        assertEquals(4, Role.count());
    }

    @Test
    public void findByName() {
        Role admin = Role.findByName("admin");
        assertEquals("admin", admin.name);
    }

    @Test
    public void create() {
        Role r = new Role("uberadmin");
        Model m = r.save();
        assertNotNull(m);
        Role retrieved = Role.findByName("uberadmin");
        assertEquals(m.getId(), retrieved.getId());
    }

}
