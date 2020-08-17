package ntou.project.djidrone.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import dji.sdk.mission.MissionControl;
import dji.sdk.mission.activetrack.ActiveTrackOperator;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.ToastUtil;

public class SettingFragment extends Fragment {

    private static LiveStreamManager liveStreamManager;
    private TextView mTvLiveStream, mTvGestureMode;
    private Switch mSwLiveStream, mSwGestureMode;
    private ActiveTrackOperator mActiveTrackOperator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwLiveStream = getActivity().findViewById(R.id.sw_live_stream);
        mTvLiveStream = getActivity().findViewById(R.id.tv_live_stream);
        mSwGestureMode = getActivity().findViewById(R.id.sw_gesture_mode);
        mTvGestureMode = getActivity().findViewById(R.id.tv_gesture_mode);
        initListener();
        liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
    }

    private void initListener() {
        OnToggle onToggle = new OnToggle();
        mActiveTrackOperator = MissionControl.getInstance().getActiveTrackOperator();
        if(mActiveTrackOperator != null)
            mSwGestureMode.setChecked(mActiveTrackOperator.isGestureModeEnabled());
        mSwLiveStream.setOnCheckedChangeListener(onToggle);
        mSwGestureMode.setOnCheckedChangeListener(onToggle);
    }

    private class OnToggle implements CompoundButton.OnCheckedChangeListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.sw_live_stream:
                    liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
                    if (isChecked) {
                        mTvLiveStream.setText(R.string.open);
                    } else {
                        mTvLiveStream.setText(R.string.close);
                    }
                    if (null != liveStreamManager) {//已連接
                        if (liveStreamManager.getLiveUrl() == null) {
                            ToastUtil.showToast("null");
                        } else {
                            if (isChecked) {
                                liveStreamManager.startStream();
                                ToastUtil.showToast("live stream open success");
                            } else {
                                liveStreamManager.stopStream();
                                ToastUtil.showToast("live stream close success");
                            }
                        }
                    } else {//未連接
                        ToastUtil.showToast("live stream manager = null");
                    }
                    break;
                    case R.id.sw_gesture_mode:
                        mActiveTrackOperator = MissionControl.getInstance().getActiveTrackOperator();
                        if(mActiveTrackOperator != null){
                            mActiveTrackOperator.setGestureModeEnabled(isChecked,djiError -> {
                                ToastUtil.showErrorToast("set gesture mode " + isChecked + " success",djiError);
                                if(null == djiError){
                                    if(isChecked)
                                        mTvGestureMode.setText(R.string.open);
                                    else
                                        mTvGestureMode.setText(R.string.close);
                                }
                            });
                        }else{
                            if(isChecked)
                                mTvGestureMode.setText(R.string.open);
                            else
                                mTvGestureMode.setText(R.string.close);
                        }
                        break;
                default:
                    break;
            }
        }
    }

    public static void setLiveStreamUrl(String liveStreamUrl) {
        liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        if (null != liveStreamManager) {//已連接
            liveStreamManager.setLiveUrl(liveStreamUrl);
        } else {
            ToastUtil.showToast("live stream manager = null");
        }
    }

}
