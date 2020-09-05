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
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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
import dji.common.mission.activetrack.ActiveTrackMission;
import dji.common.mission.activetrack.ActiveTrackMode;
import dji.common.mission.activetrack.ActiveTrackTargetState;
import dji.common.mission.activetrack.ActiveTrackTrackingState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.activetrack.ActiveTrackMissionOperatorListener;
import dji.sdk.mission.activetrack.ActiveTrackOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import ntou.project.djidrone.fragment.BatteryFragment;
import ntou.project.djidrone.fragment.CameraFragment;
import ntou.project.djidrone.fragment.ControllerFragment;
import ntou.project.djidrone.fragment.MainFragment;
import ntou.project.djidrone.fragment.SensorFragment;
import ntou.project.djidrone.fragment.SettingFragment;
import ntou.project.djidrone.fragment.SignalFragment;
import ntou.project.djidrone.fragment.VideoSurfaceFragment;
import ntou.project.djidrone.utils.DJIApplication;
import ntou.project.djidrone.utils.DialogUtil;
import ntou.project.djidrone.utils.GoogleMapUtil;
import ntou.project.djidrone.utils.OthersUtil;
import ntou.project.djidrone.utils.ToastUtil;

public class MobileActivity extends FragmentActivity {
    //WindowSet
    private View decorView;
    //MainFragment Width
    private Size mFragmentFrameSize;
    private static final String TAG = MobileActivity.class.getName();
    private static BaseProduct mProduct = null;
    private ConstraintLayout mainLayout, constraintBottom;
    private ToggleButton btn_changeMode, relativeLeftToggle;
    private LinearLayout linearLeft, linearRight;
    private ImageView mImgSensor;
    private ImageView mBtnCamera;
    private ImageView mBtnTakeoffLanding, mBtnRTH;
    private TextView mTvState, mTvBatteryPower;
    //test data
    private TextView mTvTest;
    private StringBuffer stringBuffer;
    //test data
    private List<Fragment> fragments;
    private VideoSurfaceFragment mVideoSurfaceFragment, mVideoSurfaceFragmentSmall;
    private Handler mHandler;
    //camera
    private Camera camera = null;
    private String resolutionRatio = "16:9";
    public static GestureDetector gestureDetector;
    private FrameLayout mFragmentFrame, mapView, droneView;
    public static boolean isRecording = false;
    //signal
    private ImageView mImgSignal;
    private final int[] signalLevelDrawble = new int[]{R.drawable.icon_signal_level0, R.drawable.icon_signal_level1
            , R.drawable.icon_signal_level2, R.drawable.icon_signal_level3
            , R.drawable.icon_signal_level4, R.drawable.icon_signal_level5};

    //map
    private SupportMapFragment gMapFragment, gMapFragmentSmall;
    private GoogleMapUtil gMapUtil, gMapUtilSmall;
    private boolean isMapView = false;
    //battery
    private Battery battery;
    //flightController
    private FlightController flightController;
    private FlightControllerState.Callback flightStateCallback;
    private AlertDialog confirmLandingDialog;
    public VirtualStick mVirtualStick;
    private String[] flightModes;
    private boolean isFlying, isRTH;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };
    private int fragmentPosition;
    //Active Track
