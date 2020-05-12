package ntou.project.djidrone.fragment;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import ntou.project.djidrone.DJIApplication;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;

public class CameraFragment extends Fragment {
    private static final String TAG = CameraFragment.class.getName();
    private ToggleButton mTbtnCameraMode;
    private TextView mTvCameraMode;
    private ImageView mBtnCamera;
    private Handler mHandler;
    private static SettingsDefinitions.ShootPhotoMode photoMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toggle toggle = new Toggle();
        mTbtnCameraMode = view.findViewById(R.id.tbtn_camera_mode);
        mTvCameraMode = view.findViewById(R.id.tv_camera_mode);
        mBtnCamera = getActivity().findViewById(R.id.btn_camera);
        mHandler = new Handler(Looper.getMainLooper());
        mTbtnCameraMode.setOnCheckedChangeListener(toggle);
        if(MobileActivity.isRecording)
            mTbtnCameraMode.setEnabled(false);
        super.onViewCreated(view, savedInstanceState);
    }

    private class Toggle implements CompoundButton.OnCheckedChangeListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tbtn_camera_mode:
                    Camera camera = DJIApplication.getCameraInstance();
                    if (isChecked) {
                        mTvCameraMode.setText(R.string.record_video);
                        mBtnCamera.setImageResource(R.drawable.record_video);
                        mBtnCamera.setTag(R.drawable.record_video);
                        if (null != camera)
                            switchCameraMode(SettingsDefinitions.CameraMode.RECORD_VIDEO);
                    } else {
                        mTvCameraMode.setText(R.string.shoot_photo);
                        mBtnCamera.setImageResource(R.drawable.shoot_photo);
                        mBtnCamera.setTag(R.drawable.shoot_photo);
                        if (null != camera) {
                            switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void switchCameraMode(SettingsDefinitions.CameraMode cameraMode) {
        Camera camera = DJIApplication.getCameraInstance();
        if (camera != null) {
            camera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });
        }
    }

    private void showToast(final String toastMsg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_LONG).show();
                Log.d(TAG, toastMsg);
            }
        });
    }

    public static synchronized SettingsDefinitions.ShootPhotoMode getPhotoMode() {
        Camera camera = DJIApplication.getCameraInstance();
        if (null != camera) {
            camera.getShootPhotoMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ShootPhotoMode>() {
                @Override
                public void onSuccess(SettingsDefinitions.ShootPhotoMode shootPhotoMode) {
                    photoMode = shootPhotoMode;
                }

                @Override
                public void onFailure(DJIError djiError) {
                    Log.d(TAG, djiError.getDescription());
                    photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE;
                }
            });
        }
        return photoMode;
    }

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()){
//                case "camera.enable":
//                    mTbtnCameraMode.setEnabled(true);
//                    break;
//                case "camera.disable":
//                    mTbtnCameraMode.setEnabled(false);
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
//    public static synchronized void setPhotoMode(SettingsDefinitions.ShootPhotoMode newPhotoMode) {
//        Camera camera = DJIApplication.getCameraInstance();
//        if (null != camera)
//            camera.setShootPhotoMode(newPhotoMode, new CommonCallbacks.CompletionCallback() {
//                @Override
//                public void onResult(DJIError djiError) {
//                    if (djiError == null) {
//                        Log.d(TAG, "init shoot photo mode single: success");
//                        photoMode = newPhotoMode;
//                    } else {
//                        Log.d(TAG, djiError.getDescription());
//                        photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE;
//                    }
//                }
//            });
//    }
}
