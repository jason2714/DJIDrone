package ntou.project.djidrone;


import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import com.secneo.sdk.Helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import ntou.project.djidrone.utils.ToastUtil;

public class DJIApplication extends Application{

    public static final String FLAG_CONNECTION_CHANGE = "fpv_tutorial_connection_change";
    public static final String TAG = DJIApplication.class.getName();
    private static BaseProduct mProduct;
    public Handler mHandler;
    private static Application instance = null;




    public void setContext(Application application) {
        instance = application;
    }
    /**
     * This function is used to get the instance of DJIBaseProduct.
     * If no product is connected, it returns null.
     */
    public static synchronized BaseProduct getProductInstance() {
        mProduct = DJISDKManager.getInstance().getProduct();
        return mProduct;
    }

    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() instanceof Aircraft){
            camera = ((Aircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof HandHeld) {
            camera = ((HandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }

    public static synchronized Battery getBatteryInstance() {

        if (getProductInstance() == null) return null;

        Battery battery = null;

        if (getProductInstance() instanceof Aircraft){
            battery = ((Aircraft) getProductInstance()).getBattery();
        } else if (getProductInstance() instanceof HandHeld) {
            battery = ((HandHeld) getProductInstance()).getBattery();
        }

        return battery;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    public static synchronized FlightController getFlightControllerInstance() {
        if (getProductInstance() == null) return null;

        FlightController flightController = null;
        if (getProductInstance().isConnected()) {
            if (getProductInstance() instanceof Aircraft) {
                flightController = ((Aircraft) getProductInstance()).getFlightController();
            }
        }

        return flightController;
    }

    public static Application getInstance() {
        return DJIApplication.instance;
    }

    //websocket
    @ServerEndpoint(value="/websocketTest/{userId}")
    public static class Test {
        private Logger logger = LoggerFactory.getLogger(Test.class);

        private static String userId;

        //连接时执行
        @OnOpen
        public void onOpen(@PathParam("userId") String userId, Session session) throws IOException{
            this.userId = userId;
            logger.debug("新连接：{}",userId);
        }

        //关闭时执行
        @OnClose
        public void onClose(){
            logger.debug("连接：{} 关闭",this.userId);
        }

        //收到消息时执行
        @OnMessage
        public void onMessage(String message, Session session) throws IOException {
            logger.debug("收到用户{}的消息{}",this.userId,message);
            session.getBasicRemote().sendText("收到 "+this.userId+" 的消息 "); //回复用户
        }

        //连接错误时执行
        @OnError
        public void onError(Session session, Throwable error){
            logger.debug("用户id为：{}的连接发送错误",this.userId);
            error.printStackTrace();
        }
    }
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mHandler = new Handler(Looper.getMainLooper());
//        /**
//         * When starting SDK services, an instance of interface DJISDKManager.DJISDKManagerCallback will be used to listen to
//         * the SDK Registration result and the product changing.
//         */
//        mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {
//
//            //Listens to the SDK registration result
//            @Override
//            public void onRegister(DJIError djiError) {
//                if(djiError == DJISDKError.REGISTRATION_SUCCESS) {
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Register Success", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    DJISDKManager.getInstance().startConnectionToProduct();
//
//                } else {
//
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Register sdk fails, check network is available", Toast.LENGTH_LONG).show();
//                        }
//                    });
//
//                }
//                Log.e("TAG", djiError.toString());
//            }
//
//            @Override
//            public void onProductDisconnect() {
//                Log.d("TAG", "onProductDisconnect");
//                notifyStatusChange();
//            }
//            @Override
//            public void onProductConnect(BaseProduct baseProduct) {
//                Log.d("TAG", String.format("onProductConnect newProduct:%s", baseProduct));
//                notifyStatusChange();
//
//            }
//            @Override
//            public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
//                                          BaseComponent newComponent) {
//                if (newComponent != null) {
//                    newComponent.setComponentListener(new BaseComponent.ComponentListener() {
//
//                        @Override
//                        public void onConnectivityChange(boolean isConnected) {
//                            Log.d("TAG", "onComponentConnectivityChanged: " + isConnected);
//                            notifyStatusChange();
//                        }
//                    });
//                }
//
//                Log.d("TAG",
//                        String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
//                                componentKey,
//                                oldComponent,
//                                newComponent));
//
//            }
//
//            @Override
//            public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {
//
//            }
//
//            @Override
//            public void onDatabaseDownloadProgress(long l, long l1) {
//
//            }
//
//        };
//        //Check the permissions before registering the application for android system 6.0 above.
//        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int permissionCheck2 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_PHONE_STATE);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (permissionCheck == 0 && permissionCheck2 == 0)) {
//            //This is used to start SDK services and initiate SDK.
//            DJISDKManager.getInstance().registerApp(getApplicationContext(), mDJISDKManagerCallback);
//            Toast.makeText(getApplicationContext(), "registering, pls wait...", Toast.LENGTH_LONG).show();
//
//        } else {
//            Toast.makeText(getApplicationContext(), "Please check if the permission is granted.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void notifyStatusChange() {
//        mHandler.removeCallbacks(updateRunnable);
//        mHandler.postDelayed(updateRunnable, 500);
//    }
//
//    private Runnable updateRunnable = new Runnable() {
//
//        @Override
//        public void run() {
//            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
//            getApplicationContext().sendBroadcast(intent);
//        }
//    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        Helper.install(this);
        setContext(this);
    }

}