//    ActiveTrack mActiveTrack = null;
    private boolean isDrawingRect;
    private double startX, startY;
    private ImageView mImgSelectRect;
    private ImageView mImgTargetRect;
    private ActiveTrackOperator mActiveTrackOperator;
    private AlertDialog confirmActiveTrackDialog;

    //camera
    @Override
    protected void onResume() {
        super.onResume();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE //隱藏狀態欄和標題欄
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN//全螢幕顯示
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//隱藏手機虛擬按鍵HOME/BACK/LIST按鍵
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//隱藏手機虛擬按鍵HOME/BACK/LIST按鍵
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//讓navigation bar 過一段時間自動隱藏
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {//called when UI change
                if (visibility == 0) {//當navigation bar顯示時會=0
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//隱藏手機虛擬按鍵HOME/BACK/LIST按鍵
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//讓navigation bar 過一段時間自動隱藏
                    //只設hide的話會蓋掉immersive
                }
            });
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

        initViewId();
        initLinstener();
        initActiveTrack();
        initFlightControllerCallback();
        initUI();
    }

    private void initUI() {
        refreshSDKRelativeUI();
        setFrameRatio();
        setBattery();
    }

    private void initViewId() {
        flightModes = getResources().getStringArray(R.array.mavic_pro2_mode);
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
        mFragmentFrame = findViewById(R.id.container);
        mBtnTakeoffLanding = findViewById(R.id.btn_takeoff_landing);
        mBtnRTH = findViewById(R.id.btn_rth);
        mImgSensor = findViewById(R.id.sensorIcon);
        mImgSignal = findViewById(R.id.img_sensorIcon);
        //test
        mTvTest = findViewById(R.id.tv_test);
        //Virtual Stick
        mVirtualStick = new VirtualStick(this);
        //Active Track
//        mActiveTrack = new ActiveTrack();
        mImgSelectRect = findViewById(R.id.img_active_track_select_rect);
        mImgTargetRect = findViewById(R.id.img_active_track_target_rect);
        //setting
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
                .add(mFragmentFrame.getId(), fragments.get(0))
                .add(mFragmentFrame.getId(), fragments.get(1))
                .add(mFragmentFrame.getId(), fragments.get(2))
                .add(mFragmentFrame.getId(), fragments.get(3))
                .add(mFragmentFrame.getId(), fragments.get(4))
                .add(mFragmentFrame.getId(), fragments.get(5))
                .add(mFragmentFrame.getId(), fragments.get(6))
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
        mBtnCamera.setOnClickListener(onclick);
        mapView.setOnClickListener(onclick);
        mBtnTakeoffLanding.setOnClickListener(onclick);
        mBtnRTH.setOnClickListener(onclick);
        //用post就會排在queue後面執行 => 布局完成後才getWidth()
        mFragmentFrame.post(() -> {
            mFragmentFrameSize = new Size(mFragmentFrame.getWidth(),mFragmentFrame.getHeight());
            ((MainFragment)fragments.get(0)).setGridLLayout(mFragmentFrameSize);
            gestureDetector = new GestureDetector(MobileActivity.this, new GestureListener(mFragmentFrameSize.getWidth(), 50, 100));
        });
//        已在scrollview override
//        mFragmentFrame.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//        });

    }

    private void initActiveTrack() {
        mActiveTrackOperator = MissionControl.getInstance().getActiveTrackOperator();

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
                        constraintBottom.setVisibility(View.INVISIBLE);
                        linearRight.setVisibility(View.INVISIBLE);
                        mVirtualStick.setStickVisible(false);
                    } else {
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.RIGHT, R.id.gdlnvtRight, ConstraintSet.LEFT);
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.BOTTOM, R.id.gdlnhzBottom, ConstraintSet.TOP);
                        layoutMainSet.connect(mapView.getId(), ConstraintSet.RIGHT, R.id.gdlnvtMap, ConstraintSet.LEFT);
                        layoutMainSet.clear(mapView.getId(), ConstraintSet.LEFT);
                        layoutMainSet.applyTo(mainLayout);
                        linearRight.setVisibility(View.VISIBLE);
                        constraintBottom.setVisibility(View.VISIBLE);
                        mVirtualStick.setStickVisible(true);
                    }
                    break;
                case R.id.relativeLeftToggle:
                    if (isChecked) {
                        layoutMainSet.connect(relativeLeftToggle.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        layoutMainSet.clear(relativeLeftToggle.getId(), ConstraintSet.RIGHT);
                        layoutMainSet.applyTo(mainLayout);
                        linearLeft.setVisibility(View.INVISIBLE);
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
                        Switch mSwCameraMode = findViewById(R.id.sw_camera_mode);
                        if (null != mSwCameraMode)
                            mSwCameraMode.setEnabled(isRecording);
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
                case R.id.btn_takeoff_landing:
                    DialogUtil.showDialogExceptActionBar(new AlertDialog.Builder(MobileActivity.this, R.style.set_dialog)
                            .setTitle((isFlying) ? "Confirm Start Landing" : "Confirm Takeoff")
                            .setMessage((isFlying) ? "確定要降落嗎" : "確定要起飛嗎")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    flightController = DJIApplication.getFlightControllerInstance();
                                    if (null != flightController)
                                        startTakeoffLanding();
                                    else {
                                        isFlying = !isFlying;
                                        refreshTakeoffLandingIcon();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create());
                    break;
                case R.id.btn_rth:
                    flightController = DJIApplication.getFlightControllerInstance();
                    if (isRTH) {
                        isRTH = false;
                        mBtnRTH.setImageResource(R.drawable.icon_rth);
                        if (flightController != null)
                            flightController.cancelGoHome(djiError -> {
                                ToastUtil.showErrorToast("cancel go home", djiError);
                            });
                        DialogUtil.showDialog(MobileActivity.this, "Cancel RTH success");
                    } else {
                        DialogUtil.showDialogExceptActionBar(new AlertDialog.Builder(MobileActivity.this, R.style.set_dialog)
                                .setTitle("Confirm Return To Home")
                                .setMessage("確認要執行RTH Mission嗎")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (null != flightController)
                                            startRTH();
                                        else {
                                            isRTH = true;
                                            mBtnRTH.setImageResource(R.drawable.icon_cancel);
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .create());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void initFlightControllerCallback() {
        flightController = DJIApplication.getFlightControllerInstance();
        if (null == flightController) {
            flightStateCallback = null;
            return;
        }

        flightStateCallback = flightControllerState -> {
            //test flightMode
            Log.d(DJIApplication.TAG, "isLandingConfirmationNeeded : " + flightControllerState.isLandingConfirmationNeeded());
            Log.d(DJIApplication.TAG, "flightModeToString : " + flightControllerState.getFlightMode().toString());
            if (isFlying != flightController.getState().isFlying()) {
                isFlying = !isFlying;
                refreshTakeoffLandingIcon();
            }

            mHandler.post(() -> {
//                stringBuffer = new StringBuffer().append(flightControllerState.getGPSSignalLevel())
//                        .append(flightControllerState.getAircraftLocation().getLatitude() + " ")
//                        .append(flightControllerState.getAircraftLocation().getLongitude() + " ")
//                        .append(GoogleMapUtil.checkGpsCoordinates(flightControllerState.getAircraftLocation().getLatitude(),
//                                flightControllerState.getAircraftLocation().getLongitude()) + " ");
//                mTvTest.setText(stringBuffer);
                setSensor(flightControllerState.getFlightMode().toString());
                setGPSLevelIcon(flightControllerState);
            });

//                ToastUtil.showToast(flightControllerState.getFlightMode().toString());
            gMapUtil.initFlightController(flightControllerState);
            if (flightControllerState.isLandingConfirmationNeeded()) {
                if (confirmLandingDialog == null) {
                    confirmLandingDialog = new AlertDialog.Builder(MobileActivity.this, R.style.set_dialog)
                            .setTitle("Confirm landing")
                            .setMessage("確定要降落嗎")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    flightController.confirmLanding(djiError -> {
                                        confirmLandingDialog = null;
                                        ToastUtil.showErrorToast("降落成功", djiError);
                                    });
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    flightController.cancelLanding(djiError -> {
                                        confirmLandingDialog = null;
                                        ToastUtil.showErrorToast("取消降落成功", djiError);
                                    });
                                }
                            })
                            .create();
                    DialogUtil.showDialogExceptActionBar(confirmLandingDialog);
                }

            }
        };

    }

    public void triggerOnMapClick() {
        mapView.performClick();
    }

    public void changeFragment(int position) {
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
                    .add(droneView.getId(), mVideoSurfaceFragment)
                    .commit();
        }
    }

    private void captureAction() {
        camera = DJIApplication.getCameraInstance();
        if (null == camera)
            return;
        camera.setShootPhotoMode(CameraFragment.getPhotoMode(), djiError -> {
            if (null == djiError) {
                mHandler.postDelayed(() -> camera.startShootPhoto(djiError1 ->
                        ToastUtil.showErrorToast("take photo: success", djiError1)), 300);
            }
        });
    }

    // Method for starting recording
    private void startRecord() {
        camera = DJIApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(djiError -> {
                if (djiError == null) {
                    mHandler.postDelayed(() -> {
                        mBtnCamera.setImageResource(R.drawable.icon_recording);
                        ToastUtil.showToast("Record video: success");
                    }, 300);
                } else {
                    ToastUtil.showToast(djiError.getDescription());
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
            camera.stopRecordVideo(djiError -> {
                if (djiError == null) {
                    mHandler.postDelayed(() -> {
                        mBtnCamera.setImageResource(R.drawable.icon_record_video);
                        ToastUtil.showToast("Stop recording: success");
                    }, 300);
                } else {
                    ToastUtil.showToast(djiError.getDescription());
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
        initFlightControllerCallback();
        initUI();
    }

    private void refreshSDKRelativeUI() {
        mProduct = DJIApplication.getProductInstance();
        flightController = DJIApplication.getFlightControllerInstance();
        //virtual stick
        mVirtualStick.flightControllerChange(flightController);
        if (null != mProduct) {
            if (mProduct.isConnected()) {
                Log.d(TAG, "connect to icon_aircraft");
                mTvState.setText(R.string.connected);
                //rtmp server
                SettingFragment.setLiveStreamUrl(getResources().getString(R.string.RTMP_url));
                //flight controller state callback
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
                    mHandler.post(() -> {
                        mTvBatteryPower.setText(batteryState.getChargeRemainingInPercent() + "%");
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

    private void setSensor(String flightMode) {
        if (flightMode.equals(flightModes[0])) {//s禁用避障
            mImgSensor.setImageResource(R.drawable.sensor_none);
        } else if (flightMode.equals(flightModes[1])) {//p無法左右
            mImgSensor.setImageResource(R.drawable.sensor_frontandrear);
        } else if (flightMode.equals(flightModes[2])) {
            mImgSensor.setImageResource(R.drawable.sensor_surround);//t都可
        } else {//never use
            mImgSensor.setImageResource(R.drawable.sensor_lateral);
        }
    }

    private void startTakeoffLanding() {
        if (isFlying) {
            flightController.startLanding(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    ToastUtil.showErrorToast("start landing", djiError);
                }
            });
        } else {
            flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
//                    DialogUtil.showDialogBasedOnError(MobileActivity.this,djiError);
                    ToastUtil.showErrorToast("takeoff success", djiError);
                }
            });
        }

    }

    private void startRTH() {
        flightController.startGoHome(djiError -> {
            mHandler.post(() -> {
                if (null == djiError) {
                    isRTH = true;
                    mBtnRTH.setImageResource(R.drawable.icon_cancel);
                }
                ToastUtil.showErrorToast("start going home", djiError);
            });
        });
    }

    private void refreshTakeoffLandingIcon() {
        mHandler.post(() -> {
            if (isFlying)
                mBtnTakeoffLanding.setImageResource(R.drawable.icon_landing);
            else {
                mBtnTakeoffLanding.setImageResource(R.drawable.icon_takeoff);
                mBtnRTH.setImageResource(R.drawable.icon_rth);
            }
        });

    }


    //Gesture Listener
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private final int FLING_MIN_DISTANCE_HOR, FLING_MIN_VELOCITY;
        private final int FLING_MAX_DISTANCE_VER;

        public GestureListener(int frameWidth, int maxDistanceVer, int velocity) {
            FLING_MIN_DISTANCE_HOR = frameWidth / 3;//長度1/3
            FLING_MIN_VELOCITY = velocity;
            FLING_MAX_DISTANCE_VER = maxDistanceVer;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
////            test
//            runOnUiThread(() -> {
//                ToastUtil.showToast(FLING_MIN_DISTANCE_HOR + " " + FLING_MAX_DISTANCE_VER);
//            });
            if (fragmentPosition == 0) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }
            if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE_HOR
                    && Math.abs(e1.getY() - e2.getY()) < FLING_MAX_DISTANCE_VER
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
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private RectF getActiveTrackRect(View iv) {
        View parent = (View) iv.getParent();
        return new RectF(
                ((float) iv.getLeft() + iv.getX()) / (float) parent.getWidth(),
                ((float) iv.getTop() + iv.getY()) / (float) parent.getHeight(),
                ((float) iv.getRight() + iv.getX()) / (float) parent.getWidth(),
                ((float) iv.getBottom() + iv.getY()) / (float) parent.getHeight()
        );
    }

    private void drawActiveTrackRect(View view, Rect rect) {
        view.setX(rect.left);
        view.setY(rect.top);
        view.getLayoutParams().width = rect.right - rect.left;
        view.getLayoutParams().height = rect.bottom - rect.top;
        view.requestLayout();
        view.setVisibility(View.VISIBLE);
    }

    private void updateActiveTrackRect(ActiveTrackTrackingState trackingState) {
        if (null == trackingState)
            return;
        RectF trackingRectF = trackingState.getTargetRect();
        if (trackingRectF == null) {
            mHandler.post(() -> mImgTargetRect.setVisibility(View.INVISIBLE));
            return;
        }

        //TODO 有什差????
//        final int l = (int)((trackingRect.centerX() - trackingRect.width() / 2) * parent.getWidth());
//        final int t = (int)((trackingRect.centerY() - trackingRect.height() / 2) * parent.getHeight());
//        final int r = (int)((trackingRect.centerX() + trackingRect.width() / 2) * parent.getWidth());
//        final int b = (int)((trackingRect.centerY() + trackingRect.height() / 2) * parent.getHeight());
        Rect trackingRect = new Rect((int) (trackingRectF.left * droneView.getWidth())
                , (int) (trackingRectF.top * droneView.getHeight())
                , (int) (trackingRectF.right * droneView.getWidth())
                , (int) (trackingRectF.bottom * droneView.getHeight()));
        mHandler.post(() -> {
            ActiveTrackTargetState targetState = trackingState.getState();
            //TODO
            if ((targetState == ActiveTrackTargetState.CANNOT_CONFIRM)
                    || (targetState == ActiveTrackTargetState.UNKNOWN)) {
                mImgTargetRect.setImageResource(R.drawable.rect_active_track_cannotconfirm);
                mHandler.post(() -> mTvTest.setText(trackingState.getReason().toString()));
            } else if (targetState == ActiveTrackTargetState.WAITING_FOR_CONFIRMATION) {
                if (confirmActiveTrackDialog == null) {
                    confirmActiveTrackDialog = new AlertDialog.Builder(MobileActivity.this, R.style.set_dialog)
                            .setTitle("Confirm ActiveTrack")
                            .setMessage("Confirming Active Track?")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mActiveTrackOperator.acceptConfirmation(djiError -> {
                                        confirmActiveTrackDialog = null;
                                        ToastUtil.showToast("Accept Active Track\nStart Active Tracking");
                                    });
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mActiveTrackOperator.rejectConfirmation(djiError -> {
                                        confirmActiveTrackDialog = null;
                                        ToastUtil.showErrorToast("Reject Active Track Success", djiError);
                                    });
                                }
                            })
                            .create();
                    DialogUtil.showDialogExceptActionBar(confirmActiveTrackDialog);
                }
                mImgTargetRect.setImageResource(R.drawable.rect_active_track_needconfirm);
            } else if (targetState == ActiveTrackTargetState.TRACKING_WITH_LOW_CONFIDENCE) {
                mImgTargetRect.setImageResource(R.drawable.rect_active_track_lowconfidence);
            } else if (targetState == ActiveTrackTargetState.TRACKING_WITH_HIGH_CONFIDENCE) {
                mImgTargetRect.setImageResource(R.drawable.rect_active_track_highconfidence);
            }
            drawActiveTrackRect(mImgTargetRect, trackingRect);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void activeTrackEnable(boolean enable) {
        if (enable) {
            mImgSelectRect.bringToFront();
            mImgTargetRect.bringToFront();
            mActiveTrackOperator.addListener(activeTrackEvent -> {
//                mHandler.post(() -> mTvTest.setText(activeTrackEvent.getCurrentState().toString()));
                updateActiveTrackRect(activeTrackEvent.getTrackingState());
            });
            if (DJIApplication.isAircraftConnected())
                //auto sensing
                mActiveTrackOperator.enableAutoSensing(djiError -> ToastUtil.showErrorToast("start Auto Sensing success", djiError));
//            mActiveTrackOperator.setRecommendedConfiguration(djiError ->mHandler.post(()-> ToastUtil.showErrorToast("Set Recommended Config Success", djiError)));
            droneView.setOnTouchListener((view, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (OthersUtil.calcManhattanDistance(startX, startY, event.getX(), event.getY()) < 20
                                && !isDrawingRect)
                            return true;
                        isDrawingRect = true;
                        Rect rect = new Rect(Math.max((int) (startX < event.getX() ? startX : event.getX()), 0)
                                , Math.max((int) (startY < event.getY() ? startY : event.getY()), 0)
                                , Math.min((int) (startX >= event.getX() ? startX : event.getX()), view.getWidth())
                                , Math.min((int) (startY >= event.getY() ? startY : event.getY()), view.getHeight()));
                        if (rect.height() == 0 || rect.width() == 0)
                            return true;
                        mHandler.post(() -> drawActiveTrackRect(mImgSelectRect, rect));
                        break;
                    case MotionEvent.ACTION_UP:
                        RectF mActiveTrackRectF = getActiveTrackRect(mImgSelectRect);
//                        ToastUtil.showToast(mActiveTrackRectF.left + " " +
//                                mActiveTrackRectF.top + " " +
//                                mActiveTrackRectF.right + " " +
//                                mActiveTrackRectF.bottom + " ");
                        ActiveTrackMission mActiveTrackMission = new ActiveTrackMission(mActiveTrackRectF, ActiveTrackMode.TRACE);
                        if (isDrawingRect) {
                            mActiveTrackOperator.startTracking(mActiveTrackMission, djiError ->
                                    mHandler.post(() -> ToastUtil.showErrorToast("Start Active Track Success", djiError)));
                        }
                        isDrawingRect = false;
                        //如果不push進UI thread的queue而讓其他thread執行
                        //執行的thread會跟UI thread concurrent執行而造成順序相反
                        //rect 圖形不會消失
                        mHandler.post(() -> mImgSelectRect.setVisibility(View.INVISIBLE));
                        break;
                    default:
                        break;
                }
                return true;
            });
        } else {
            droneView.setOnTouchListener(null);
//            mActiveTrackOperator.disableAutoSensing(djiError -> ToastUtil.showErrorToast("Disable Auto Sensing Success", djiError));
            mActiveTrackOperator.stopTracking(djiError -> ToastUtil.showErrorToast("Stop Tracking Success", djiError));
            mActiveTrackOperator.removeAllListeners();
        }
    }

    private void setGPSLevelIcon(FlightControllerState flightControllerState) {
        String signalLevel = flightControllerState.getGPSSignalLevel().toString();
        int level = OthersUtil.parseInt(signalLevel.substring(signalLevel.length() - 1));
        mImgSignal.setImageResource(signalLevelDrawble[level]);
    }
}

