package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Reference;
import models.deadbolt.RoleHolder;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Codec;
import play.modules.morphia.Model;

import java.util.Arrays;
import java.util.Date;
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

    @Email
    @Required
    public String email;

    /**
     * Hält den SHA1 Wert des Passwortes (40 Hexadezimalstellen binär String).
     */
    public String passwd_hash;

    @Required
    @Reference
    public Role role;

    public Date lastLoginAt;

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

    // TODO: Strange: YAML import seems not to be able to resolve Role type???
    public void setRoleByName(String rolename) {
        this.role = Role.findByName(rolename);
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPasswd(String passwd) {
        if (passwd != null && passwd.trim().length() > 0) {
            Logger.info("~~ set password for user %s", userName);
            // calculate hash out of the given plain password
            this.passwd_hash = Codec.hexSHA1(passwd);
        }
    }

    public static User authenticate(String userName, String password) {
        User u = User.q("userName", userName).filter("passwd_hash", password).get();
        Logger.info("User u: %s", u);
        if (u != null) {
            u.lastLoginAt = new Date();
            u.save();
        }
        return u;
    }

    // ~~

    @Override
    public String toString() {
        return this.userName;
    }

}
