package controllers;

import models.User;
import play.Logger;

/**
 * This class providing user authentication and
 * group membership verification.
 */
public class Security extends Secure.Security {

    static boolean authenticate(String username, String password) {
        Logger.info("Authenticating %s ...", username);
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
     * @param groupname One or more group names (separated by comma)
     */
    static boolean check(String groupname) {
        String username = connected();
    	if (username == null) {
    		Logger.warn("User can not be logged in, cannot check group membership.");
    		return false;
    	}
        // TODO: cache user info to avoid look up on every single secure.cache tag
        User user = User.findByUserName(username);
        Logger.debug("check if user %s is member of group %s", user, groupname);
        boolean found = false;
        if (user != null && user.role != null) {
            String[] roles = groupname.split(",");
            for (String role : roles) {
                if (user.role.equals(role)) {
                    found = true;
                    break;
                }
            }

        } else {
    		Logger.warn("User '%s' is not allowed to access: %s", username, request.path);
        }
        return found;
    }
    
    static void onDisconnected() {
        // String username = params.get("username");
        Application.index();
    }


    static void onAuthenticated() {
        Logger.info("Login by user %s", connected());
    }

    static void onDisconnect() {
        Logger.info("Logout by user %s", connected());
    }

    static void onCheckFailed(String groupname) {
        Logger.warn("Failed auth for group %s", groupname);
        forbidden();
    }

}
