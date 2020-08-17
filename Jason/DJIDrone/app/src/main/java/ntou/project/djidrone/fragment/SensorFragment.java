package ntou.project.djidrone.fragment;

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
import androidx.fragment.app.Fragment;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import ntou.project.djidrone.DJIApplication;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.ToastUtil;

public class SensorFragment extends Fragment {
    private Switch mSwAvoidance;
    private TextView mTvAvoidance;
    private FlightController flightController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnToggle onToggle = new OnToggle();
        mSwAvoidance = view.findViewById(R.id.sw_avoidance_mode);
        mTvAvoidance = view.findViewById(R.id.tv_avoidance_mode);
        flightController = DJIApplication.getFlightControllerInstance();
        setCollisionAvoidance(true);
        mSwAvoidance.setOnCheckedChangeListener(onToggle);
    }

    private class OnToggle implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.sw_avoidance_mode:
                    setCollisionAvoidance(isChecked);
                    break;
                default:
                    break;
            }
        }
    }

    private void setCollisionAvoidance(boolean isOn) {
        flightController = DJIApplication.getFlightControllerInstance();
        if (null == flightController) {
            if (isOn)
                mTvAvoidance.setText(R.string.open);
            else
                mTvAvoidance.setText(R.string.close);
            return;
        }
        FlightAssistant flightAssistant = flightController.getFlightAssistant();
        if (null != flightAssistant) {
            CommonCallbacks.CompletionCallback completionCallback = djiError -> {
                ToastUtil.showErrorToast("set avoidance " + isOn + " success", djiError);
                if (null == djiError) {
                    getActivity().runOnUiThread(() -> {
                        if (isOn)
                            mTvAvoidance.setText(R.string.open);
                        else
                            mTvAvoidance.setText(R.string.close);
                    });
                }
            };
            flightAssistant.setCollisionAvoidanceEnabled(isOn, completionCallback);
            flightAssistant.setActiveObstacleAvoidanceEnabled(isOn, completionCallback);
        }
    }
}
