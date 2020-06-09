package ntou.project.djidrone.fragment;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import ntou.project.djidrone.DJIApplication;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.ToastUtil;

public class CameraFragment extends Fragment {
    private static final String TAG = CameraFragment.class.getName();
    private ToggleButton mTbtnCameraMode;
    private TextView mTvCameraMode;
    private ImageView mBtnCamera;
    private static SettingsDefinitions.ShootPhotoMode photoMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnToggle onToggle = new OnToggle();
        mTbtnCameraMode = view.findViewById(R.id.tbtn_camera_mode);
        mTvCameraMode = view.findViewById(R.id.tv_camera_mode);
         mBtnCamera = getActivity().findViewById(R.id.btn_camera);
        mTbtnCameraMode.setOnCheckedChangeListener(onToggle);
        super.onViewCreated(view, savedInstanceState);
    }

    private class OnToggle implements CompoundButton.OnCheckedChangeListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tbtn_camera_mode:
                    Camera camera = DJIApplication.getCameraInstance();
                    if (null != camera) {//已連接
                        if (isChecked) {
                            switchCameraMode(SettingsDefinitions.CameraMode.RECORD_VIDEO);
                        } else {
                            switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                        }
                    } else {//未連接
                        if (isChecked) {
                            mTvCameraMode.setText(R.string.record_video);
                            mBtnCamera.setImageResource(R.drawable.icon_record_video);
                            mBtnCamera.setTag(R.drawable.icon_record_video);
                        } else {
                            mTvCameraMode.setText(R.string.shoot_photo);
                            mBtnCamera.setImageResource(R.drawable.icon_shoot_photo);
                            mBtnCamera.setTag(R.drawable.icon_shoot_photo);
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
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast("Switch Camera Mode Succeeded");
                                if (cameraMode == SettingsDefinitions.CameraMode.SHOOT_PHOTO) {
                                    mTvCameraMode.setText(R.string.shoot_photo);
                                    mBtnCamera.setImageResource(R.drawable.icon_shoot_photo);
                                    mBtnCamera.setTag(R.drawable.icon_shoot_photo);
                                } else if (cameraMode == SettingsDefinitions.CameraMode.RECORD_VIDEO) {
                                    mTvCameraMode.setText(R.string.record_video);
                                    mBtnCamera.setImageResource(R.drawable.icon_record_video);
                                    mBtnCamera.setTag(R.drawable.icon_record_video);
                                } else {
                                    ToastUtil.showToast("" + cameraMode);
                                }
                            }
                        });
                    } else {
                        mTbtnCameraMode.setChecked(!mTbtnCameraMode.isChecked());
                        ToastUtil.showToast(djiError.getDescription());
                    }
                }
            });
        }
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
