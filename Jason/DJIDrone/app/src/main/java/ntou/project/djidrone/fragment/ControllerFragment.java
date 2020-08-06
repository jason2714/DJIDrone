package ntou.project.djidrone.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import dji.common.camera.SettingsDefinitions;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import ntou.project.djidrone.DJIApplication;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;

public class ControllerFragment extends Fragment {

    private Switch mTbtnVirtualStickState;
    private TextView mTvVirtualStickState;
    private BaseProduct mProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controller,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTbtnVirtualStickState = view.findViewById(R.id.sw_virtual_stick_state);
        mTvVirtualStickState = view.findViewById(R.id.tv_virtual_stick_state);
        OnToggle onToggle = new OnToggle();
        mTbtnVirtualStickState.setOnCheckedChangeListener(onToggle);
        super.onViewCreated(view, savedInstanceState);
    }
    private class OnToggle implements CompoundButton.OnCheckedChangeListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.sw_virtual_stick_state:
                    mProduct = DJIApplication.getProductInstance();
                    if (null != mProduct) {//已連接
                        if(getActivity() instanceof MobileActivity)
                            ((MobileActivity)getActivity()).mVirtualStick.virtualStickEnable(isChecked);
                        if (isChecked) {
                            mTvVirtualStickState.setText(R.string.open);
                        } else {
                            mTvVirtualStickState.setText(R.string.close);
                        }
                    } else {//未連接
                        if (isChecked) {
                            mTvVirtualStickState.setText(R.string.open);
                        } else {
                            mTvVirtualStickState.setText(R.string.close);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}