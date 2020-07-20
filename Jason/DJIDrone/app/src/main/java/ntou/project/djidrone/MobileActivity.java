

package ntou.project.djidrone;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

import dji.common.battery.BatteryState;
import dji.common.camera.ResolutionAndFrameRate;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import ntou.project.djidrone.fragment.BatteryFragment;
import ntou.project.djidrone.fragment.CameraFragment;
import ntou.project.djidrone.fragment.ControllerFragment;
import ntou.project.djidrone.fragment.MainFragment;
import ntou.project.djidrone.fragment.SensorFragment;
import ntou.project.djidrone.fragment.SettingFragment;
import ntou.project.djidrone.fragment.SignalFragment;
import ntou.project.djidrone.fragment.VideoSurfaceFragment;
import ntou.project.djidrone.utils.DialogUtil;
import ntou.project.djidrone.utils.GoogleMapUtil;
import ntou.project.djidrone.utils.OnScreenJoystick;
import ntou.project.djidrone.utils.ToastUtil;

public class MobileActivity extends FragmentActivity {
    private static final String TAG = MobileActivity.class.getName();
    private static BaseProduct mProduct = null;
    private ConstraintLayout mainLayout, constraintBottom;
    private ToggleButton btn_changeMode, relativeLeftToggle;
    private LinearLayout linearLeft, linearRight;
    private ImageView mBtnCamera;
    private ImageView mBtnTakeoff, mBtnLanding;
    private TextView mTvState, mTvBatteryPower;
    private List<Fragment> fragments;
    private VideoSurfaceFragment mVideoSurfaceFragment,mVideoSurfaceFragmentSmall;
    private Handler mHandler;
    //camera
    private Camera camera = null;
    private String resolutionRatio = "16:9";
    private GestureDetector gestureDetector;
    private FrameLayout mFrameSetting, mapView, droneView;
    private int fragmentPosition;
    public static boolean isRecording = false;
    //map
    private SupportMapFragment gMapFragment, gMapFragmentSmall;
    private GoogleMapUtil gMapUtil, gMapUtilSmall;
    private boolean isMapView = false;
    //battery
    private Battery battery;
    private StringBuffer stringBuffer = new StringBuffer();
    //flightController
    private FlightController flightController;
    private FlightControllerState.Callback flightStateCallback;
    private AlertDialog comfirmLandingDialog;
    //virtual stick
    private OnScreenJoystick mStickLeft,mStickRight;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    //camera
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE|View.SYSTEM_UI_FLAG_LAYOUT_STABLE //隱藏狀態欄和標題欄
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN//全螢幕顯示
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//隱藏手機虛擬按鍵HOME/BACK/LIST按鍵
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);

        IntentFilter filter = new IntentFilter(DJIApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        Toast.makeText(MobileActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
        initViewId();
        initLinstener();
        initUI();
    }

    private void initUI() {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initFlightControllerCallback();
        refreshSDKRelativeUI();
        setFrameRatio();
        setBattery();
        setSensor();
    }

    private void initViewId() {
        mHandler = new Handler(Looper.getMainLooper());
        mainLayout = findViewById(R.id.mainLayout);
        mTvState = findViewById(R.id.tv_state);
        mTvBatteryPower = findViewById(R.id.tv_battery_power);
        btn_changeMode = findViewById(R.id.btn_changeMode);
        linearLeft = findViewById(R.id.linearLeft);
        linearRight = findViewById(R.id.linearRight);
        constraintBottom = findViewById(R.id.constraintBottomBottom);
        relativeLeftToggle = findViewById(R.id.relativeLeftToggle);
        mapView = findViewById(R.id.mapView);
        mBtnCamera = findViewById(R.id.btn_camera);
        mBtnCamera.setTag(R.drawable.icon_shoot_photo);
        droneView = findViewById(R.id.droneView);
        mFrameSetting = findViewById(R.id.container);
        mBtnTakeoff = findViewById(R.id.btn_takeoff);
        mBtnLanding = findViewById(R.id.btn_landing);
        //Virtual Stick
        mStickLeft = findViewById(R.id.leftStick);
        mStickRight = findViewById(R.id.rightStick);
        mVideoSurfaceFragmentSmall = new VideoSurfaceFragment(true);
        mVideoSurfaceFragment = new VideoSurfaceFragment(false);
        gMapFragment = SupportMapFragment.newInstance();
        gMapFragmentSmall = SupportMapFragment.newInstance();
        gMapUtil = new GoogleMapUtil(this, false);
        gMapUtilSmall = new GoogleMapUtil(this, true);
        gMapFragment.getMapAsync(gMapUtil);
        gMapFragmentSmall.getMapAsync(gMapUtilSmall);
        fragments = getFragments();
        fragmentPosition = 0;
        getSupportFragmentManager()//getFragmentManager
                .beginTransaction()//要求 FragmentManager 回傳一個 FragmentTransaction 物件，用以進行 Fragment 的切換。
                .add(mFrameSetting.getId(), fragments.get(0))
                .add(mFrameSetting.getId(), fragments.get(1))
                .add(mFrameSetting.getId(), fragments.get(2))
                .add(mFrameSetting.getId(), fragments.get(3))
                .add(mFrameSetting.getId(), fragments.get(4))
                .add(mFrameSetting.getId(), fragments.get(5))
                .add(mFrameSetting.getId(), fragments.get(6))
                .hide(fragments.get(1))
                .hide(fragments.get(2))
                .hide(fragments.get(3))
                .hide(fragments.get(4))
                .hide(fragments.get(5))
                .hide(fragments.get(6))
                .add(droneView.getId(), mVideoSurfaceFragment)
                .add(droneView.getId(), gMapFragment)
                .hide(gMapFragment)
//                .add(mapView.getId(), mVideoSurfaceFragmentSmall)
                .add(mapView.getId(), gMapFragmentSmall)
//                .hide(mVideoSurfaceFragmentSmall)
                .commit();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initLinstener() {
        Onclick onclick = new Onclick();
        Toggle toggle = new Toggle();
        btn_changeMode.setOnCheckedChangeListener(toggle);
        relativeLeftToggle.setOnCheckedChangeListener(toggle);
        //test
        relativeLeftToggle.setVisibility(View.GONE);
        //test
//        stickRight.setOnClickListener(onclick);
//        stickLeft.setOnClickListener(onclick);
        mBtnCamera.setOnClickListener(onclick);
        mapView.setOnClickListener(onclick);
        mBtnTakeoff.setOnClickListener(onclick);
        mBtnLanding.setOnClickListener(onclick);
        //滑動返回main fragment
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int FLING_MIN_DISTANCE = mFrameSetting.getWidth() / 2, FLING_MIN_VELOCITY = 100;//長度一半
                if (fragmentPosition == 0) {
                    Log.d(TAG, "on main fragment");
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
                if (e1.getX() - e2.getX() < -FLING_MIN_DISTANCE
                        && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
//                    new Thread(() -> {
//                        try {
//                            fragmentPosition = 0;
//                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
//                        } catch (Exception e) {
//                            Log.e(TAG, e.toString());
//                        }
//                    }).start();
                    changeFragment(0);
                    Log.d(TAG, "back to main");
                }
                Log.d(TAG, "onFling");
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        mFrameSetting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !gestureDetector.onTouchEvent(event);
            }
        });
    }

    private class Toggle implements CompoundButton.OnCheckedChangeListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ConstraintSet layoutMainSet = new ConstraintSet();
            layoutMainSet.clone(mainLayout);
            TransitionManager.beginDelayedTransition(mainLayout);
            switch (buttonView.getId()) {
                case R.id.btn_changeMode:
//                    TransitionManager.beginDelayedTransition(mainLayout);
                    if (isChecked) {
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                        layoutMainSet.connect(mapView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        layoutMainSet.clear(mapView.getId(), ConstraintSet.RIGHT);
                        layoutMainSet.applyTo(mainLayout);
                        mStickLeft.setVisibility(View.GONE);
                        mStickRight.setVisibility(View.GONE);
                        constraintBottom.setVisibility(View.GONE);
                        linearRight.setVisibility(View.GONE);
                    } else {
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.RIGHT, R.id.gdlnvtRight, ConstraintSet.LEFT);
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.BOTTOM, R.id.gdlnhzBottom, ConstraintSet.TOP);
                        layoutMainSet.connect(mapView.getId(), ConstraintSet.RIGHT, R.id.gdlnvtMap, ConstraintSet.LEFT);
                        layoutMainSet.clear(mapView.getId(), ConstraintSet.LEFT);
                        layoutMainSet.applyTo(mainLayout);
                        linearRight.setVisibility(View.VISIBLE);
                        constraintBottom.setVisibility(View.VISIBLE);
                        mStickLeft.setVisibility(View.VISIBLE);
                        mStickRight.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.relativeLeftToggle:
                    if (isChecked) {
                        layoutMainSet.connect(relativeLeftToggle.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        layoutMainSet.clear(relativeLeftToggle.getId(), ConstraintSet.RIGHT);
                        layoutMainSet.applyTo(mainLayout);
                        linearLeft.setVisibility(View.GONE);
                    } else {
                        layoutMainSet.connect(relativeLeftToggle.getId(), ConstraintSet.RIGHT, R.id.gdlnvtLeft, ConstraintSet.RIGHT);
                        layoutMainSet.clear(relativeLeftToggle.getId(), ConstraintSet.LEFT);
                        layoutMainSet.applyTo(mainLayout);
                        linearLeft.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class Onclick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_camera:
                    if ((Integer) mBtnCamera.getTag() == R.drawable.icon_shoot_photo) {
                        captureAction();
                    } else if ((Integer) mBtnCamera.getTag() == R.drawable.icon_record_video) {
                        ToggleButton mTbtnCameraMode = findViewById(R.id.tbtn_camera_mode);
                        if (null != mTbtnCameraMode)
                            mTbtnCameraMode.setEnabled(isRecording);
                        isRecording = !isRecording;
                        if (isRecording) {
                            startRecord();
                        } else {
                            stopRecord();
                        }
                    }
                    break;
                case R.id.mapView:
                    Log.d(TAG, "change fragment");
                    changeMapFragment();
                    break;
                case R.id.btn_takeoff:
                    DialogUtil.showDialogExceptActionBar(new AlertDialog.Builder(MobileActivity.this,R.style.set_dialog)
                            .setTitle("Confirm takeoff")
                            .setMessage("確定要起飛嗎")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startTakeoff();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create());
                    break;
                case R.id.btn_landing:
                    DialogUtil.showDialog(MobileActivity.this, "start landing");
                    startLanding();
                    break;
                default:
                    break;
            }
        }
    }

    private void initFlightControllerCallback() {
        flightController = DJIApplication.getFlightControllerInstance();
        if (null == flightController){
            flightStateCallback = null;
            return;
        }

        flightStateCallback = new FlightControllerState.Callback() {
            @Override
            public void onUpdate(FlightControllerState flightControllerState) {
                gMapUtil.initFlightController(flightControllerState);
                if (flightControllerState.isLandingConfirmationNeeded()) {
                    if (comfirmLandingDialog == null) {
                        comfirmLandingDialog = new AlertDialog.Builder(MobileActivity.this)
                                .setTitle("Confirm landing")
                                .setMessage("確定要降落嗎")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        flightController.confirmLanding(new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {
                                                comfirmLandingDialog = null;
                                                ToastUtil.showErrorToast("降落成功", djiError);
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        flightController.cancelLanding(new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {
                                                comfirmLandingDialog = null;
                                                ToastUtil.showErrorToast("取消降落成功", djiError);
                                            }
                                        });
                                    }
                                }).create();
                        DialogUtil.showDialogExceptActionBar(comfirmLandingDialog);
                    }

                }
            }
        };

    }

    public void triggerOnMapClick() {
        mapView.performClick();
    }

    public void changeFragment(int position) {
//        fragmentPosition = position;
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(mFrameSetting.getId(), fragments.get(position))
//                .addToBackStack(null)
//                .commit();
        getSupportFragmentManager()
                .beginTransaction()
                .hide(fragments.get(fragmentPosition))
                .show(fragments.get(position))
                .commit();
        fragmentPosition = position;
    }

    private void changeMapFragment() {
        isMapView = !isMapView;
        if (isMapView) {
            getSupportFragmentManager()//getFragmentManager
                    .beginTransaction()//要求 FragmentManager 回傳一個 FragmentTransaction 物件，用以進行 Fragment 的切換。
                    .hide(gMapFragmentSmall)
//                    .hide(mVideoSurfaceFragment)
                    .remove(mVideoSurfaceFragment)
                    .show(gMapFragment)
//                    .show(mVideoSurfaceFragmentSmall)
                    .add(mapView.getId(), mVideoSurfaceFragmentSmall)
                    .commit();
        } else {
            getSupportFragmentManager()//getFragmentManager
                    .beginTransaction()//要求 FragmentManager 回傳一個 FragmentTransaction 物件，用以進行 Fragment 的切換。
                    .hide(gMapFragment)
                    .remove(mVideoSurfaceFragmentSmall)
                    .show(gMapFragmentSmall)
                    .add(droneView.getId(),mVideoSurfaceFragment)
                    .commit();
        }
    }

    private void captureAction() {
        camera = DJIApplication.getCameraInstance();
        if (camera != null) {
            camera.setShootPhotoMode(CameraFragment.getPhotoMode(), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            ToastUtil.showToast("take photo: success");
                                        } else {
                                            ToastUtil.showToast(djiError.getDescription());
                                        }
                                    }
                                });
                            }
                        }, 1000);
                    }
                }
            });
        }
    }

    // Method for starting recording
    private void startRecord() {
        camera = DJIApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBtnCamera.setImageResource(R.drawable.icon_recording);
                                ToastUtil.showToast("Record video: success");
                            }
                        }, 500);
                    } else {
                        ToastUtil.showToast(djiError.getDescription());
                    }
                }
            }); // Execute the startRecordVideo API
        } else {
            mBtnCamera.setImageResource(R.drawable.icon_recording);
        }
    }

    // Method for stopping recording
    private void stopRecord() {
        camera = DJIApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBtnCamera.setImageResource(R.drawable.icon_record_video);
                                ToastUtil.showToast("Stop recording: success");
                            }
                        }, 500);
                    } else {
                        ToastUtil.showToast(djiError.getDescription());
                    }
                }
            }); // Execute the stopRecordVideo API
        } else {
            mBtnCamera.setImageResource(R.drawable.icon_record_video);
        }
    }

    private List<Fragment> getFragments() {
        List<Fragment> newFragments = new ArrayList<>();
        newFragments.add(new MainFragment());
        newFragments.add(new BatteryFragment());
        newFragments.add(new SensorFragment());
        newFragments.add(new SignalFragment());
        newFragments.add(new ControllerFragment());
        newFragments.add(new CameraFragment());
        newFragments.add(new SettingFragment());
        return newFragments;
    }

    private void onProductConnectionChange() {
        initUI();
    }

    private void refreshSDKRelativeUI() {
        mProduct = DJIApplication.getProductInstance();
        if (null != mProduct) {
            if (mProduct.isConnected()) {
                Log.d(TAG, "connect to icon_aircraft");
                mTvState.setText(R.string.connected);
                SettingFragment.setLiveStreamUrl(getResources().getString(R.string.RTMP_url));
                //google map
                flightController = DJIApplication.getFlightControllerInstance();
                if (null != flightController)
                    flightController.setStateCallback(flightStateCallback);
            } else if (mProduct instanceof Aircraft) {
                Log.d(TAG, "only connect to remote controller");
                mTvState.setText(R.string.connected_remote_control);
                SettingFragment.setLiveStreamUrl(null);
                //google map
                gMapUtil.unInitFlightController();
            }
        } else {
            Log.d(TAG, "product disconnected");
            SettingFragment.setLiveStreamUrl(null);
            mTvState.setText(R.string.disconnected);
        }
    }

    private void setFrameRatio() {
        camera = DJIApplication.getCameraInstance();
        ConstraintSet layoutMainSet = new ConstraintSet();
        layoutMainSet.clone(mainLayout);
        if (null != camera) {
            ResolutionAndFrameRate[] resolutionAndFrameRates =
                    camera.getCapabilities().videoResolutionAndFrameRateRange();
            String width = resolutionAndFrameRates[0].getResolution().toString().split("[_]")[1].split("[x]")[0];
            String height = resolutionAndFrameRates[0].getResolution().toString().split("[_]")[1].split("[x]")[1];
            resolutionRatio = width + ":" + height;
            Log.d(TAG, "" + resolutionAndFrameRates[0].getResolution());
        }
        layoutMainSet.setDimensionRatio(R.id.droneView, resolutionRatio);
        layoutMainSet.setDimensionRatio(mapView.getId(), resolutionRatio);
        layoutMainSet.applyTo(mainLayout);
    }

    private void setBattery() {
        battery = DJIApplication.getBatteryInstance();
        if (null != battery) {
            //TODO fail
//            battery.getCellVoltages(new CommonCallbacks.CompletionCallbackWith<Integer[]>() {
//                @Override
//                public void onSuccess(Integer[] integers) {
//                    ToastUtil.showToast("cell voltages" + integers);
//                }
//
//                @Override
//                public void onFailure(DJIError djiError) {
//                    ToastUtil.showToast(djiError.getDescription());
//                }
//            });
//            battery.setLevel1CellVoltageThreshold(3600, new CommonCallbacks.CompletionCallback() {
//                @Override
//                public void onResult(DJIError djiError) {
//                    if (djiError == null) {
//                        ToastUtil.showToast("set level1 threshold" + 3600);
//                    } else {
//                        ToastUtil.showToast(djiError.getDescription());
//                    }
//                }
//            });
//            battery.getLevel1CellVoltageThreshold(new CommonCallbacks.CompletionCallbackWith<Integer>() {
//                @Override
//                public void onSuccess(Integer integer) {
//                    ToastUtil.showToast("level1 threshold" + integer);
//                }
//
//                @Override
//                public void onFailure(DJIError djiError) {
//                    ToastUtil.showToast(djiError.getDescription());
//                }
//            });
            battery.setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState batteryState) {
//                    stringBuffer.append(batteryState.getChargeRemainingInPercent())
//                            .append("%");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            mTvBatteryPower.setText(stringBuffer.toString());
                            mTvBatteryPower.setText(batteryState.getChargeRemainingInPercent() + "%");
                        }
                    });
