package controllers;

import models.ContentType;
import play.mvc.With;

/**
 * Backoffice for content types.
 *
 * @author Niko Schmuck
 * @since 22.01.2012
 */
@With(Secure.class)
@Check("admin")
@CRUD.For(ContentType.class)
public class ContentTypes extends CRUD {
}
