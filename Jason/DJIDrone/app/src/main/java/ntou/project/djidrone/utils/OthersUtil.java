package ntou.project.djidrone.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Size;

import ntou.project.djidrone.Define;

public class OthersUtil {
    public static boolean isNumeric(String str) {
        if (str.equals(""))
            return false;
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                if (i != 0 || (str.charAt(0) != '-'))
                    return false;
            }
        }
        return true;
    }

    public static int parseIntDefault(String str, int defaultValue) {
        int value;
        try {
            value = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            value = defaultValue;
            if (isNumeric(str))
                ToastUtil.showToast("out of Integer bound");
            else
                ToastUtil.showToast("not a number");
        }
        return value;
    }

    public static int parseInt(String str) {
        int value;
        try {
            value = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            if (isNumeric(str)) {
                value = Define.OUT_OF_BOUND;
                ToastUtil.showToast("out of Integer bound");
            } else {
                value = Define.NOT_A_NUMBER;
                ToastUtil.showToast("not a number");
            }
        }
        return value;
    }

    public static float parseFloat(String str) {
        float value;
        try {
            value = Float.parseFloat(str);
        } catch (NumberFormatException e) {
            if (isNumeric(str)) {
                value = Define.OUT_OF_BOUND;
                ToastUtil.showToast("out of Integer bound");
            } else {
                value = Define.NOT_A_NUMBER;
                ToastUtil.showToast("not a number");
            }
        }
        return value;
    }

    public static double calcManhattanDistance(double point1X, double point1Y, double point2X, double point2Y) {
        return Math.abs(point1X - point2X) + Math.abs(point1Y - point2Y);
    }

    public static float convertDpToPixel(float dp, Context context){
        float px = dp * getDensity(context);
        return px;
    }

    public static float convertPixelToDp(float px, Context context){
        float dp = px / getDensity(context);
        return dp;
    }

    public static float getDensity(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    public static Size getScreenSizePixel(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return new Size(metrics.widthPixels,metrics.heightPixels);
    }

}
