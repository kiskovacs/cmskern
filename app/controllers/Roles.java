package controllers;

import models.Role;

/**
 * Backoffice for user roles.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
// TODO: protect against unsecured access
@CRUD.For(Role.class)
public class Roles extends CRUD {
}
