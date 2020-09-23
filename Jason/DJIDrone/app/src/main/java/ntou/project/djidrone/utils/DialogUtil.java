package ntou.project.djidrone.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import dji.common.error.DJIError;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;

public class DialogUtil{

    public static void showDialog(Context ctx, String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.set_dialog);
        builder.setMessage(str);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        showDialogExceptActionBar(builder.create());
    }

    public static void showDialog(Context ctx, int strId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.set_dialog);
        builder.setMessage(strId);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        showDialogExceptActionBar(builder.create());

    }

    public static void showDialogBasedOnError(Context ctx, DJIError djiError) {
        if (null == djiError) {
            showDialog(ctx, R.string.success);
        } else {
            showDialog(ctx, djiError.getDescription());
        }
    }

    public static void showDialogExceptActionBar(AlertDialog alertDialog){
        alertDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//TODO COLOR
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        if(!alertDialog.isShowing())
            alertDialog.show();
    }
}
