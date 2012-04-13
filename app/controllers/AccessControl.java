package controllers;

import controllers.deadbolt.DeadboltHandler;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;
import controllers.deadbolt.RestrictedResourcesHandler;
import models.User;
import models.deadbolt.Role;
import models.deadbolt.RoleHolder;
import play.Logger;
import play.mvc.Controller;

import java.util.Arrays;
import java.util.List;

/**
 * Bridging the standard security interceptor with the deadbolt
 * security access control list implementation.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
public class AccessControl extends Controller implements DeadboltHandler {

    public void beforeRoleCheck() {
        Logger.info("****************** beforeRoleCheck");
        // Note that if you provide your own implementation of Secure's Security class you would refer to that instead
        /*
        if (!Secure.Security.isConnected()) {
            try {
                if (!session.contains("username")) {
                    Logger.info("Redirect to login screen ...");
                    flash.put("url", "GET".equals(request.method) ? request.url : "/");
                    Secure.login();
                }
            } catch (Throwable t) {
                Logger.warn("Problem while forwarding to login page: %s", t);
                throw new RuntimeException(t);
                // handle this in an app-specific way
            }
        }
        */
    }

    public RoleHolder getRoleHolder() {
        String userName = Secure.Security.connected();
        if (userName != null) {
            Logger.info("Role holder for user %s", userName);
            return User.findByUserName(userName);
        } else {
            // TODO: just for the time being
            return new RoleHolder() {
                @Override
                public List<? extends Role> getRoles() {
                    return Arrays.asList(new models.Role("editor"));
                }
            };
        }
    }

    /**
     * Custom handling of access failure.  Note that further information on the request can be taken from
     * Http.Request.current(), such as actionMethod, etc.
     *
     * @param controllerClassName the name of the controller class.
     */
    public void onAccessFailure(String controllerClassName) {
        Logger.warn("Hit an authorisation issue when trying to access [%s -> %s]", controllerClassName, request.actionMethod);
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