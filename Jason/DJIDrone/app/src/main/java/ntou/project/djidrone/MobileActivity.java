

package ntou.project.djidrone;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import ntou.project.djidrone.fragment.MainFragment;

public class MobileActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout,constraintBottom;
    private ToggleButton btn_changeMode, relativeLeftToggle;
    private LinearLayout linearLeft,linearRight;
    private ImageView droneView,mapView;
    private ImageView stickLeft,stickRight;
    private MainFragment mainFragment;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//隱藏狀態欄和標題欄
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//全螢幕顯示
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//隱藏手機虛擬按鍵HOME/BACK/LIST按鍵

        Toast.makeText(MobileActivity.this, "登入成功", Toast.LENGTH_SHORT).show();

        initViewId();
        initLinstener();
    }

    private void initViewId() {
        mainLayout = findViewById(R.id.mainLayout);
        btn_changeMode = findViewById(R.id.btn_changeMode);
        linearLeft = findViewById(R.id.linearLeft);
        linearRight = findViewById(R.id.linearRight);
        constraintBottom = findViewById(R.id.constraintBottomBottom);
        relativeLeftToggle = findViewById(R.id.relativeLeftToggle);
        droneView = findViewById(R.id.droneView);
        mapView = findViewById(R.id.mapView);
        stickLeft = findViewById(R.id.leftStick);
        stickRight = findViewById(R.id.rightStick);
    }

    private void initLinstener() {
        Onclick onclick = new Onclick();
        Toggle toggle = new Toggle();
        btn_changeMode.setOnCheckedChangeListener(toggle);
        relativeLeftToggle.setOnCheckedChangeListener(toggle);
        stickRight.setOnClickListener(onclick);
        stickLeft.setOnClickListener(onclick);
        mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container,mainFragment).commitAllowingStateLoss();
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
                    TransitionManager.beginDelayedTransition(mainLayout);
                    if (isChecked) {
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                        layoutMainSet.connect(mapView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        layoutMainSet.clear(mapView.getId(),ConstraintSet.RIGHT);
                        layoutMainSet.applyTo(mainLayout);
                        constraintBottom.setVisibility(View.GONE);
                        linearRight.setVisibility(View.GONE);
                    } else {
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.RIGHT, R.id.gdlnvtRight, ConstraintSet.LEFT);
                        layoutMainSet.connect(droneView.getId(), ConstraintSet.BOTTOM, R.id.gdlnhzBottom, ConstraintSet.TOP);
                        layoutMainSet.connect(mapView.getId(), ConstraintSet.RIGHT, R.id.gdlnvtMap, ConstraintSet.LEFT);
                        layoutMainSet.clear(mapView.getId(),ConstraintSet.LEFT);
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
}

