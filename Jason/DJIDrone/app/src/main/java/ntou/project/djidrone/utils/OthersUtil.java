package ntou.project.djidrone.utils;

public class OthersUtil {
    public static boolean isNumeric(String str) {
        if(str.equals(""))
            return false;
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
