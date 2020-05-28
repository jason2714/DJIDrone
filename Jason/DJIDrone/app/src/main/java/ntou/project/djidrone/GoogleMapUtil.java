package ntou.project.djidrone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class GoogleMapUtil implements GoogleMap.OnMapClickListener, OnMapReadyCallback {

    private GoogleMap gMap;
    private double droneLocationLat = 181d, droneLocationLng = 181d;
    private Marker droneMarker = null;
    private FlightController mFlightController;
    private static Activity activity;
    private Location location = null;
    private LocationManager locationManager = null;
    private UiSettings gMapUiSettings;
    private boolean isSmallMap;


    public GoogleMapUtil(Activity activity, boolean isSmallMap) {
        GoogleMapUtil.activity = activity;
        this.isSmallMap = isSmallMap;
        MapsInitializer.initialize(activity);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(DJIApplication.TAG, "map Click");
        if (isSmallMap)
            ((MobileActivity) activity).triggerOnMapClick();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // TODO Auto-generated method stub
        // Initializing Amap object
        if (gMap == null) {
            gMap = googleMap;
            gMapUiSettings = gMap.getUiSettings();
            setUpMap();
        }
        gMap.setMyLocationEnabled(true);
        gMapUiSettings.setMyLocationButtonEnabled(true);//現在位置
        if (isSmallMap) {
        } else {
//            gMapUiSettings.setCompassEnabled(true);//預設true
            gMapUiSettings.setZoomControlsEnabled(true);
            gMapUiSettings.setMapToolbarEnabled(true);//沒顯示
        }
        locationManager = (LocationManager) (activity.getSystemService(Context.LOCATION_SERVICE));
        if (checkLocationEnable()) {
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            locationServiceInitial();
        } else {
            ToastUtil.showToast("請先開啟定位");
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //開啟設定頁面
        }
        LatLng NTOUCSE = new LatLng(25.150985, 121.779992);
        LatLng userLocation;
        if (null != location) {
            Log.d("MobileActivity", "get location success");
            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            gMap.addMarker(new MarkerOptions().position(userLocation).title("Marker User Location"));
        } else {
            Log.d("MobileActivity", "can't get location");
            userLocation = NTOUCSE;
            gMap.addMarker(new MarkerOptions().position(userLocation).title("Marker in NTOUCSE"));
        }
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
    }

    private void setUpMap() {
        gMap.setOnMapClickListener(this);// add the listener for click for amap object
    }

    public void unInitFlightController() {
        mFlightController = DJIApplication.getFlightControllerInstance();

        if (mFlightController != null) {
            mFlightController.setStateCallback(null);
        }
    }

    public void initFlightController() {

        mFlightController = DJIApplication.getFlightControllerInstance();

        if (mFlightController != null) {//TODO not a number
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
//                    ToastUtil.showToast("GPSSignalLevel" + djiFlightControllerCurrentState.getGPSSignalLevel());
                    Log.d(DJIApplication.TAG,"GPSSignalLevel" + djiFlightControllerCurrentState.getGPSSignalLevel());
//                    ToastUtil.showToast("AircraftLocation" + djiFlightControllerCurrentState.getAircraftLocation());
                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    Log.d(DJIApplication.TAG, "droneLocationLat" + djiFlightControllerCurrentState.getAircraftLocation().getLatitude());
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    Log.d(DJIApplication.TAG, "droneLocationLng" + djiFlightControllerCurrentState.getAircraftLocation().getLongitude());
                    updateDroneLocation();
                }
            });
        }
    }

    public static boolean checkGpsCoordinates(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    private void updateDroneLocation() {

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));
//        ToastUtil.showToast("updateDroneLocation");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }
                if (checkGpsCoordinates(droneLocationLat, droneLocationLng)) {
                    droneMarker = gMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void cameraUpdate() {
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = 15.0f;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        gMap.moveCamera(cu);
    }

    //location
    private boolean checkLocationEnable() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private void locationServiceInitial() {
//         做法一,由程式判斷用GPS_provider
//           if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
//               location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);  //使用GPS定位座標
//         }
//         else if ( locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
//         { location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //使用GPS定位座標
//         }
//         else {}
        // 做法二,由Criteria物件判斷提供最準確的資訊
        Criteria criteria = new Criteria();  //資訊提供者選取標準
        String bestProvider = locationManager.getBestProvider(criteria, true);    //選擇精準度最高的提供者
        Log.d("MobileActivity", bestProvider);
        location = locationManager.getLastKnownLocation(bestProvider);//取得上次定位位置
    }

}
