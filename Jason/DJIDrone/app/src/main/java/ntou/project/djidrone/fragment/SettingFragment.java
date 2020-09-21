package ntou.project.djidrone.fragment;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.activetrack.ActiveTrackOperator;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import ntou.project.djidrone.MainActivity;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.DJIApplication;
import ntou.project.djidrone.utils.OthersUtil;
import ntou.project.djidrone.utils.ToastUtil;

public class SettingFragment extends Fragment {

    private static LiveStreamManager liveStreamManager;
    private TextView mTvLiveStream, mTvGestureMode;
    private TextView mTvRetreat, mTvWebSocket;
    private Switch mSwLiveStream, mSwGestureMode;
    private Switch mSwRetreat, mSwWebSocket;
    private ActiveTrackOperator mActiveTrackOperator;
    private FlightController mFlightController;
    //web socket
    private TextView mTvWebSocketTest;
    private Handler webSocketHandler;
    private HandlerThread webSocketHandlerThread;
    private static final int SERVER_PORT = 5000;
    private static final String SERVER_IP = "140.121.198.99";
    private Runnable connect = () -> {
        String statusStr = "";
        Log.d(DJIApplication.TAG, "run runnable->connect");
        try {
            Socket mSocketClient = new Socket(SERVER_IP, SERVER_PORT);
            BufferedReader mBufferedReader = new BufferedReader(new InputStreamReader(mSocketClient.getInputStream()));
            if (mSocketClient.isConnected()) {
                statusStr = "Connect Success";
            } else {
                statusStr = "Connect Fail";
            }
            Log.d(DJIApplication.TAG, statusStr);
            String finalStatusStr = statusStr;
            getActivity().runOnUiThread(() -> {
                ToastUtil.showToast(finalStatusStr);
                mTvWebSocketTest.setText(finalStatusStr);
            });
            while (true) {
                try {
                    String socketData = mBufferedReader.readLine();
                    Log.d(DJIApplication.TAG, "check data");
                    if (socketData != null) {
                        getActivity().runOnUiThread(() -> {
                            getSocketData(socketData);
                            mTvWebSocketTest.setText(socketData);
                        });
                    }
                    PrintWriter printwriter = new PrintWriter(mSocketClient.getOutputStream(), true);
                    printwriter.write("receive data success"); // write the message to output stream
                    printwriter.flush();
//                                printwriter.close();
                } catch (IOException e) {
                    Log.d(DJIApplication.TAG, "read file error");
                    Log.d(DJIApplication.TAG, e.toString());
                    e.printStackTrace();
                }
            }
//            new Timer().schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    try {
//                        tmp = br.readLine();
//                        Log.d(DJIApplication.TAG, "check data");
//                        Log.d(DJIApplication.TAG, tmp);
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                //Do your UI operations like dialog opening or Toast here
//                                tv_show.setText(tmp);
//                            }
//                        });
//                        PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
//                        printwriter.write("receive data success"); // write the message to output stream
//                        printwriter.flush();
////                                printwriter.close();
//                    } catch (IOException e) {
//                        Log.d(DJIApplication.TAG, "read file error");
//                        Log.d(DJIApplication.TAG, e.toString());
//                        e.printStackTrace();
//                    }
//                }
//            }, 0, 100);
        } catch (UnknownHostException e1) {
            statusStr = "Unknown host please make sure IP address";
        } catch (IOException e2) {
            statusStr = "Error Occurred";
        } finally {
            Log.d(DJIApplication.TAG, statusStr);
            String finalStatusStr = statusStr;
            getActivity().runOnUiThread(() -> {
                mTvWebSocketTest.setText(finalStatusStr);
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initListener();
        liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DJIApplication.TAG, "setting onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DJIApplication.TAG, "setting onPause");
    }

    private void initView() {
        mSwLiveStream = getActivity().findViewById(R.id.sw_live_stream);
        mTvLiveStream = getActivity().findViewById(R.id.tv_live_stream);
        mSwGestureMode = getActivity().findViewById(R.id.sw_gesture_mode);
        mTvGestureMode = getActivity().findViewById(R.id.tv_gesture_mode);
        mSwRetreat = getActivity().findViewById(R.id.sw_retreat);
        mTvRetreat = getActivity().findViewById(R.id.tv_retreat);
        mTvWebSocket = getActivity().findViewById(R.id.tv_web_socket);
        mSwWebSocket = getActivity().findViewById(R.id.sw_web_socket);
        mTvWebSocketTest = getActivity().findViewById(R.id.tv_web_socket_test);
        //init
        mActiveTrackOperator = MissionControl.getInstance().getActiveTrackOperator();
        mSwGestureMode.setChecked(mActiveTrackOperator.isGestureModeEnabled());
        //webSocket
        webSocketHandlerThread = new HandlerThread("webSocket");
        webSocketHandlerThread.start();
        webSocketHandler = new Handler(webSocketHandlerThread.getLooper());
    }

    private void initListener() {
        OnToggle onToggle = new OnToggle();
        mSwLiveStream.setOnCheckedChangeListener(onToggle);
        mSwGestureMode.setOnCheckedChangeListener(onToggle);
        mSwRetreat.setOnCheckedChangeListener(onToggle);
        mSwWebSocket.setOnCheckedChangeListener(onToggle);
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
                    mActiveTrackOperator.setGestureModeEnabled(isChecked, djiError -> {
                        ToastUtil.showErrorToast("set gesture mode " + isChecked + " success", djiError);
                        if (null == djiError) {
                            if (isChecked)
                                mTvGestureMode.setText(R.string.open);
                            else
                                mTvGestureMode.setText(R.string.close);
                        } else {
                            mSwGestureMode.setChecked(!isChecked);
                        }
                    });
                    break;
                case R.id.sw_retreat:
                    mActiveTrackOperator.setRetreatEnabled(isChecked, djiError -> {
                        ToastUtil.showErrorToast("set retreat " + isChecked + " success", djiError);
                        if (null == djiError) {
                            if (isChecked)
                                mTvRetreat.setText(R.string.open);
                            else
                                mTvRetreat.setText(R.string.close);
                        } else {
                            mSwRetreat.setChecked(!isChecked);
                        }
                    });
                    break;
                case R.id.sw_web_socket:
                    flightControlEnable(isChecked);
                    if (isChecked) {
                        mTvWebSocket.setText(R.string.open);
//                        webSocketHandlerThread.start();
                        Log.d(DJIApplication.TAG, "sw_web_socket checked");
                        webSocketHandler.post(connect);
                    } else {
                        mTvWebSocket.setText(R.string.close);
//                        webSocketHandlerThread.quitSafely();
                        Log.d(DJIApplication.TAG, "sw_web_socket unchecked");
                        webSocketHandler.removeCallbacks(connect);
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

    private void flightControlEnable(boolean enable) {
        mFlightController = DJIApplication.getFlightControllerInstance();
        if (null == mFlightController)
            return;
        mFlightController.setVirtualStickModeEnabled(enable, djiError -> {
            if (djiError != null) {
                ToastUtil.showToast(djiError.getDescription());
            } else {
                if (enable) {
                    mFlightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
                    mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
                    mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
                    mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
                    ToastUtil.showToast("Enable Virtual Stick Success");
                } else {
                    ToastUtil.showToast("Disable Virtual Stick Success");
                }
            }
        });
    }

    private void getSocketData(String socketData) {
        float distance = 5;
        float mPitch = 0;
        float mRoll = 0;
        float mThrottle = 0;
        float mYaw = 0;
        boolean flag = true;
        switch (socketData) {
            case "w":
            case "W":
                mThrottle = distance;
                break;
            case "a":
            case "A":
                mRoll = -distance;
                break;
            case "s":
            case "S":
                mThrottle = -distance;
                break;
            case "d":
            case "D":
                mRoll = distance;
                break;
            case "q":
            case "Q":
                mYaw = -90;
                break;
            case "e":
            case "E":
                mYaw = 90;
                break;
            default:
                flag = false;
                break;

        }
        if (flag) {
            Log.d(DJIApplication.TAG, socketData);
            if (null != mFlightController) {
                mFlightController.sendVirtualStickFlightControlData(
                        new FlightControlData(mPitch, mRoll, mYaw, mThrottle), djiError -> {
                            if (djiError == null) {
                                ToastUtil.showToast("set data: success");
                            } else {
                                ToastUtil.showToast(djiError.getDescription());
                            }
                        });
            }
        } else {
            String[] cord = socketData.split(",");
            RectF mRectF = new RectF(OthersUtil.parseFloat(cord[0]),
                    OthersUtil.parseFloat(cord[1]),
                    OthersUtil.parseFloat(cord[2]),
                    OthersUtil.parseFloat(cord[3]));
//            Log.d(DJIApplication.TAG,mRectF.left + "\n" +
//                    mRectF.top + "\n" +
//                    mRectF.right + "\n" +
//                    mRectF.bottom + "\n");
            if (getActivity() instanceof MobileActivity)
                ((MobileActivity) getActivity()).drawTestRect(mRectF);
        }
    }
}
