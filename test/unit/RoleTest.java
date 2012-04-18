package unit;

import models.Role;
import org.junit.Before;
import org.junit.Test;
import play.modules.morphia.Model;
import play.test.MorphiaFixtures;
import play.test.UnitTest;

/**
 * @author Niko Schmuck
 * @since 18.04.2012
 */
public class RoleTest extends UnitTest {

    @Before
    public void setUpData() {
        MorphiaFixtures.delete(Role.class);
        MorphiaFixtures.loadModels("bootstrap-user.yml");
    }

    @Test
    public void count() {
        assertEquals(4, Role.count());
    }

    @Test
    public void findByName() {
        Role admin = Role.findByName("admin");
        assertEquals("admin", admin.getRoleName());
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
