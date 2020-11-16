package ntou.project.djidrone.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.utils.DJIApplication;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.ToastUtil;

public class SensorFragment extends Fragment {
    private Switch mSwAvoidance;
    private Switch mSwRTHAvoidance;
    private Switch mSwLandingProtection;
    private Switch mSwAPAS;
    private TextView mTvAvoidance;
    private TextView mTvRTHAvoidance;
    private TextView mTvLandingProtection;
    private TextView mTvAPAS;
    private FlightController mFlightController;
    private Compass compass;
    private FlightAssistant mFlightAssistant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFlightController = DJIApplication.getFlightControllerInstance();
        initView();
        initListener();
        //TODO downward landing protection

        //TODO compass calibration
        if (null != mFlightController) {
            compass = mFlightController.getCompass();
            if (compass.hasError()) {
//            compass.startCalibration(djiError -> {
//            });
            }
        }
    }

    private void initView() {
        mTvAvoidance = getActivity().findViewById(R.id.tv_avoidance);
        mTvRTHAvoidance = getActivity().findViewById(R.id.tv_RTH_avoidance);
        mTvLandingProtection = getActivity().findViewById(R.id.tv_landing_protection);
        mTvAPAS = getActivity().findViewById(R.id.tv_APAS);
        mSwAvoidance = getActivity().findViewById(R.id.sw_avoidance);
        mSwRTHAvoidance = getActivity().findViewById(R.id.sw_RTH_avoidance);
        mSwLandingProtection = getActivity().findViewById(R.id.sw_landing_protection);
        mSwAPAS = getActivity().findViewById(R.id.sw_APAS);
    }

    private void initListener() {
        OnToggle onToggle = new OnToggle();
        mSwAvoidance.setOnCheckedChangeListener(onToggle);
        mSwAPAS.setOnCheckedChangeListener(onToggle);
        mSwRTHAvoidance.setOnCheckedChangeListener(onToggle);
        mSwLandingProtection.setOnCheckedChangeListener(onToggle);
    }

    private class OnToggle implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.sw_avoidance:
                    setCollisionAvoidance(isChecked);
                    break;
                case R.id.sw_APAS:
                    setAPAS(isChecked);
                    break;
                case R.id.sw_RTH_avoidance:
                    setRTHAvoidance(isChecked);
                    break;
                case R.id.sw_landing_protection:
                    setLandingProtection(isChecked);
                    break;
                default:
                    break;
            }
        }
    }

    private void setCollisionAvoidance(boolean isOn) {
        mFlightController = DJIApplication.getFlightControllerInstance();
        if (null == mFlightController) {
            mSwAvoidance.setChecked(!isOn);
            return;
        }
        mFlightAssistant = mFlightController.getFlightAssistant();
        if (null != mFlightAssistant) {
            CommonCallbacks.CompletionCallback completionCallback = djiError -> {
                ToastUtil.showErrorToast("set avoidance " + isOn + " success", djiError);
                if (null == djiError) {
                    String[] flightModes = getResources().getStringArray(R.array.mavic_pro2_mode);
                    getActivity().runOnUiThread(() -> {
                        if(getActivity() instanceof MobileActivity)
                            ((MobileActivity)getActivity()).getAvoidanceState(isOn);
                        if (isOn){
                            mTvAvoidance.setText(R.string.open);
                        }
                        else{
                            mTvAvoidance.setText(R.string.close);
                        }
                    });
                }
            };
            mFlightAssistant.setCollisionAvoidanceEnabled(isOn, completionCallback);
            mFlightAssistant.setActiveObstacleAvoidanceEnabled(isOn, completionCallback);
        }
    }

    private void setAPAS(boolean isOn) {
        mFlightController = DJIApplication.getFlightControllerInstance();
        if (null == mFlightController) {
            mSwAPAS.setChecked(!isOn);
            return;
        }
        mFlightAssistant = mFlightController.getFlightAssistant();
        if (null != mFlightAssistant) {
            mFlightAssistant.setAdvancedPilotAssistanceSystemEnabled(isOn, djiError -> {
                getActivity().runOnUiThread(() -> {
                    ToastUtil.showErrorToast("set APAS " + isOn + " success", djiError);
                    if (null == djiError) {
                        if (isOn)
                            mTvAPAS.setText(R.string.open);
                        else
                            mTvAPAS.setText(R.string.close);
                    }
                });
            });
        }
    }

    private void setRTHAvoidance(boolean isOn) {
        mFlightController = DJIApplication.getFlightControllerInstance();
        if (null == mFlightController) {
            mSwRTHAvoidance.setChecked(!isOn);
            return;
        }
        mFlightAssistant = mFlightController.getFlightAssistant();
        if (null != mFlightAssistant) {
            mFlightAssistant.setRTHObstacleAvoidanceEnabled(isOn, djiError -> {
                getActivity().runOnUiThread(() -> {
                    ToastUtil.showErrorToast("set RTH Avoidance " + isOn + " success", djiError);
                    if (null == djiError) {
                        if (isOn)
                            mTvRTHAvoidance.setText(R.string.open);
                        else
                            mTvRTHAvoidance.setText(R.string.close);
                    }
                });
            });
        }
    }

    private void setLandingProtection(boolean isOn){
        mFlightController = DJIApplication.getFlightControllerInstance();
        if (null == mFlightController) {
            mSwLandingProtection.setChecked(!isOn);
            return;
        }
        mFlightAssistant = mFlightController.getFlightAssistant();
        if (null != mFlightAssistant) {
            mFlightAssistant.setLandingProtectionEnabled(isOn, djiError -> {
                getActivity().runOnUiThread(() -> {
                    ToastUtil.showErrorToast("set Landing Protection " + isOn + " success", djiError);
                    if (null == djiError) {
                        if (isOn)
                            mTvLandingProtection.setText(R.string.open);
                        else
                            mTvLandingProtection.setText(R.string.close);
                    }
                });
            });
        }
    }
}
