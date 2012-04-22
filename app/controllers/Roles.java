package controllers;

import models.Role;
import play.mvc.With;

/**
 * Backoffice for user roles.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
// TODO: protect against unsecured access
@CRUD.For(Role.class)
@With(Secure.class)
@Check("admin")
public class Roles extends CRUD {
}
