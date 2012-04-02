package secure;

import models.deadbolt.Role;

/**
 * @author Niko Schmuck
 * @since 02.04.2012
 */
public class CmsKernRole implements Role {

    private String name;

    public CmsKernRole(String name) {
        this.name = name;
    }

    public String getRoleName() {
        return name;
    }
}