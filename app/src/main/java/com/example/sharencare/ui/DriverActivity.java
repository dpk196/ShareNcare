package com.example.sharencare.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.sharencare.Adapters.RecyclerViewAdapter;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.Distance;
import com.google.maps.model.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;

public class DriverActivity extends AppCompatActivity {
    private static final String TAG = "DriverActivity";
    //vars
    private ArrayList<String> source = new ArrayList<>();
    private ArrayList<String> destination = new ArrayList<>();
    private FirebaseFirestore mDb;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeoApiContext mGeoApiContext;
    private  LatLng sourceLatlng;
    private  LatLng destinationLatlng;
    private  String sourceText;
    private String destinationText;
    private DirectionsResult result;
    TripDetail tripDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mGeoApiContext=new GeoApiContext.Builder().apiKey(getString(R.string.api_key)).build(); //Setting the apicontext for the directions Api
        placesPredictionFrom();
        placesPredictionTo();
       // getTripsFromFireStore();
        getLastKnownLocation();
    }

    private void calculateDirections() {
        DirectionsApiRequest directions;
        Log.d(TAG, "calculateDirections: calculating directions.");
        try {
            com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                    destinationLatlng.latitude,
                    destinationLatlng.longitude
            );
            directions = new DirectionsApiRequest(mGeoApiContext);


            directions.origin(
                    new com.google.maps.model.LatLng(
                            sourceLatlng.latitude,
                            sourceLatlng.longitude
                    )
            );
            directions.alternatives(true);
            Log.d(TAG, "calculateDirections: destination: " + destination.toString());
            directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {

                @Override
                public void onResult(DirectionsResult r) {
                    result=r;
                    Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                    Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                    Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                    tripDetails();
                }

                @Override
                public void onFailure(Throwable e) {
                    //  Toast.makeText(DriverActivity.this, "Please be more specific", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

                }
            });
        } catch (Exception e) {
            //Toast.makeText(this, "Please be more Specific", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "calculateDirections: Problem detected"+e.getMessage());
        }
    }

    private void tripDetails() {

        TripDetail tripDetail=new TripDetail();
        Bundle bundle=new Bundle();
        tripDetail.setTrip_source(sourceText);
        tripDetail.setTrip_destination(destinationText);
        Log.d(TAG, "tripDetails: "+sourceText);
        Log.d(TAG, "tripDetails: "+destinationText);
        tripDetail.setStatus("Yet to start");
        Intent intent  =new Intent(DriverActivity.this,TripDetails.class);
        LatLng sourceLatLng=new LatLng(sourceLatlng.latitude,sourceLatlng.longitude);
        LatLng destinationLatLng=new LatLng(destinationLatlng.latitude,destinationLatlng.longitude);
        try {
            intent.putExtra("destinationText",destinationText);
            intent.putExtra("sourceText",sourceText);
            intent.putExtra("DirectionsResults",result);
            bundle.putParcelable("SourceGeoPoint",sourceLatLng);
            bundle.putParcelable("DestinationGeoPoint",destinationLatLng);
            intent.putExtra("bundle", bundle);
            startActivity(intent);
        }catch (Exception e){
            Log.d(TAG, "tripDetails: "+e.getMessage());
        }
    }




    ////.......Places Prediction  from.................
    private void placesPredictionFrom() {
        Log.d(TAG, "placesPrediction: Called");
        if(!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }
        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_from);
        autocompleteFragment.isHidden();
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(22.5825856, 88.4452336),
                new LatLng(22.7580747,88.7024556));
        autocompleteFragment.setLocationBias(bounds);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Get info about the selected place.
                Address address=geoLocate(place.getName());
                if(address!=null) {
                    Log.i(TAG, "Place Source:" + place.getName());
                    sourceText=place.getName();
                    sourceLatlng = new LatLng(address.getLatitude(), address.getLongitude());
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    //.....Place Prediction To.......

    private void placesPredictionTo() {
        Log.d(TAG, "placesPrediction: Called");
        if(!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }
        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_to);

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(22.5825856, 88.4452336),
                new LatLng(22.7580747,88.7024556));
        autocompleteFragment.setLocationBias(bounds);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Get info about the selected place.
                Address address=geoLocate(place.getName());
                if(address!=null) {
                    Log.i(TAG, "Place Destination:" + place.getName());
                    destinationText=place.getName();
                    destinationLatlng = new LatLng(address.getLatitude(), address.getLongitude());
                    calculateDirections();
                }


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private Address geoLocate(String name) {
        Address address=null;
        Log.d(TAG, "geoLocate: Getting the latitude and Longitude of the Source and Destination for:"+name);
        Geocoder geocoder= new Geocoder(DriverActivity.this);
        List<Address> addressList= new ArrayList<>();
        try{
            Log.d(TAG, "geoLocate: inside try");
           addressList= geocoder.getFromLocationName(name,1);
        }catch (IOException e){
            Log.d(TAG, "geoLocate: "+e.getMessage());
        }
        if(addressList.size()>0){
            Log.d(TAG, "geoLocate: inside size>0");
             address =addressList.get(0);
            if(address.hasLatitude()&&address.hasLongitude()){

                     Log.d(TAG, "geoLocate: lat:"+address.getLatitude());
                     Log.d(TAG, "geoLocate: long:"+address.getLongitude());
            }
            else {
                Toast.makeText(this, "cant go that location", Toast.LENGTH_SHORT).show();
            }
        }
         return  address;
    }


    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: Getting user last Known Location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location=task.getResult();
                    GeoPoint mUserPosition=new GeoPoint(location.getLatitude(),location.getLongitude());
                    Log.d(TAG, "onComplete: Location Coordinates:"+mUserPosition.toString());
                }
            }
        });
    }
    //.....getting Trips From FireStore.............
    private void getTripsFromFireStore() {
    }

//    private String calculateFare(){
//       String distance=result.routes[0].legs[0].distance.toString();
//       String str="";
//       for(int i=0;i<distance.length()-3;i++){
//           str=str+distance.charAt(i);
//       }
//       Double d=Double.valueOf(str);
//       d*=7;
//       int i=d.intValue();
//       String fare=Integer.toString(i);
//       return  fare;
//    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(source, destination,this,tripDetail );
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


}

