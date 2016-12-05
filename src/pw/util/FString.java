package pw.util;

/**
 * Strings static functions
 * Created by lina on 14.09.16.
 */
@SuppressWarnings("WeakerAccess")
public class FString {
    public static String cutFrom(String string, int pos) {
        return string.substring(pos);
    }

    public static String cutFrom(String string, String trigger) {
        if (string.contains(trigger))
            return string.substring(string.indexOf(trigger) + trigger.length());

        return string;
    }

    public static String cutTo(String string, int pos) {
        return string.substring(0, pos);
    }

    public static String cutTo(String string, String trigger) {
        if (string.contains(trigger))
            return string.substring(0, string.indexOf(trigger));

        return string;
    }

    public static String getBetween(String string, String from, String to) {
        if (string.contains(to) && string.contains(from))
            return cutTo(cutFrom(string, from), to);

        if (string.contains(from))
            return cutFrom(string, from);

        return string;
    }
}
