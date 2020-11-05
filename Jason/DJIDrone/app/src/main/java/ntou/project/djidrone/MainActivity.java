package ntou.project.djidrone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import ntou.project.djidrone.utils.DJIApplication;

public class MainActivity extends AppCompatActivity {

    private EditText account, password;
    private Button submit;
    private TextView mProductInformation, mProductState;
    //android integrate import
    private static final String TAG = MainActivity.class.getName();
    private static BaseProduct mProduct;
    private Handler mHandler;
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
            notifyStatusChange();
        }
    };
    private DJISDKManager.SDKManagerCallback myDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {
        @Override
        public void onRegister(DJIError djiError) {
            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                showToast("Register Success");
                //register success後啟動 開始等裝置連接,成功連接後跑onProductConnect
                DJISDKManager.getInstance().startConnectionToProduct();
            } else {
                showToast("Register sdk fails, please check the bundle id and network connection!");
            }
            Log.v(TAG, djiError.getDescription());
        }

        @Override
        public void onProductDisconnect() {
            mProduct = DJISDKManager.getInstance().getProduct();
            Log.d(TAG, "onProductDisconnect");
            showToast("Product Disconnected");
            notifyStatusChange();

        }

        @Override
        public void onProductConnect(BaseProduct baseProduct) {
            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
            showToast("Product Connected");
            notifyStatusChange();

        }


        @Override
        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                      BaseComponent newComponent) {

            if (newComponent != null) {
                newComponent.setComponentListener(mDJIComponentListener);
            }
            Log.d(TAG,
                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                            componentKey,
                            oldComponent,
                            newComponent));

        }

        @Override
        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

        }

        @Override
        public void onDatabaseDownloadProgress(long l, long l1) {
            showToast("progressRunning");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("UserData");
//        mDatabase.child("test").setValue(new User("123", "456"));
        initView();
        initLinstener();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //init receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        unregisterReceiver(mReceiver);
    }

//    @Override
//    protected void onDestroy() {
//        Log.e(TAG, "onDestroy");
//        unregisterReceiver(mReceiver);
//        super.onDestroy();
//    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }


    private void initView() {
        account = findViewById(R.id.userId);
        password = findViewById(R.id.password);
        submit = findViewById(R.id.submit);
        mProductInformation = findViewById(R.id.tv_product_information);
        mProductState = findViewById(R.id.tv_product_state);
    }

    private void initLinstener() {
        Onclick onclick = new Onclick();
        findViewById(R.id.btn_skip)
                .setOnClickListener(onclick);
        submit.setOnClickListener(onclick);
        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    checkInformation();
                return false;
            }
        });
    }

    private void checkInformation() {
        User user = new User(account.getText().toString(), password.getText().toString());
        DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(getResources().getString(R.string.database_url));
//        mDatabase.child("test").setValue(new User("123", "456"));
        mDatabase.child("UserData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean loginSuccess = false;
                for (DataSnapshot userData : snapshot.getChildren()) {
                    User dbUser = userData.getValue(User.class);
                    if (user.equals(dbUser)) {
                        loginSuccess = true;
                        break;
                    }
                }
                if (loginSuccess) {
                    showToast("登入成功");
                    Intent intent = new Intent(MainActivity.this, MobileActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(Define.LOG_TAG, "account : " + user.username +
                            "\npassword : " + user.password);
                    Toast.makeText(MainActivity.this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast(error.getMessage());
            }
        });
    }

    private class Onclick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit:
                    checkInformation();
                    break;
                case R.id.btn_skip:
                    startActivity(new Intent(MainActivity.this, MobileActivity.class));
                    break;
            }
        }
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), myDJISDKManagerCallback);
                }
            });
        }
    }

    //            無用
    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    //    public static class ConnectivityChangeEvent {
//    }
    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(DJIApplication.FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

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

    //receive notify change
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        mProduct = DJIApplication.getProductInstance();
        if (null != mProduct) {
            Log.d(TAG, "refreshSDK: True");
            if (mProduct.isConnected()) {
                Log.d(TAG, "connect to icon_aircraft");
                mProductState.setText(R.string.connection_success);
                mProductInformation.setText(mProduct.getModel().getDisplayName());
                submit.setEnabled(true);
            } else if (mProduct instanceof Aircraft) {
                Log.d(TAG, "only connect to remote controller");
                mProductState.setText(R.string.connection_only_rc);
                mProductInformation.setText(R.string.product_information);
                submit.setEnabled(false);
            }
        } else {
            Log.d(TAG, "refreshSDK: False");
            mProductState.setText(R.string.connection_loose);
            mProductInformation.setText(R.string.product_information);
            submit.setEnabled(false);
        }
    }

}