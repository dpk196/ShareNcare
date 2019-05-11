package com.example.sharencare.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Interfaces.FCMTokenInterface;
import com.example.sharencare.Interfaces.SearchForLaterOnTripsInterface;
import com.example.sharencare.Interfaces.SearchForOnTripRidesInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.threads.GetMatchedFCMToken;
import com.example.sharencare.threads.SearchForOnTripRides;
import com.example.sharencare.threads.SearchForRideLater;
import com.example.sharencare.utils.CalculateFare;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.MapMaker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

import static com.example.sharencare.ui.MapsActivity.geoPoint;

public class TripDetailsRider extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener,SearchForOnTripRidesInterface {
    private static final String TAG = "TripDetailsRider";
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private LatLngBounds mLatLngBounds;
    public static com.google.maps.model.LatLng startPoint ;
    public static com.google.maps.model.LatLng endPoint;
    Intent intent;
    public static String source;
    public static String destination;
    public static String duration;
    String distance;
    public static  String fare="";
    private TextView tripDistance,tripDuration,tripFare;
    private MarkerOptions markerSource;
    private MarkerOptions markerDestination;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ProgressBar mProgressBar;
    private ArrayList<String > matchedUserIds=new ArrayList<>();
    public static ArrayList<TripDetail> mCollectionTrips;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_rider);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView =  findViewById(R.id.trip_view_on_map_rider);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        intent = getIntent();
        findViewById(R.id.start_trip_now_button_tripDetails_rider).setOnClickListener(this);
        findViewById(R.id.start_trip_later_tripDetails_rider).setOnClickListener(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mProgressBar=findViewById(R.id.trip_details_rider_progressBar);
        gettingIntents();
        setTextView();
        getStartingEndingCoordinate(StaticPoolClass.directionsResultRider);

    }

    private void setTextView() {
        tripDistance=findViewById(R.id.trip_distance_tripDetialsRider);
        tripDuration=findViewById(R.id.trip_duration_tripDetialsRider);
        tripFare=findViewById(R.id.trip_fare_tripDetialsRider);
        tripDistance.setText(distance);
        tripDuration.setText(duration);
        tripFare.setText("Rs"+" "+fare);
    }

    private void gettingIntents() {
        destination = intent.getStringExtra("tripTo");
        source = intent.getStringExtra("tripFrom");
        distance = intent.getStringExtra("distance");
        duration=intent.getStringExtra("duration");
        fare=CalculateFare.calculateFare(intent.getStringExtra("distance"));
        Log.d(TAG, "gettingIntents: trip From" + source);
        Log.d(TAG, "gettingIntents: trip to" + destination);
        Log.d(TAG, "gettingIntents: distance"+distance);
        Log.d(TAG, "gettingIntents: duration"+duration);
        Log.d(TAG, "gettingIntents: Fare"+fare);
    }

    private void getStartingEndingCoordinate(final DirectionsResult result) {
        for (DirectionsRoute route : result.routes) {
            StaticPoolClass.tripDestinationLatLng = route.legs[0].endLocation;
            StaticPoolClass.tripSourceLatLng = route.legs[0].startLocation;
            startPoint= StaticPoolClass.tripDestinationLatLng;
            endPoint=StaticPoolClass.tripSourceLatLng;
            Log.d(TAG, "getStartingEndingCoordinate:Start:" + startPoint.toString());
            Log.d(TAG, "getStartingEndingCoordinate: End:" + endPoint.toString());
            Log.d(TAG, "getStartingEndingCoordinate: Start Adress and startLocation" + route.legs[0].startAddress+" location:"+route.legs[0].startLocation.toString());
            Log.d(TAG, "getStartingEndingCoordinate: End: Adress and startLocation " + route.legs[0].endAddress+" location:"+route.legs[0].endLocation.toString());
            double bottomBoundary = startPoint.lat -0.1;
            double leftBoundary = startPoint.lng -0.1 ;
            double topBoundary = endPoint.lat +0.1;
            double rightBoundary= endPoint.lng +0.1 ;
            mLatLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            LatLng latlng_src=new LatLng(startPoint.lat,startPoint.lng);
            LatLng latLng_dtn=new LatLng(endPoint.lat,endPoint.lng);
            markerSource= new MarkerOptions().position(latlng_src).title(destination);
            markerDestination=new MarkerOptions().position(latLng_dtn).title(source);
            break;
        }
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                    polyline.setClickable(true);
                    break;
                }
            }
        });
    }

    private  void setCameraView(){
        //total view of the map
        try {

            mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds,10));
                    Log.d(TAG, "onMapLoaded: setting up camera");

                }
            });
        }catch (Exception e){
            Log.d(TAG, "setCameraView: "+e.getMessage());
        }
    }
    private  void setMapMarker(){
        mGoogleMap.addMarker(markerDestination);
        mGoogleMap.addMarker(markerSource);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_trip_now_button_tripDetails_rider:{
                Log.d(TAG, "onClick: ");
                initThreadOnTrip();
                showDialog();
                break;
            }
            case R.id.start_trip_later_tripDetails_rider:{
                showDialog();
                startActivity(new Intent(TripDetailsRider.this,ScheduleTripRequiredDetails.class));
                break;
            }
        }

    }

    private  void initThreadOnTrip(){
        SearchForOnTripRides searchForOnTripRides =new SearchForOnTripRides(this,this,source,destination);
        searchForOnTripRides.execute();

    }


    @Override
    public void matchedOnTripRides(ArrayList<UserLocation> matchedRides,ArrayList<TripDetail> trips) {
        mCollectionTrips=trips;
        Log.d(TAG, "matchedOnTripRides: matched userRides size"+matchedRides.size());
        matchedUserIds.clear();
        if(matchedRides.size()>0){

            for (UserLocation matchedLocation:matchedRides){
                Log.d(TAG, "matchedOnTripRides: "+matchedLocation.toString());
                matchedUserIds.add(matchedLocation.getUser_id());
            }
            Intent intent =new Intent(TripDetailsRider.this,AvailableRides.class);
            intent.putExtra("matchedRides",matchedUserIds);
            hideDialog();
            startActivity(intent);
        }else {
            hideDialog();
            Log.d(TAG, "matchedOnTripRides: No Matched Rides Retrived");
            Toast.makeText(this, "No Rides Available to:"+destination, Toast.LENGTH_LONG).show();
        }
    }






    private void showDialog(){mProgressBar.setVisibility(View.VISIBLE);}
    private void hideDialog(){if(mProgressBar.getVisibility()==View.VISIBLE){mProgressBar.setVisibility(View.INVISIBLE);}}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setCameraView();
        setMapMarker();
        addPolylinesToMap(StaticPoolClass.directionsResultRider);
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


    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

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

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }



}
