package pftp.util;

public class StringUtil {
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNumber(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i)))
                return false;
        }
        return true;
    }
}
