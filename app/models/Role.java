package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import play.data.validation.Required;
import play.modules.morphia.Model;

/**
 * An authorization role which can be assigned to one or more users
 * accessing the system.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
@Entity(value = "roles", noClassnameStored = true)
public class Role extends Model implements models.deadbolt.Role {

    @Required
    @Indexed(unique = true)
    public String name;

    // ~~

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String getRoleName() {
        return name;
    }

    public static Role findByName(String name) {
        return Role.find("name", name).first();
    }

    // ~~

    @Override
    public String toString() {
        return this.name;
    }

}