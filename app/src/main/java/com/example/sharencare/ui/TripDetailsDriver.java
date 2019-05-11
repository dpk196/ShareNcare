package com.example.sharencare.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.utils.CalculateFare;
import com.example.sharencare.utils.DatePickerDialogFragment;
import com.example.sharencare.utils.StaticPoolClass;
import com.example.sharencare.utils.TimePickerDialogFragment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

import static com.example.sharencare.utils.CalculateFare.calculateFare;

public class TripDetailsDriver extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = "TripDetailsDriver";
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private LatLngBounds mLatLngBounds;
    Intent intent;
    String source;
    String destination;
    String duration;
    String distance;
    private static com.google.maps.model.LatLng startPoint ;
    private static com.google.maps.model.LatLng endPoint;
    static String fare = "Free Ride";
    private TextView tripDistance, tripDuration;
    private MarkerOptions markerSource;
    private MarkerOptions markerDestination;
    public static TripDetail tripDetail;
    private boolean onTrip = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_driver);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.trip_view_on_map_driver);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        intent = getIntent();
        findViewById(R.id.start_trip_now_button_tripDetails_driver).setOnClickListener(this);
        findViewById(R.id.start_trip_later_tripDetails_driver).setOnClickListener(this);
        gettingIntents();
        setTextView();
        getStartingEndingCoordinate(StaticPoolClass.directionsResultDriver);
    }

    private void setTextView() {
        tripDistance = findViewById(R.id.trip_distance_tripDetialsDriver);
        tripDuration = findViewById(R.id.trip_duration_tripDetialsDriver);
        tripDistance.setText(distance);
        tripDuration.setText(duration);

    }

    private void gettingIntents() {
        destination = intent.getStringExtra("tripTo");
        source = intent.getStringExtra("tripFrom");
        distance = intent.getStringExtra("distance");
        duration = intent.getStringExtra("duration");
        Log.d(TAG, "gettingIntents: trip From" + source);
        Log.d(TAG, "gettingIntents: trip to" + destination);
        Log.d(TAG, "gettingIntents: distance" + distance);
        Log.d(TAG, "gettingIntents: duration" + duration);
        Log.d(TAG, "gettingIntents: Fare" + fare);
    }

    private void getStartingEndingCoordinate(final DirectionsResult result) {
        for (DirectionsRoute route : result.routes) {
            StaticPoolClass.tripDestinationLatLng = route.legs[0].endLocation;
            StaticPoolClass.tripSourceLatLng = route.legs[0].startLocation;
            startPoint= StaticPoolClass.tripDestinationLatLng;
            endPoint=StaticPoolClass.tripSourceLatLng;
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
            markerSource = new MarkerOptions().position(latlng_src).title(destination);
            markerDestination = new MarkerOptions().position(latLng_dtn).title(source);
            break;
        }
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
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                    polyline.setClickable(true);
                    break;
                }
            }
        });
    }

    private void setCameraView() {
        //total view of the map
        try {

            mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds, 10));

                }
            });
        } catch (Exception e) {
            Log.d(TAG, "setCameraView: " + e.getMessage());
        }
    }

    private void setMapMarker() {
        mGoogleMap.addMarker(markerDestination);
        mGoogleMap.addMarker(markerSource);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setCameraView();
        setMapMarker();
        addPolylinesToMap(StaticPoolClass.directionsResultDriver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_trip_now_button_tripDetails_driver: {
                for (TripDetail tripDetail : DriverActivity.collectionTrips) {
                    if (tripDetail.getStatus().equals("On trip")) {
                        Log.d(TAG, "onClick: User is Already on a trip");
                        onTrip = true;
                        AlertDialog.Builder builder = new AlertDialog.Builder(TripDetailsDriver.this);
                        builder.setMessage("You are already on an Ongoing  Trip");
                        builder.setTitle("Can not have more than One Ongoing Trip");
                        builder.setCancelable(false);
                        builder
                                .setPositiveButton(
                                        "Ok",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                Intent intent = new Intent(TripDetailsDriver.this, HomeActivity.class);
                                                startActivity(intent);

                                            }
                                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    if (onTrip == true) {
                        break;
                    }
                }
                if (onTrip != true) {
                    createAtripDetailsObject();
                    submitDetailsToFireStore(tripDetail);
                    Log.d(TAG, "onClick: Building Trip Object");
                }

                break;
            }
            case R.id.start_trip_later_tripDetails_driver: {
                createAtripDetailsObject();
                Intent intent = new Intent(TripDetailsDriver.this, ScheduleTripDriver.class);
                createAtripDetailsObject();
                startActivity(intent);

            }
        }

    }

    private void createAtripDetailsObject() {
        tripDetail = new TripDetail("", "On trip", source, destination, FirebaseAuth.getInstance().getCurrentUser().getUid(), duration, fare, distance, "", "driver", null,"");
        Log.d(TAG, "createAtripDetailsObject: " + tripDetail);

    }

    private void submitDetailsToFireStore(TripDetail tripDetail) {
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newTripRef = mDb.collection(getString(R.string.collection_trips)).document();
        Log.d(TAG, "submitDetailsToFireStore: id of the document:"+newTripRef.getId());
        tripDetail.setTripId(newTripRef.getId());
        newTripRef.set(tripDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Details Submitted Successfully");
                    Log.d(TAG, "onComplete: " + task.toString());
                    Toast.makeText(TripDetailsDriver.this, "You will be notified when a rider is found", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(TripDetailsDriver.this);
                    builder.setMessage("You will be Notified when a rider is found");
                    builder.setTitle("Have a safe Journey");
                    builder.setCancelable(false);
                    builder
                            .setPositiveButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            Intent intent = new Intent(TripDetailsDriver.this, HomeActivity.class);
                                            startActivity(intent);

                                        }
                                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                } else {
                    Log.d(TAG, "onComplete: Something went wrong");
                }
            }
        });


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

