package controllers;

import models.ContentType;

/**
 * Backoffice for content types.
 *
 * @author Niko Schmuck
 * @since 22.01.2012
 */
// TODO: protect against unsecured access
@CRUD.For(ContentType.class)
public class ContentTypes extends CRUD {
}
