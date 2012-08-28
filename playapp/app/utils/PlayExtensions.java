package utils;

import org.apache.commons.lang.StringUtils;

public class PlayExtensions extends play.templates.JavaExtensions {

    public static String abbreviate(String in, int maxLength) {
        return StringUtils.abbreviate(in.replaceAll("\\<.*?>", ""), maxLength);
    }

	public static String up(String s) {
		return s.toUpperCase();
	}

}