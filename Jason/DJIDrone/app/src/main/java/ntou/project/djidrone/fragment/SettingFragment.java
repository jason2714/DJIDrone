package ntou.project.djidrone.fragment;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import dji.common.camera.SettingsDefinitions;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import ntou.project.djidrone.DJIApplication;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.ToastUtil;

public class SettingFragment extends Fragment {

    private static LiveStreamManager liveStreamManager;
    private TextView mTvLiveStream;
    private Switch mSwLiveStream;
    private EditText mEtRthHeight;
    private int rthHeight;
    private FlightController mFlightController;

    public boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

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
        mEtRthHeight = getActivity().findViewById(R.id.et_rth_height);
        initListener();
        liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
    }

    private void initListener() {
        OnToggle onToggle = new OnToggle();
        mSwLiveStream.setOnCheckedChangeListener(onToggle);
        //setting
        mEtRthHeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isNumeric(mEtRthHeight.getText().toString())){
                                rthHeight = Integer.parseInt(mEtRthHeight.getText().toString());
                                if(rthHeight<20 || rthHeight >500){
                                    ToastUtil.showToast("out of rth height bound");
                                    rthHeight = 20;
                                    mEtRthHeight.setText(String.valueOf(rthHeight));
                                }else{
                                    ToastUtil.showToast("set rth height success");
                                }
                            }else{
                                ToastUtil.showToast("not a number");
                                rthHeight = 20;
                                mEtRthHeight.setText(String.valueOf(rthHeight));
                            }
                            mFlightController = DJIApplication.getFlightControllerInstance();
                        }
                    });

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
