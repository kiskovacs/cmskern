package utils;

public class PlayExtensions extends play.templates.JavaExtensions {

	public static String up(String s) {
		return s.toUpperCase();
	}

    public static String thumbnailUrl(String s) {
        return s.replaceAll("(.*)/o/(.*)", "$1/t/$2");
    }

    
}