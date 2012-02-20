package controllers;

import play.mvc.Controller;

import java.util.HashMap;

/**
 * @author Niko Schmuck
 * @since 20.02.2012
 */
public class Callouts extends Controller {
    
    public static void get(String name) {
        renderTemplate("Callouts/" + name + ".html", new HashMap());
    }
    
}
