package controllers;

import models.User;
import play.mvc.With;

/**
 * Backoffice for users accessing cmskern.
 *
 * @author Niko Schmuck
 * @since 02.04.2012
 */
@CRUD.For(User.class)
@With(Secure.class)
@Check("admin")
public class Users extends CRUD {
}
