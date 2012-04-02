package controllers;

import controllers.deadbolt.DeadboltHandler;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;
import controllers.deadbolt.RestrictedResourcesHandler;
import models.User;
import models.deadbolt.RoleHolder;
import play.Logger;
import play.mvc.Controller;

/**
 * @author Niko Schmuck
 * @since 02.04.2012
 */
public class AccessControl extends Controller implements DeadboltHandler {

    public void beforeRoleCheck() {
        Logger.info("****************** beforeRoleCheck");
        // Note that if you provide your own implementation of Secure's Security class you would refer to that instead
        if (!Secure.Security.isConnected()) {
            try {
                if (!session.contains("username")) {
                    flash.put("url", "GET".equals(request.method) ? request.url : "/");
                    Secure.login();
                }
            } catch (Throwable t) {
                // handle this in an app-specific way
            }
        }
    }

    public RoleHolder getRoleHolder() {
        String userName = Secure.Security.connected();
        return User.findByUserName(userName);
    }

    /**
     * Custom handling of access failure.  Note that further information on the request can be taken from
     * Http.Request.current(), such as actionMethod, etc.
     *
     * @param controllerClassName the name of the controller class.
     */
    public void onAccessFailure(String controllerClassName) {
        Logger.error("Hit an authorisation issue when trying to access [%s]", controllerClassName);
        forbidden();
    }


    // currently not required

    public ExternalizedRestrictionsAccessor getExternalizedRestrictionsAccessor() {
        return null;
    }

    public RestrictedResourcesHandler getRestrictedResourcesHandler() {
        return null;
    }
}