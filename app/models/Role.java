package models;

import com.google.code.morphia.annotations.Entity;
import play.data.validation.Required;
import play.modules.morphia.Model;

/**
 * @author Niko Schmuck
 * @since 02.04.2012
 */
@Entity
public class Role extends Model implements models.deadbolt.Role {

    @Required
    public String name;

    public Role(String name) {
        this.name = name;
    }

    public String getRoleName() {
        return name;
    }

    public static Role findByName(String name) {
        return Role.find("name", name).first();
    }

    @Override
    public String toString() {
        return this.name;
    }

}