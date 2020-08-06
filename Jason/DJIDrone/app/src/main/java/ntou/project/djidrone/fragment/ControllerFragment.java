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

import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import ntou.project.djidrone.DJIApplication;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.OthersUtil;
import ntou.project.djidrone.utils.ToastUtil;

public class ControllerFragment extends Fragment {

    private Switch mTbtnVirtualStickState;
    private TextView mTvVirtualStickState;
    private EditText mEtRthHeight;
    private int rthHeight;
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
        mTbtnVirtualStickState = view.findViewById(R.id.sw_virtual_stick_state);
        mTvVirtualStickState = view.findViewById(R.id.tv_virtual_stick_state);
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
        mTbtnVirtualStickState.setOnCheckedChangeListener(onToggle);
        mEtRthHeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mFlightController = DJIApplication.getFlightControllerInstance();
                    if (OthersUtil.isNumeric(mEtRthHeight.getText().toString())) {
                        rthHeight = Integer.parseInt(mEtRthHeight.getText().toString());
                        if (rthHeight >= 20 && rthHeight <= 100) {
                            setRthHeight("set rth height success");
                        } else {
                            rthHeight = DEFAULT_RTH_HEIGHT;
                            mEtRthHeight.setText(String.valueOf(rthHeight));
                            setRthHeight("out of rth height bound");
                        }
                    } else {
                        rthHeight = DEFAULT_RTH_HEIGHT;
                        mEtRthHeight.setText(String.valueOf(rthHeight));
                        setRthHeight("not a number");
                    }
                    mFlightController = DJIApplication.getFlightControllerInstance();
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
                default:
                    break;
            }
        }
    }

    private void setRthHeight(String successText) {
        if (null == mFlightController)
            return;
        mFlightController.setGoHomeHeightInMeters(rthHeight, djiError -> {
            ToastUtil.showErrorToast(successText, djiError);
        });
    }
}