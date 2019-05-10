package com.example.sharencare.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Fragments.FirstDriverFragment;
import com.example.sharencare.Fragments.SectionsStatePagerAdapter;
import com.example.sharencare.Models.ClusterMarker;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.LocationService;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.example.sharencare.utils.MyClusterManagerRenderer;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

import static com.example.sharencare.utils.StaticPoolClass.currentUserLocation;
import static com.example.sharencare.utils.StaticPoolClass.otherUserDetails;
import static com.example.sharencare.utils.StaticPoolClass.rideAcceptedFlag;


public class RidesFoundShowOnMapForDriver extends AppCompatActivity implements OnMapReadyCallback , GoogleMap.OnInfoWindowClickListener {
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
    private static final int LOCATION_UPDATE_INTERVAL = 2500;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private User  userOtherUserDetails;
    private User currentUserDetails;
    private String otp="";
    private TextView tripTo;
    private ArrayList<UserLocation> mUserLocations=new ArrayList<>();
    private Marker markerCurrentUser;
    private Marker markerOtherUser;
    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;
    private GeoApiContext mGeoApiContext;
    SectionsStatePagerAdapter adapter=new SectionsStatePagerAdapter(getSupportFragmentManager());
    private String snippet="";
    private Marker mSelectedMarker;
    private String duration="";
    private String maxChargeableFare="";


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

        Log.d(TAG, "onMapReady: Called");
        mMap.setOnInfoWindowClickListener(this);
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
        mUserLocations.add(myLocation);
        otherUserLocation = StaticPoolClass.otherUserLocation;
        mUserLocations.add(otherUserLocation);
        tripDetails = StaticPoolClass.tripDetails;
        userOtherUserDetails=StaticPoolClass.otherUserDetails;
        currentUserDetails=StaticPoolClass.currentUserDetails;
        maxChargeableFare=StaticPoolClass.fare;
        Log.d(TAG, "initializeVars: Max Chargeable fare:"+maxChargeableFare);
        Log.d(TAG, "initializeVars: MyLocation:" + myLocation.toString());
        Log.d(TAG, "initializeVars: OtherUserLocation:" + otherUserLocation.toString());
        Log.d(TAG, "initializeVars: TripDetails:" + tripDetails.toString());
        Log.d(TAG, "initializeVars: Other User Details:"+userOtherUserDetails);
        Log.d(TAG, "initializeVars: current User Details:"+currentUserDetails);
        if(mGeoApiContext==null){
            mGeoApiContext=new GeoApiContext.Builder().apiKey(getString(R.string.api_key)).build();
        }


    }

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveLocations(currentUserLocation.getUser_id());
                retrieveLocations(otherUserLocation.getUser_id());

                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }
    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
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
                    addMapMarkers();
                }
            });
        }catch (Exception e){
            Log.d(TAG, "setCameraView: "+e.getMessage());
        }
    }

    private void retrieveLocations(String userId){

        DocumentReference mUserLocationReference= FirebaseFirestore.getInstance().collection(getString(R.string.collection_userlocation))
                .document(userId);
        mUserLocationReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                   UserLocation userLocation=task.getResult().toObject(UserLocation.class);
                   if(userLocation.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                      mClusterMarkers.get(0).setPosition(new LatLng(userLocation.getGeoPoint().getLatitude(),userLocation.getGeoPoint().getLongitude()));
                      mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(0));
                   }else{
                       mClusterMarkers.get(1).setPosition(new LatLng(userLocation.getGeoPoint().getLatitude(),userLocation.getGeoPoint().getLongitude()));
                       mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(1));
                   }


                }
            }
        });
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
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                RidesFoundShowOnMapForDriver.this.startForegroundService(serviceIntent);

            }else{
                Log.d(TAG, "startLocationService: Starting Services");
                startService(serviceIntent);
            }
        }
    }





    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable();
        startLocationService();
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
            int avatar;
            for(UserLocation userLocation:mUserLocations){
                Log.d(TAG, "addMapMarkers: adding marker to the location:"+userLocation.toString());
                try{

                    if(userLocation.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        snippet="This is you";
                        avatar=R.drawable.car;
                        ClusterMarker clusterMarker=new ClusterMarker(new LatLng(myLocation.getGeoPoint().getLatitude(),myLocation.getGeoPoint().getLongitude()),"My Location",snippet,avatar);
                        mClusterManager.addItem(clusterMarker);
                        mClusterMarkers.add(clusterMarker);

                    }
                    else{
                        snippet="Determine route to"+" "+otherUserDetails.getUsername()+"?";
                        avatar=R.drawable.person;
                    ClusterMarker clusterMarker=new ClusterMarker(new LatLng(otherUserLocation.getGeoPoint().getLatitude(),otherUserLocation.getGeoPoint().getLongitude()),otherUserDetails.getUsername()+"'s"+" "+"location",snippet,avatar);
                        mClusterManager.addItem(clusterMarker);
                        mClusterMarkers.add(clusterMarker);

                    }

                }catch (Exception e){
                    Log.d(TAG, "addMapMarkers: Error"+e.getMessage());

                }
            }
            mClusterManager.cluster();

        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.getTitle().contains("Duration")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(RidesFoundShowOnMapForDriver.this);
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(RidesFoundShowOnMapForDriver.this.getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                Toast.makeText(RidesFoundShowOnMapForDriver.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        if(marker.getSnippet().equals("This is you")){
            marker.hideInfoWindow();
        }
        else{
            if(!marker.getSnippet().equals(duration)){
                final AlertDialog.Builder builder = new AlertDialog.Builder(RidesFoundShowOnMapForDriver.this);
                builder.setMessage(marker.getSnippet())
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                calculateDirections(marker);
                                mSelectedMarker=marker;
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }

        }
    }

    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        myLocation.getGeoPoint().getLatitude(),
                        myLocation.getGeoPoint().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }
    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {

//                        Log.d(TAG, "run: latlng: " + latLng.toString());
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng));

                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                    polyline.setClickable(true);
                    duration="Distance:"+result.routes[0].legs[0].distance.toString();
                    mSelectedMarker.setSnippet("Distance:"+result.routes[0].legs[0].distance.toString());
                    mSelectedMarker.setTitle("Duration:"+result.routes[0].legs[0].duration.toString());
                    mSelectedMarker.showInfoWindow();
                    break;
                }
            }
        });
    }


}
