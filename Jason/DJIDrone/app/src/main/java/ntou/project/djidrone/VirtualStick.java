package ntou.project.djidrone;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import ntou.project.djidrone.listener.OnScreenJoystickListener;
import ntou.project.djidrone.utils.ToastUtil;

public class VirtualStick {
    //virtual stick
    private OnScreenJoystick mStickLeft, mStickRight;
    private FlightController mFlightController;
    private Timer mSendVirtualStickDataTimer;
    private SendVirtualStickDataTask mSendVirtualStickDataTask;

    private float mPitch;
    private float mRoll;
    private float mYaw;
    private float mThrottle;

    public VirtualStick(Activity activity) {
        mStickLeft = activity.findViewById(R.id.leftStick);
        mStickRight = activity.findViewById(R.id.rightStick);
        initListener();
        initFlightController();
    }

    public void setStickVisible(boolean visible) {
        if (visible) {
            mStickLeft.setVisibility(View.VISIBLE);
            mStickRight.setVisibility(View.VISIBLE);
        } else {
            mStickLeft.setVisibility(View.INVISIBLE);
            mStickRight.setVisibility(View.INVISIBLE);
        }
    }

    private void initListener() {
        mStickRight.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float pitchJoyControlMaxSpeed = 10;
                float rollJoyControlMaxSpeed = 10;

                mPitch = (float) (pitchJoyControlMaxSpeed * pX);

                mRoll = (float) (rollJoyControlMaxSpeed * pY);

                if (null == mSendVirtualStickDataTimer) {
                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                    mSendVirtualStickDataTimer = new Timer();
                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);
                }

            }

        });

        mStickLeft.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float verticalJoyControlMaxSpeed = 2;
                float yawJoyControlMaxSpeed = 30;

                mYaw = (float) (yawJoyControlMaxSpeed * pX);
                mThrottle = (float) (verticalJoyControlMaxSpeed * pY);

                if (null == mSendVirtualStickDataTimer) {
                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                    mSendVirtualStickDataTimer = new Timer();
                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
                }

            }
        });
    }

    private void initFlightController() {
        Aircraft aircraft = DJIApplication.getAircraftInstance();
        if (aircraft == null || !aircraft.isConnected()) {
            mFlightController = null;
            return;
        }
        mFlightController = DJIApplication.getFlightControllerInstance();
        mFlightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        mFlightController.getSimulator().setStateCallback(new SimulatorState.Callback() {
            @Override
            public void onUpdate(final SimulatorState stateData) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        String yaw = String.format("%.2f", stateData.getYaw());
                        String pitch = String.format("%.2f", stateData.getPitch());
                        String roll = String.format("%.2f", stateData.getRoll());
                        String positionX = String.format("%.2f", stateData.getPositionX());
                        String positionY = String.format("%.2f", stateData.getPositionY());
                        String positionZ = String.format("%.2f", stateData.getPositionZ());

                        ToastUtil.showToast("Yaw : " + yaw + ", Pitch : " + pitch + ", Roll : " + roll + "\n" + ", PosX : " + positionX +
                                ", PosY : " + positionY +
                                ", PosZ : " + positionZ);
                    }
                });
            }
        });

    }

    class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            if (mFlightController != null) {
                mFlightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                mPitch, mRoll, mYaw, mThrottle
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
//                                if (djiError == null) {
//                                    ToastUtil.showToast("set data: success");
//                                } else {
//                                    ToastUtil.showToast(djiError.getDescription());
//                                }
                            }
                        }
                );
            }
        }
    }

    public void virtualStickEnable(boolean enable) {
        if (null == mFlightController)
            return;
        mFlightController.setVirtualStickModeEnabled(enable, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showToast(djiError.getDescription());
                } else {
                    if (enable)
                        ToastUtil.showToast("Enable Virtual Stick Success");
                    else
                        ToastUtil.showToast("Disable Virtual Stick Success");
                }
            }
        });
    }
}
