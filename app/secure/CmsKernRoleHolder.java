package secure;

import models.deadbolt.Role;
import models.deadbolt.RoleHolder;

import java.util.Arrays;
import java.util.List;

/**
 * @author Niko Schmuck
 * @since 02.04.2012
 */
public class CmsKernRoleHolder implements RoleHolder {

    public List<? extends Role> getRoles() {
        return Arrays.asList(
                new CmsKernRole("editor"),
                new CmsKernRole("admin")
        );
    }
}