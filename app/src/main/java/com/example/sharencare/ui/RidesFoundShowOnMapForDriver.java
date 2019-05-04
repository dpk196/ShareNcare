package com.example.sharencare.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.sharencare.Fragments.FirstDriverFragment;
import com.example.sharencare.Fragments.SectionsStatePagerAdapter;
import com.example.sharencare.Models.ClusterMarker;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.example.sharencare.utils.MyClusterManagerRenderer;
import com.example.sharencare.utils.StaticPoolClass;
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
import static com.example.sharencare.utils.StaticPoolClass.currentUserLocation;
import static com.example.sharencare.utils.StaticPoolClass.rideAcceptedFlag;


public class RidesFoundShowOnMapForDriver extends AppCompatActivity implements OnMapReadyCallback {
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
    private String otp="";
    private TextView tripTo;
    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;
    SectionsStatePagerAdapter adapter=new SectionsStatePagerAdapter(getSupportFragmentManager());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides_found_show_on_map_for_driver);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView =  findViewById(R.id.user_list_map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        mViewPager=findViewById(R.id.driver_fragment_container);
        initializeVars();
        if(rideAcceptedFlag==false)
                 setUpViewPager();

    }


   private void setUpViewPager(){
         rideAcceptedFlag=true;
        adapter.addFragment(new FirstDriverFragment(),"FirstDriverFragment");
        mViewPager.setAdapter(adapter);
   }
   public  void setViewPager(int fragmentNumber, Fragment fragment){
        adapter.addFragment(fragment,"SecondDriverFragment");
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(fragmentNumber);
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
        myLocation = currentUserLocation;
        otherUserLocation = StaticPoolClass.otherUserLocation;
        tripDetails = StaticPoolClass.tripDetails;
        userOtherUserDetails=StaticPoolClass.otherUserDetails;
        currentUserDetails=StaticPoolClass.currentUserDetails;
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
            double bottomBoundary = myLocation.getGeoPoint().getLatitude() -0.01;
            double leftBoundary = myLocation.getGeoPoint().getLongitude() -0.01 ;
            double topBoundary = myLocation.getGeoPoint().getLatitude() +0.01;
            double rightBoundary =myLocation.getGeoPoint().getLongitude() +0.01 ;
            mLatLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds,100));
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
