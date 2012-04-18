package controllers;

import models.User;
import play.Logger;

/**
 * Hinweis: weiteres Login Verfahren, bei erfolgreichem Anmelden, steckt
 * im {@link Application} Controller.
 */
public class Security extends Secure.Security {

    static boolean authenticate(String username, String password) {
        boolean valid = (User.authenticate(username, password) != null);
        if (!valid) {
        	Logger.warn("Invalid combination of username (%s) and password: %s", username, request.path);
        } else {
        	Logger.info("Successfully authenticated user '%s'", username);
        }

        return valid;
    }

    /**
     * check if user is at least in one group
     *
     * @param groupname Ein oder mehrere Gruppennamen (mit Komma separiert)
     */
    static boolean check(String groupname) {
    	String username = connected();
    	if (username == null) {
    		Logger.warn("User is not logged in, cannot check group membership.");
    		return false;
    	}
        User user = User.findByUserName(username);
        Logger.info("----> check user: %s", user);
        if (user != null) {
            // Ist Benutzer der geforderten Gruppe angehörig?
/*
            String[] reqGroup = groupname.split(",");
            if (user.isMemberOfAtLeastOne(reqGroup)) {
                cacheUpdateUser(username);  // TODO: vielleicht etwas seltener
            	return true;
            } else {
            	// ... tritt auch bei jedem secure.check tag Aufruf
        		// Logger.debug("User '%s' is not assigned to group '%s': %s", username, groupname, request.path);
            }
*/
        } else {
    		Logger.warn("Unknown user name '%s' specified: %s", username, request.path);
        }
        return false;
    }
    
    static void onDisconnected() {
        // String username = params.get("username");
        Application.index();
    }

}
