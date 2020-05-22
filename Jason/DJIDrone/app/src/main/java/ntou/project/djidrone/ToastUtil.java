package ntou.project.djidrone;

import android.widget.Toast;

public class ToastUtil {
    public static void showToast(String msg) {
        Toast.makeText(DJIApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }
}
