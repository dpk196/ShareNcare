package com.example.sharencare.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.sharencare.Models.ClusterMarker;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.LocationService;
import com.example.sharencare.utils.MyClusterManagerRenderer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private LatLngBounds mLatLngBounds;
    private FirebaseFirestore mDb;
    static LocationService.MyBinder binder;
    static LocationService mLocationService;
    static ServiceConnection serviceConnection;
    static GeoPoint geoPoint;
    boolean flag = false;
    private LatLngBounds mMapBoundary;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private UserLocation userLocation;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    Intent fromTripDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startLocationService();
        fromTripDetails=getIntent();
       // userLocation= TripDetailsDriver.userLocation;
        Log.d(TAG, "onCreate: "+userLocation.toString());

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        addMapMarkers();
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

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: Called");
        DocumentReference mUserLocationReference=FirebaseFirestore.getInstance().collection(getString(R.string.collection_userlocation))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
         mUserLocationReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
             @Override
             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                   if(task.isSuccessful()){
                        userLocation=task.getResult().toObject(UserLocation.class);
                       Log.d(TAG, "onComplete: "+userLocation.toString());
                       LatLng latLng=new LatLng(userLocation.getGeoPoint().getLatitude(),userLocation.getGeoPoint().getLongitude());
                       mClusterMarkers.get(0).setPosition(latLng);
                       mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(0));
                       Log.d(TAG, "onComplete: Updating User Location");
                   }
             }
         });
    }
    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }


    //..................
  private  void addMapMarkers(){
        if(mMap!=null){
            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(this, mMap, mClusterManager);
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            String snippet = "You";
            int avatar = R.drawable.car;
            try {
                ClusterMarker newClusterMarker = new ClusterMarker(new LatLng(userLocation.getGeoPoint().getLatitude(), userLocation.getGeoPoint().getLongitude()), "My Location", snippet, avatar);
                mClusterManager.addItem(newClusterMarker);
                mClusterMarkers.add(newClusterMarker);
                mClusterManager.cluster();
                setCameraView();
            }catch (Exception e){
                Log.d(TAG, "addMapMarkers: Error occured"+e.getMessage());
            }



        }
  }
  //............................

    private  void setCameraView(){
        //total view of the map
        try {
            Log.d(TAG, "setCameraView: Setting Camera view to:"+userLocation.getGeoPoint().toString());
            double bottomBoundary = userLocation.getGeoPoint().getLatitude() -0.1;
            double leftBoundary = userLocation.getGeoPoint().getLongitude() -0.1 ;
            double topBoundary = userLocation.getGeoPoint().getLatitude() +0.1;
            double rightBoundary =userLocation.getGeoPoint().getLongitude() +0.1 ;
            mLatLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds, 0));
        }catch (Exception e){
            Log.d(TAG, "setCameraView: "+e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUserLocationsRunnable();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();

    }
}
