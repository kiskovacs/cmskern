package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Reference;
import models.deadbolt.RoleHolder;
import play.Logger;
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

    // ~

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

    /* @Added void cascadeAdd() {
        Logger.info("----> (%s) role %s ", userName, role);
        if (!role.comments.contains(this)) {
            post.comments.add(this);
            post.save();
        }
    }*/

    public void setRole(String rolename) {
        Logger.info("********* SET ROLE: " + rolename);
        this.role = Role.findByName(rolename);
    }



    @Override
    public String toString() {
        return this.userName;
    }

}
