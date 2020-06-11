package ntou.project.djidrone.utils;

import android.widget.Toast;

import dji.common.error.DJIError;
import ntou.project.djidrone.DJIApplication;

public class ToastUtil {

    public static void showToast(String msg) {
        Toast.makeText(DJIApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorToast(String str ,DJIError djiError) {
        if(null == djiError){
            showToast(str);
        }else{
            showToast(djiError.getDescription());
        }
    }
}
