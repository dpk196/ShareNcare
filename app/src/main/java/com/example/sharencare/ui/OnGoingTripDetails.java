package com.example.sharencare.ui;

import android.location.Address;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

public class OnGoingTripDetails extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "OnGoingTripDetails";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mMapView;
    private GoogleMap mMap;
    private TripDetail trip=StaticPoolClass.tripDetails;
    private TextView tripDistance,tripDuration,tripFare;
    private MarkerOptions markerSource;
    private MarkerOptions markerDestination;
    public static  com.google.maps.model.LatLng startPoint;
    public static com.google.maps.model.LatLng endPoint;
    private LatLngBounds mLatLngBounds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_going_trip_details);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView =  findViewById(R.id.on_going_trip_details_map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        try{
            tripDistance=findViewById(R.id.trip_distance_tripDetialsDriver);
            tripDuration=findViewById(R.id.trip_duration_tripDetialsDriver);
            tripFare=findViewById(R.id.max_fare_value);
            tripDistance.setText(StaticPoolClass.tripDetails.getTrip_distance());
            tripDuration.setText(StaticPoolClass.tripDetails.getTrip_duration());
            tripFare.setText("Rs"+" "+ StaticPoolClass.fare);

        }catch(NullPointerException e){
            Log.d(TAG, "onCreate: "+e.getMessage());
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        getStartingEndingCoordinate(StaticPoolClass.directionsResultDriver);
        setMapMarker();
        setCameraView();
        addPolylinesToMap(StaticPoolClass.directionsResultDriver);
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
                    break;
                }
            }
        });
    }
    private void getStartingEndingCoordinate(final DirectionsResult result) {
        for (DirectionsRoute route : result.routes) {
            startPoint = route.legs[0].endLocation;
            endPoint = route.legs[0].startLocation;
            Log.d(TAG, "getStartingEndingCoordinate:Start:" + startPoint.toString());
            Log.d(TAG, "getStartingEndingCoordinate: End:" + endPoint.toString());
            Log.d(TAG, "getStartingEndingCoordinate: Start Adress and startLocation" + route.legs[0].startAddress + " location:" + route.legs[0].startLocation.toString());
            Log.d(TAG, "getStartingEndingCoordinate: End: Adress and startLocation " + route.legs[0].endAddress + " location:" + route.legs[0].endLocation.toString());
            double bottomBoundary = startPoint.lat - 0.1;
            double leftBoundary = startPoint.lng - 0.1;
            double topBoundary = endPoint.lat + 0.1;
            double rightBoundary = endPoint.lng + 0.1;
            mLatLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            LatLng latlng_src = new LatLng(startPoint.lat, startPoint.lng);
            LatLng latLng_dtn = new LatLng(endPoint.lat, endPoint.lng);
            markerSource = new MarkerOptions().position(latlng_src).title(StaticPoolClass.tripDetails.getTrip_destination());
            markerDestination = new MarkerOptions().position(latLng_dtn).title(StaticPoolClass.tripDetails.getTrip_source());
            break;
        }
    }
    private void setCameraView() {
        //total view of the map
        try {

            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds, 10));

                }
            });
        } catch (Exception e) {
            Log.d(TAG, "setCameraView: " + e.getMessage());
        }
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

    private void setMapMarker() {
        mMap.addMarker(markerDestination);
        mMap.addMarker(markerSource);

    }
}
