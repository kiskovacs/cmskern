package models;

import play.data.validation.Required;

import java.io.Serializable;

/**
 * An authorization role which can be assigned to one or more users
 * accessing the system.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
// @MongoEntity("roles")
public class Role implements Serializable {

    @Required
    // TODO @Indexed(unique = true)
    public String name;

    // ~~

    public Role(String name) {
        this.name = name;
    }

    /*
    public static Role findByName(String name) {
        return Role.find("byName", name).first();
    }
    */

    // ~~

    @Override
    public String toString() {
        return this.name;
    }

}