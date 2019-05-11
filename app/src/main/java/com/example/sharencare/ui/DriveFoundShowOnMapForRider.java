package com.example.sharencare.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Fragments.FirstRiderFragment;
import com.example.sharencare.Fragments.SectionsStatePagerAdapter;
import com.example.sharencare.Models.ClusterMarker;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.utils.MyClusterManagerRenderer;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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

import static com.example.sharencare.utils.StaticPoolClass.desAddress;
import static com.example.sharencare.utils.StaticPoolClass.otherUserDetails;

public class DriveFoundShowOnMapForRider extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener , GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "DriveFoundShowOnMapForR";
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
    private User userOtherUserDetails;
    private User currentUserDetails;
    private String otp = "";
    private TextView tripTo;
    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;
    SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
    private GeoApiContext mGeoApiContext;
    private String snippet;
    private Marker mSelectedMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides_found_show_on_map_for_rider);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.map_view_rider);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        initializeVars();
        mViewPager=findViewById(R.id.rider_fragment_container);
        initializeVars();
        if(StaticPoolClass.rideAcceptedFlag ==false)
            setUpViewPager();

    }

    private void setUpViewPager() {
        StaticPoolClass.rideAcceptedFlag = true;
        adapter.addFragment(new FirstRiderFragment(), "FirstRiderFragment");
        mViewPager.setAdapter(adapter);
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
                    mSelectedMarker.setSnippet("Distance:"+result.routes[0].legs[0].distance.toString());
                    mSelectedMarker.setTitle("Duration:"+result.routes[0].legs[0].duration.toString());
                    mSelectedMarker.showInfoWindow();
                    break;
                }
            }
        });
    }
    private  void setCameraViewToTrip(){
        //total view of the map
        try {
            Log.d(TAG, "setCameraView: Setting Camera view to:"+myLocation.getGeoPoint().toString());
            double bottomBoundary = myLocation.getGeoPoint().getLatitude() -0.01;
            double leftBoundary = myLocation.getGeoPoint().getLongitude() -0.01 ;
            double topBoundary = StaticPoolClass.desAddress.getLatitude() +0.01;
            double rightBoundary =StaticPoolClass.desAddress.getLongitude() +0.01 ;
            mLatLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds,100));

                }
            });
        }catch (Exception e){
            Log.d(TAG, "setCameraView: "+e.getMessage());
        }
    }
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

                try{
                        snippet="This is you";
                        avatar=R.drawable.person;
                        ClusterMarker clusterMarkerCurrentUser=new ClusterMarker(new LatLng(myLocation.getGeoPoint().getLatitude(),myLocation.getGeoPoint().getLongitude()),"My Location",snippet,avatar);
                        mClusterManager.addItem(clusterMarkerCurrentUser);
                        mClusterMarkers.add(clusterMarkerCurrentUser);
                        snippet="Car Owner";
                        avatar=R.drawable.car;
                        ClusterMarker clusterMarkerOtherUser=new ClusterMarker(new LatLng(otherUserLocation.getGeoPoint().getLatitude(),otherUserLocation.getGeoPoint().getLongitude()),otherUserDetails.getUsername()+"'s"+" "+"location",snippet,avatar);
                        mClusterManager.addItem(clusterMarkerOtherUser);
                        mClusterMarkers.add(clusterMarkerOtherUser);
                        avatar=R.drawable.destination;
                        snippet=StaticPoolClass.tripDetails.getTrip_destination();
                        ClusterMarker clusterMarkerOtherDestination=new ClusterMarker(new LatLng(StaticPoolClass.tripDestinationLatLng.lat,StaticPoolClass.tripDestinationLatLng.lng),"Destination",snippet,avatar);
                        mClusterManager.addItem(clusterMarkerOtherDestination);

                }catch (Exception e){
                    Log.d(TAG, "addMapMarkers: Error"+e.getMessage());

                }
            }
            mClusterManager.cluster();


    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        setCameraViewToTrip();
        if(marker.getSnippet().equals("This is you")){
            marker.hideInfoWindow();
        }
        else{
            Log.d(TAG, "onInfoWindowClick: marker Snippet "+marker.getSnippet());
            if(marker.getSnippet().equals(StaticPoolClass.tripDetails.getTrip_destination())){
                final AlertDialog.Builder builder = new AlertDialog.Builder(DriveFoundShowOnMapForRider.this);
                builder.setMessage("Calculate distance to "+StaticPoolClass.tripDetails.getTrip_destination())
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


    private void initializeVars() {
        myLocation =StaticPoolClass.currentUserLocation;
        otherUserLocation = StaticPoolClass.otherUserLocation;
        tripDetails = StaticPoolClass.tripDetails;
        userOtherUserDetails=StaticPoolClass.otherUserDetails;
        currentUserDetails=StaticPoolClass.currentUserDetails;
        Log.d(TAG, "initializeVars: MyLocation:" + myLocation.toString());
        Log.d(TAG, "initializeVars: OtherUserLocation:" + otherUserLocation.toString());
        Log.d(TAG, "initializeVars: TripDetails:" + tripDetails.toString());
        Log.d(TAG, "initializeVars: Other User Details:"+userOtherUserDetails);
        Log.d(TAG, "initializeVars: current User Details:"+currentUserDetails);
        if(mGeoApiContext==null){
            mGeoApiContext=new GeoApiContext.Builder().apiKey(getString(R.string.api_key)).build();
        }
    }

    private void startUserLocationsRunnable() {
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

    private void stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveMyLocations() {
        // Log.d(TAG, "retrieveUserLocations: Called");
        DocumentReference mUserLocationReference = FirebaseFirestore.getInstance().collection(getString(R.string.collection_userlocation))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mUserLocationReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    userLocation = task.getResult().toObject(UserLocation.class);
                    // Log.d(TAG, "onComplete: "+userLocation.toString());
                    LatLng latLng = new LatLng(myLocation.getGeoPoint().getLatitude(), myLocation.getGeoPoint().getLongitude());
                    // mClusterMarkers.get(0).setPosition(latLng);
                    // mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(0));
                    //  Log.d(TAG, "onComplete: Updating User Location");
                }
            }
        });
    }

    private void retrieveOtherUserLocations() {
        // Log.d(TAG, "retrieveOtherUserLocations: Called");
        DocumentReference mUserLocationReference = FirebaseFirestore.getInstance().collection(getString(R.string.collection_userlocation))
                .document(otherUserLocation.getUser_id());
        mUserLocationReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    userLocation = task.getResult().toObject(UserLocation.class);
                    // Log.d(TAG, "onComplete: Other User Location "+userLocation.toString());
                    LatLng latLng = new LatLng(userLocation.getGeoPoint().getLatitude(), userLocation.getGeoPoint().getLongitude());
                    //  mClusterMarkers.get(0).setPosition(latLng);
                    //   mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(0));
                    //   Log.d(TAG, "onComplete: Updating User Location");
                }
            }
        });

    }

    private void setCameraView() {
        //total view of the map
        try {
            Log.d(TAG, "setCameraView: Setting Camera view to:" + myLocation.getGeoPoint().toString());
            double bottomBoundary = myLocation.getGeoPoint().getLatitude() -0.01;
            double leftBoundary = myLocation.getGeoPoint().getLongitude() -0.01 ;
            double topBoundary = myLocation.getGeoPoint().getLatitude() +0.01;
            double rightBoundary =myLocation.getGeoPoint().getLongitude() +0.01;
            mLatLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds, 50));
                    addMapMarkers();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "setCameraView: " + e.getMessage());
        }
    }



    @Override
    public void onClick(View v) {


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
