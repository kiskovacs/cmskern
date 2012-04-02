package secure;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.DeadboltHandler;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;
import controllers.deadbolt.RestrictedResourcesHandler;
import models.deadbolt.RoleHolder;
import play.Logger;

/**
 * @author Niko Schmuck
 * @since 02.04.2012
 */
public class AccessControl implements DeadboltHandler {

    public void beforeRoleCheck() {
        // Ensure the current user is logged in, and redirect accordingly
        Logger.info("****************** beforeRoleCheck");
    }

    public RoleHolder getRoleHolder() {
        return new CmsKernRoleHolder();
    }

    /**
     * Custom handling of access failure.  Note that further information on the request can be taken from
     * Http.Request.current(), such as actionMethod, etc.
     *
     * @param controllerClassName the name of the controller class.
     */
    public void onAccessFailure(String controllerClassName) {
        Logger.error("Hit an authorisation issue when trying to access [%s]", controllerClassName);
        Deadbolt.forbidden();
    }


    // currently not required

    public ExternalizedRestrictionsAccessor getExternalizedRestrictionsAccessor() {
        return null;
    }

    public RestrictedResourcesHandler getRestrictedResourcesHandler() {
        return null;
    }
}