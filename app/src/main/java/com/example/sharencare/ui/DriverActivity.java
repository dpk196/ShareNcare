package com.example.sharencare.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.sharencare.Adapters.RecyclerViewAdapter;
import com.example.sharencare.Interfaces.DirectionsResultInterface;
import com.example.sharencare.Interfaces.TripsRetrivedFromFireStoreInterFace;
import com.example.sharencare.Interfaces.UserCurrentLocationInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.threads.DirectionsThreads;
import com.example.sharencare.threads.RetriveDetailsFromFireStore;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.Arrays;

public class DriverActivity extends AppCompatActivity implements DirectionsResultInterface, TripsRetrivedFromFireStoreInterFace {
    private static final String TAG = "DriverActivity";
    //vars
    private ProgressBar mProgressBar;
    private ArrayList<String> source = new ArrayList<>();
    private ArrayList<String> destination = new ArrayList<>();
    private FirebaseFirestore mDb;
    private GeoApiContext mGeoApiContext;
    private  LatLng sourceLatlng;
    private  LatLng destinationLatlng;
    private  String sourceText;
    private String destinationText;
    private DirectionsResult result;
    private String tripFrom,tripTo;
    private  static  String riderOrDriver="Driver";
    ArrayList<TripDetail> tripDetail=new ArrayList<>();
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        mDb = FirebaseFirestore.getInstance();
        mProgressBar = findViewById(R.id.driver_progressBar);
        placesPredictionFrom();
        placesPredictionTo();
        intent=new Intent(this, TripDetailsDriver.class);
        showDialog();
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
                intent.putExtra("source",place.getName());
                 tripFrom =place.getName()+" "+"Kolkata";
                Log.d(TAG, "onPlaceSelected: Trip From:"+tripFrom);
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
                intent.putExtra("destination",place.getName());
                tripTo=place.getName()+" "+"Kolkata";
                Log.d(TAG, "onPlaceSelected: Trip to:"+tripTo);
                initThread();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    public void initThread(){
        DirectionsThreads directionsThreads=new DirectionsThreads(tripFrom,tripTo,this,this);
        directionsThreads.execute();

    }


    //.....getting Trips From FireStore.............
    private void getTripsFromFireStore() {
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView = findViewById(R.id.driver_recyclerview);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(source, destination,this,tripDetail );
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
    private void tripDetails() {

        TripDetail tripDetail=new TripDetail();
        Bundle bundle=new Bundle();
        tripDetail.setTrip_source(sourceText);
        tripDetail.setRiderOrDriver(riderOrDriver);
        tripDetail.setTrip_destination(destinationText);
        Log.d(TAG, "tripDetails: "+sourceText);
        Log.d(TAG, "tripDetails: "+destinationText);
        tripDetail.setStatus("Yet to start");
        Intent intent  =new Intent(DriverActivity.this, TripDetailsDriver.class);
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


    @Override
    public void onDirectionsRetrived(DirectionsResult result) {
        try {
            String duration= result.routes[0].legs[0].duration.toString();
            String distance=result.routes[0].legs[0].distance.toString();
            Log.d(TAG, "onDirectionsRetrived: routes: " + result.routes[0].toString());
            Log.d(TAG, "onDirectionsRetrived: duration: " + result.routes[0].legs[0].duration);
            Log.d(TAG, "onDirectionsRetrived: distance: " + result.routes[0].legs[0].distance);
            intent.putExtra("distance",distance);
            intent.putExtra("duration",duration);
            TripDetailsDriver.tripDetail=null;
            startActivity(intent);
        } catch (Exception e) {
            Log.d(TAG, "onDirectionsRetrived: " + e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        RetriveDetailsFromFireStore retriveDetailsFromFireStore=new RetriveDetailsFromFireStore(this);
        retriveDetailsFromFireStore.execute();
//        UserCurrentLocation mUserCurrentLocation=new UserCurrentLocation(this,this);
//        mUserCurrentLocation.execute();
    }



    @Override
    public void userTripsCollectionFromFirestore(ArrayList<TripDetail> result) {
        Log.d(TAG, "userTripsCollectionFromFirestore: Called");
        for(TripDetail trip : result ){
            Log.d(TAG, "userTripsCollectionFromFirestore: "+trip.toString());
            source.add(trip.getTrip_source());
            destination.add(trip.getTrip_destination());
            try {
                tripDetail.add(trip);
            }catch (Exception e){
                Log.d(TAG, "userTripsCollectionFromFirestore: "+e.getMessage());
            }
        }
        initRecyclerView();
        hideDialog();
    }
    private void showDialog(){mProgressBar.setVisibility(View.VISIBLE);}
    private void hideDialog(){if(mProgressBar.getVisibility()==View.VISIBLE){mProgressBar.setVisibility(View.INVISIBLE);}}


}

