

package ntou.project.djidrone;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import dji.common.camera.ResolutionAndFrameRate;
import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import ntou.project.djidrone.fragment.BatteryFragment;
import ntou.project.djidrone.fragment.CameraFragment;
import ntou.project.djidrone.fragment.ControllerFragment;
import ntou.project.djidrone.fragment.GridItem;
import ntou.project.djidrone.fragment.MainFragment;
import ntou.project.djidrone.fragment.SensorFragment;
import ntou.project.djidrone.fragment.SettingFragment;
import ntou.project.djidrone.fragment.SignalFragment;
import ntou.project.djidrone.listener.GestureListener;

public class MobileActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout, constraintBottom;
    private ToggleButton btn_changeMode, relativeLeftToggle;
    private LinearLayout linearLeft, linearRight;
    private ImageView mapView;
    private ImageView stickLeft, stickRight;
    private TextView mTvState;
    private List<Fragment> fragments;
    protected TextureView mVideoSurface = null;
    //camera
    private static final String TAG = MobileActivity.class.getName();
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    protected DJICodecManager mCodecManager = null;
    protected TextureView.SurfaceTextureListener textureListener = null;
    private String resolutionRatio = "16:9";
    private GestureDetector gestureDetector;
    private FrameLayout mFrameSetting;
    private int fragmentPosition;

    //camera
    @Override
    protected void onResume() {
        super.onResume();
        initPreviewer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uninitPreviewer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);


        Toast.makeText(MobileActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
        initViewId();
        initLinstener();
        initUI();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void initUI() {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//隱藏狀態欄和標題欄
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//全螢幕顯示
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//隱藏手機虛擬按鍵HOME/BACK/LIST按鍵
        if (mVideoSurface != null)
            mVideoSurface.setSurfaceTextureListener(textureListener);
        if (null != DJIApplication.getProductInstance())
            mTvState.setText(R.string.connected);
        else
            mTvState.setText(R.string.disconnected);
        Camera camera = DJIApplication.getCameraInstance();
        if (null != camera) {
            ResolutionAndFrameRate[] resolutionAndFrameRates =
                    camera.getCapabilities().videoResolutionAndFrameRateRange();
            String width = resolutionAndFrameRates[0].getResolution().toString().split("[_]")[1].split("[x]")[0];
            String height = resolutionAndFrameRates[0].getResolution().toString().split("[_]")[1].split("[x]")[1];
            ConstraintSet layoutMainSet = new ConstraintSet();
            layoutMainSet.clone(mainLayout);
            layoutMainSet.setDimensionRatio(R.id.droneView, width + ":" + height);
            layoutMainSet.applyTo(mainLayout);
            showToast("" + resolutionAndFrameRates[0].getResolution());
            showToast(resolutionRatio);
        }
    }

    private void initViewId() {
        mainLayout = findViewById(R.id.mainLayout);
        mTvState = findViewById(R.id.tv_state);
        btn_changeMode = findViewById(R.id.btn_changeMode);
        linearLeft = findViewById(R.id.linearLeft);
        linearRight = findViewById(R.id.linearRight);
        constraintBottom = findViewById(R.id.constraintBottomBottom);
        relativeLeftToggle = findViewById(R.id.relativeLeftToggle);
        mapView = findViewById(R.id.mapView);
        stickLeft = findViewById(R.id.leftStick);
        stickRight = findViewById(R.id.rightStick);
        mVideoSurface = findViewById(R.id.droneView);
        mFrameSetting = findViewById(R.id.container);
        fragments = getFragments();
        fragmentPosition = 0;
        getSupportFragmentManager()//getFragmentManager
                .beginTransaction()//要求 FragmentManager 回傳一個 FragmentTransaction 物件，用以進行 Fragment 的切換。
                .add(mFrameSetting.getId(), fragments.get(0))
                .commit();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initLinstener() {
        Onclick onclick = new Onclick();
        Toggle toggle = new Toggle();
        btn_changeMode.setOnCheckedChangeListener(toggle);
        relativeLeftToggle.setOnCheckedChangeListener(toggle);
        stickRight.setOnClickListener(onclick);
        stickLeft.setOnClickListener(onclick);
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
        textureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.e(TAG, "onSurfaceTextureAvailable");
                if (mCodecManager == null) {
                    mCodecManager = new DJICodecManager(MobileActivity.this, surface, width, height);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mCodecManager.onSurfaceSizeChanged(width, height, 0);
                Log.e(TAG, "onSurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.e(TAG, "onSurfaceTextureDestroyed");
                if (mCodecManager != null) {
                    mCodecManager.cleanSurface();
                    mCodecManager = null;
                }

                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        };
        //滑動返回main fragment
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int FLING_MIN_DISTANCE = mFrameSetting.getWidth() / 2, FLING_MIN_VELOCITY = 100;
                if (fragmentPosition == 0){
                    Log.d(TAG,"on main fragment");
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
                if (e1.getX() - e2.getX() < -FLING_MIN_DISTANCE
                        && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    fragmentPosition = 0;
                    new Thread(() -> {
                        try {
                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }).start();
                    Log.d(TAG, "back to main");
                }
//                Log.d(TAG,""+mFrameSetting.getWidth()+"\t"+mFrameSetting.getHeight());
//                Log.d(TAG, "" + (e1.getX() - e2.getX()));
//                Log.d(TAG, "" + FLING_MIN_DISTANCE);
//                Log.d(TAG, "" + velocityX +"\t"+ velocityY);
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
                        layoutMainSet.connect(mVideoSurface.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                        layoutMainSet.connect(mVideoSurface.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                        layoutMainSet.connect(mapView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        layoutMainSet.clear(mapView.getId(), ConstraintSet.RIGHT);
                        layoutMainSet.applyTo(mainLayout);
                        constraintBottom.setVisibility(View.GONE);
                        linearRight.setVisibility(View.GONE);
                    } else {
                        layoutMainSet.connect(mVideoSurface.getId(), ConstraintSet.RIGHT, R.id.gdlnvtRight, ConstraintSet.LEFT);
                        layoutMainSet.connect(mVideoSurface.getId(), ConstraintSet.BOTTOM, R.id.gdlnhzBottom, ConstraintSet.TOP);
                        layoutMainSet.connect(mapView.getId(), ConstraintSet.RIGHT, R.id.gdlnvtMap, ConstraintSet.LEFT);
                        layoutMainSet.clear(mapView.getId(), ConstraintSet.LEFT);
                        layoutMainSet.applyTo(mainLayout);
                        linearRight.setVisibility(View.VISIBLE);
                        constraintBottom.setVisibility(View.VISIBLE);
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
            //Intent intent = null;
            switch (v.getId()) {
                case R.id.leftStick:
                    break;
                case R.id.rightStick:
                    break;
                default:
                    break;
            }
            //startActivity(intent);
        }
    }

    private void initPreviewer() {
        BaseProduct product = DJIApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(textureListener);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = DJIApplication.getCameraInstance();
        if (camera != null) {
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                Log.d(TAG, toastMsg);
            }
        });

    }

    public void changeFragment(int position) {
        fragmentPosition = position;
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(mFrameSetting.getId(), fragments.get(position))
                .commit();
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
}

