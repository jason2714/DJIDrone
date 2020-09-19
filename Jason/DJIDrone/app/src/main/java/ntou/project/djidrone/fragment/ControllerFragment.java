package ntou.project.djidrone.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import ntou.project.djidrone.utils.DJIApplication;
import ntou.project.djidrone.Define;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.OthersUtil;
import ntou.project.djidrone.utils.ToastUtil;

public class ControllerFragment extends Fragment {

    private Switch mSwVirtualStickState, mSwActiveTrackState;
    private TextView mTvVirtualStickState, mTvActiveTrackState;
    private EditText mEtRthHeight;
    private final static int DEFAULT_RTH_HEIGHT = 30;
    private FlightController mFlightController;
    private BaseProduct mProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO init height true
        mSwVirtualStickState = view.findViewById(R.id.sw_virtual_stick_state);
        mTvVirtualStickState = view.findViewById(R.id.tv_virtual_stick_state);
        mSwActiveTrackState = view.findViewById(R.id.sw_active_track_state);
        mTvActiveTrackState = view.findViewById(R.id.tv_active_track_state);
        mEtRthHeight = view.findViewById(R.id.et_rth_height);
        initListener();
    }

    //    flightController.getSmartReturnToHomeEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
//        @Override
//        public void onSuccess(Boolean aBoolean) {
//            stringBuffer.append(aBoolean);
//        }
//
//        @Override
//        public void onFailure(DJIError djiError) {
//            ToastUtil.showErrorToast("error",djiError);
//        }
//    });
    private void initListener() {
        OnToggle onToggle = new OnToggle();
        mSwVirtualStickState.setOnCheckedChangeListener(onToggle);
        mSwActiveTrackState.setOnCheckedChangeListener(onToggle);
        mEtRthHeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    int rthHeight;
                    mFlightController = DJIApplication.getFlightControllerInstance();
                    rthHeight = OthersUtil.parseInt(mEtRthHeight.getText().toString());
                    if (rthHeight != Define.NOT_A_NUMBER) {
                        if (rthHeight >= 20 && rthHeight <= 100) {
                            setRthHeight(rthHeight, "set rth height success");
                        } else {
                            rthHeight = DEFAULT_RTH_HEIGHT;
                            setRthHeight(rthHeight, "out of rth height bound");
                        }
                    } else {
                        rthHeight = DEFAULT_RTH_HEIGHT;
                        setRthHeight(rthHeight, "not a number");
                    }
                    mEtRthHeight.setText(String.valueOf(rthHeight));
                }
                return false;
            }
        });
    }

    private class OnToggle implements CompoundButton.OnCheckedChangeListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.sw_virtual_stick_state:
                    mProduct = DJIApplication.getProductInstance();
                    if (null != mProduct) {//已連接
                        if (getActivity() instanceof MobileActivity)
                            ((MobileActivity) getActivity()).mVirtualStick.virtualStickEnable(isChecked);
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
                case R.id.sw_active_track_state:
                    if (isChecked) {
                        mTvActiveTrackState.setText(R.string.open);
                    } else {
                        mTvActiveTrackState.setText(R.string.close);
                    }
                    if (getActivity() instanceof MobileActivity)
                        ((MobileActivity)getActivity()).activeTrackEnable(isChecked);
                    break;
                default:
                    break;
            }
        }
    }

    private void setRthHeight(int rthHeight, String successText) {
        if (null == mFlightController)
            return;
        mFlightController.setGoHomeHeightInMeters(rthHeight, djiError -> {
            ToastUtil.showErrorToast(successText, djiError);
        });
    }
    
}