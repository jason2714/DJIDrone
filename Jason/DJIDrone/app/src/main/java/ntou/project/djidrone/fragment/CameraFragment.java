package ntou.project.djidrone.fragment;

import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
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
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import ntou.project.djidrone.R;

public class CameraFragment extends Fragment {
    private ToggleButton mTbtnCameraMode;
    private TextView mTvCameraMode;
    private ImageView mBtnCamera;
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
        mTbtnCameraMode.setOnCheckedChangeListener(toggle);
        super.onViewCreated(view, savedInstanceState);
    }

    private class Toggle implements CompoundButton.OnCheckedChangeListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tbtn_camera_mode:
                    if (isChecked){
                        mTvCameraMode.setText(R.string.record_video);
                        mBtnCamera.setImageResource(R.drawable.record_video);
                    }
                    else{
                        mTvCameraMode.setText(R.string.shoot_photo);
                        mBtnCamera.setImageResource(R.drawable.shoot_photo);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
