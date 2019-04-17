package com.example.sharencare.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.sharencare.Interfaces.UserCurrentLocationInterface;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.LocationService;
import com.example.sharencare.threads.UserCurrentLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private LatLngBounds mLatLngBounds;
    private FirebaseFirestore mDb;
    static LocationService.MyBinder binder;
    static LocationService mLocationService;
    static ServiceConnection serviceConnection;
    static GeoPoint geoPoint;
    boolean flag = true;
    private LatLngBounds mMapBoundary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startLocationService();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MapsActivity.this.startForegroundService(serviceIntent);
            }else{
                Log.d(TAG, "startLocationService: Starting Services");
                startService(serviceIntent);



            }
        }
    }

    private boolean isLocationServiceRunning() {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.sharencare.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

//    private  void bindToLocationService(){
//        Log.d(TAG, "bindToLocationService: Binding to Location Service");
//        if(serviceConnection==null) {
//            serviceConnection = new ServiceConnection() {
//                @Override
//                public void onServiceConnected(ComponentName name, IBinder service) {
//                    Log.d(TAG, "onServiceConnected: Connected to Service");
//                    binder = (LocationService.MyBinder) service;
//                    mLocationService = binder.getService();
//
//                }
//
//                @Override
//                public void onServiceDisconnected(ComponentName name) {
//
//                }
//            };
//        }
//        Intent locationServiceIntent = new Intent(this, LocationService.class);
//        bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//
//
//    }


    private  void initThread(){
        new Thread(new Runnable() {
            private static final String TAG = " inside Thread";
            @Override
            public void run() {
                getUserLocationUpdates();
            }
        }).start();

    }

    private  void getUserLocationUpdates() {
        Log.d(TAG, "getUserLocationUpdates: called");
        while (flag==true) {
            try {
                Log.d(TAG, "getUserLocationUpdates: "+Thread.currentThread().getId());
                Thread.sleep(4000);
                geoPoint = LocationService.getUserLocation();
                Log.d(TAG, "getUserLocationUpdates: " + geoPoint.toString());

            } catch (Exception e) {
                Log.d(TAG, "getUserLocationUpdates: " + e.getMessage());
            }

        }
    }

    private  void setCameraView(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds,0));
    }

    @Override
    protected void onStop() {
        super.onStop();
        flag=false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        flag=true;
        initThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag=false;

    }
}