//                    ToastUtil.showToast("ChargeRemainingInPercent:" + batteryState.getChargeRemainingInPercent());
//                    ToastUtil.showToast("ChargeRemaining:" + batteryState.getChargeRemaining());
//                    ToastUtil.showToast("FullChargeCapacity:" + batteryState.getFullChargeCapacity());
//                    ToastUtil.showToast("Temperature:" + batteryState.getTemperature());
//                    ToastUtil.showToast("isBeingCharged:" + batteryState.isBeingCharged());
//                    ToastUtil.showToast("getCurrent:" + batteryState.getCurrent());
//                    ToastUtil.showToast("getConnectionState:" + batteryState.getConnectionState());
//                    ToastUtil.showToast("getVoltage:" + batteryState.getVoltage());
//                    ToastUtil.showToast("getLifetimeRemaining:" + batteryState.getLifetimeRemaining());
                }
            });
        } else {
            Log.d(DJIApplication.TAG, "battery = null");
        }
    }

    private void setSignal() {
        flightController = DJIApplication.getFlightControllerInstance();
//        if (null != flightController) {
//            flightController.setStateCallback(new FlightControllerState.Callback() {
//                @Override
//                public void onUpdate(FlightControllerState flightControllerState) {
//                    flightControllerState.getGPSSignalLevel();
//                }
//            });
//        }
    }

    private void setSensor() {
    }

    private void startTakeoff() {
        flightController = DJIApplication.getFlightControllerInstance();
        if (null == flightController)
            return;
        if (!flightController.getState().isFlying()) {
            flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
//                    DialogUtil.showDialogBasedOnError(MobileActivity.this,djiError);
                    if (null == djiError) {
                        ToastUtil.showToast("takeoff success");
                    } else {
                        ToastUtil.showToast(djiError.getDescription());
                    }
                }
            });
        }
    }

    private void startLanding() {
        flightController = DJIApplication.getFlightControllerInstance();
        if (null == flightController)
            return;
        if (flightController.getState().isFlying()) {
            flightController.startLanding(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
//                    DialogUtil.showDialogBasedOnError(MobileActivity.this,djiError);
                    if (null == djiError) {
                        ToastUtil.showToast("landing success");
                    } else {
                        ToastUtil.showToast(djiError.getDescription());
                    }
                }
            });
        }
    }

}

