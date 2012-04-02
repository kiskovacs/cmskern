package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import models.deadbolt.RoleHolder;
import play.data.validation.Required;
import play.modules.morphia.Model;

import javax.persistence.ManyToOne;
import java.util.Arrays;
import java.util.List;

/**
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
    @ManyToOne
    public Role role;

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

    @Override
    public String toString() {
        return this.userName;
    }

}
