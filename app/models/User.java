package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Reference;
import models.deadbolt.RoleHolder;
import play.data.validation.Required;
import play.modules.morphia.Model;

import java.util.Arrays;
import java.util.List;

/**
 * Representing a single user accessing the system.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
@Entity(value = "users", noClassnameStored = true)
@Model.AutoTimestamp
public class User extends Model implements RoleHolder {

    @Required
    @Indexed(unique = true)
    public String userName;

    public String fullName;

    @Required
    @Reference
    public Role role;

    // ~~

    public User(String userName, String fullName, Role role) {
        this.userName = userName;
        this.fullName = fullName;
        this.role = role;
    }

    public static User findByUserName(String userName) {
        return find("userName", userName).first();
    }

    public List<? extends models.deadbolt.Role> getRoles() {
        return Arrays.asList(role);
    }

    // TODO: Strange: YAML Import seems not to be able to resolve Role type???
    public void setRoleByName(String rolename) {
        this.role = Role.findByName(rolename);
    }

    public void setRole(Role role) {
        this.role = role;
    }


    @Override
    public String toString() {
        return this.userName;
    }

}
