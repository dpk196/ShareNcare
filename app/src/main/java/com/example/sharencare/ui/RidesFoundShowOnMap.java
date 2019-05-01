package com.example.sharencare.ui;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.utils.NotifactionChannel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.sharencare.services.MyFirebaseMessagingService.tripFromMessagingService;
import static com.example.sharencare.services.MyFirebaseMessagingService.userLocationFromMessagingService;
import static com.example.sharencare.ui.HomeActivity.userLocationFromNotificationChannelApplication;

public class RidesFoundShowOnMap extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "RidesFoundShowOnMap";
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private UserLocation myLocation;
    private UserLocation otherUserLocation;
    private TripDetail tripDetails;

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

    }




    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
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
    private  void initializeVars() {
        myLocation = userLocationFromNotificationChannelApplication;
        otherUserLocation = userLocationFromMessagingService;
        tripDetails = tripFromMessagingService;
        Log.d(TAG, "initializeVars: MyLocation:" + myLocation.toString());
        Log.d(TAG, "initializeVars: OtherUserLocation:" + otherUserLocation.toString());
        Log.d(TAG, "initializeVars: TripDetails:" + tripDetails.toString());
    }
}
