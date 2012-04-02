package controllers;

import models.User;

/**
 * Backoffice for content types.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
// TODO: protect against unsecured access
@CRUD.For(User.class)
public class Users extends CRUD {
}
