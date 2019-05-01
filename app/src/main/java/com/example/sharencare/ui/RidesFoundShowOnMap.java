package com.example.sharencare.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.sharencare.Interfaces.FCM;
import com.example.sharencare.Models.ClusterMarker;
import com.example.sharencare.Models.FCMData;
import com.example.sharencare.Models.FirebaseCloudMessage;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.utils.MyClusterManagerRenderer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sharencare.Adapters.SearchedRidesRecyclerViewAdapter.BASE_URL;
import static com.example.sharencare.services.MyFirebaseMessagingService.tripFromMessagingService;
import static com.example.sharencare.services.MyFirebaseMessagingService.userLocationFromMessagingService;
import static com.example.sharencare.services.MyFirebaseMessagingService.userRiderDetailsFromMessagingService;
import static com.example.sharencare.ui.HomeActivity.userLocationFromHomeScreen;


public class RidesFoundShowOnMap extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = "RidesFoundShowOnMap";
    private MapView mMapView;
    private GoogleMap mMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private UserLocation myLocation;
    private UserLocation otherUserLocation;
    private TripDetail tripDetails;
    private Handler mHandler = new Handler();
    private LatLngBounds mLatLngBounds;
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private UserLocation userLocation;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private User  userOtherUserDetails;
    private User currentUserDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides_driver_found_show_on_map);
        initializeVars();
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.user_list_map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        findViewById(R.id.accept_ride).setOnClickListener(this);
        findViewById(R.id.reject_ride).setOnClickListener(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        Log.d(TAG, "onMapReady: Called");
        setCameraView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }


    private  void initializeVars() {
        myLocation = userLocationFromHomeScreen;
        otherUserLocation = userLocationFromMessagingService;
        tripDetails = tripFromMessagingService;
        userOtherUserDetails=userRiderDetailsFromMessagingService;
        currentUserDetails=MainActivity.currentUser;
        Log.d(TAG, "initializeVars: MyLocation:" + myLocation.toString());
        Log.d(TAG, "initializeVars: OtherUserLocation:" + otherUserLocation.toString());
        Log.d(TAG, "initializeVars: TripDetails:" + tripDetails.toString());
        Log.d(TAG, "initializeVars: Other User Details:"+userOtherUserDetails);
        Log.d(TAG, "initializeVars: current User Details:"+currentUserDetails);
    }

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveMyLocations();
                retrieveOtherUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }
    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }
    private void retrieveMyLocations() {
       // Log.d(TAG, "retrieveUserLocations: Called");
        DocumentReference mUserLocationReference= FirebaseFirestore.getInstance().collection(getString(R.string.collection_userlocation))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mUserLocationReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    userLocation=task.getResult().toObject(UserLocation.class);
                   // Log.d(TAG, "onComplete: "+userLocation.toString());
                    LatLng latLng=new LatLng(myLocation.getGeoPoint().getLatitude(),myLocation.getGeoPoint().getLongitude());
                   // mClusterMarkers.get(0).setPosition(latLng);
                   // mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(0));
                  //  Log.d(TAG, "onComplete: Updating User Location");
                }
            }
        });
    }

    private void retrieveOtherUserLocations(){
       // Log.d(TAG, "retrieveOtherUserLocations: Called");
        DocumentReference mUserLocationReference= FirebaseFirestore.getInstance().collection(getString(R.string.collection_userlocation))
                .document(otherUserLocation.getUser_id());
        mUserLocationReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    userLocation=task.getResult().toObject(UserLocation.class);
                   // Log.d(TAG, "onComplete: Other User Location "+userLocation.toString());
                    LatLng latLng=new LatLng(userLocation.getGeoPoint().getLatitude(),userLocation.getGeoPoint().getLongitude());
                  //  mClusterMarkers.get(0).setPosition(latLng);
                 //   mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(0));
                 //   Log.d(TAG, "onComplete: Updating User Location");
                }
            }
        });

    }
    private  void setCameraView(){
        //total view of the map
        try {
            Log.d(TAG, "setCameraView: Setting Camera view to:"+myLocation.getGeoPoint().toString());
            double bottomBoundary = myLocation.getGeoPoint().getLatitude() -0.1;
            double leftBoundary = myLocation.getGeoPoint().getLongitude() -0.1 ;
            double topBoundary = myLocation.getGeoPoint().getLatitude() +0.1;
            double rightBoundary =myLocation.getGeoPoint().getLongitude() +0.1 ;
            mLatLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds,0));
                    setMarker();
                }
            });
        }catch (Exception e){
            Log.d(TAG, "setCameraView: "+e.getMessage());
        }
    }
    private void setMarker(){
        mMap.addMarker(new MarkerOptions().position(new LatLng(otherUserLocation.getGeoPoint().getLatitude(), otherUserLocation.getGeoPoint().getLongitude())).title("Marker"));
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.accept_ride :{
                sendRequest(userOtherUserDetails.getToken(),userOtherUserDetails.getUser_id());
                break;
            }
            case R.id.reject_trip:{
                break;
            }
        }

    }
    private  void sendRequest(String token,String userId){
        Log.d(TAG, "sendRequest: Sending request to user:"+userId);
        Log.d(TAG, "sendRequest: Sending request for the token:"+token);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FCM fcmAPI=retrofit.create(FCM.class);
        HashMap<String, String> headers=new HashMap<>();
        //attaching the headers
        headers.put("Content-Type","application/json");
        headers.put("Authorization","key="+"AAAApCdkCU8:APA91bFfJApMQKhvYQS-R0P7R9eVcUgd7R2a6iwOe37zPQ5aD2YJY2OMMc6Yx5Utg2HkT9CB9HhKCUWvZkbD4aXBlQN5AJwsfSVfVVc52q-7-mCWuuOh9d4OamKhjuE1PmaP8Lek80w_");

        //token
        FCMData data=new FCMData();
        data.setToUserId(otherUserLocation.getUser_id());
        data.setFromUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.setData_type("data_type_ride_accepted");
        data.setTitle(currentUserDetails.getUsername()+" "+"accepted your Request");
        FirebaseCloudMessage firebaseCloudMessage =new FirebaseCloudMessage();
        firebaseCloudMessage.setData(data);
        firebaseCloudMessage.setTo(token);
        Call<ResponseBody> call =fcmAPI.send(headers,firebaseCloudMessage);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: Server Response:"+response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: Unable to send message:"+t.getMessage());

            }
        });



    }


    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }



}
