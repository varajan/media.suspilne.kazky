package media.suspilne.kazky.helpers;

import java.util.regex.Pattern;

public class StringHelper {
    public static String substringTo(String text, String delimiter) {
        if (text == null || delimiter == null || delimiter.isEmpty()) {
            return text;
        }

        int index = text.indexOf(delimiter);
        if (index == -1) {
            return "";
        }

        return text.substring(0, index);
    }

    public static String substringFrom(String text, String delimiter) {
        if (text == null || delimiter == null || delimiter.isEmpty()) {
            return text;
        }

        int index = text.indexOf(delimiter);
        if (index == -1) {
            return text;
        }

        return text.substring(index + delimiter.length());
    }

    public static String trim(String text, String charsToTrim) {
        if (text == null || text.isEmpty() || charsToTrim == null || charsToTrim.isEmpty()) {
            return text;
        }

        String escapedChars = Pattern.quote(charsToTrim);
        String regex = "^(?:" + escapedChars + ")+|(?:" + escapedChars + ")+$";

        return text.trim().replaceAll(regex, "").trim();
    }

    public static boolean containsIgnoreCase(String source, String target) {
        if (source == null || target == null) return false;
        if (target.isEmpty()) return true;

        source = source.toLowerCase();
        target = target.toLowerCase().trim();

        return source.contains(target);
    }
}