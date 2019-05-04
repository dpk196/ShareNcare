package com.example.sharencare.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.sharencare.Adapters.PreviousRidesRecyclerViewAdapter;
import com.example.sharencare.Interfaces.DirectionsResultInterface;
import com.example.sharencare.Interfaces.TripsRetrivedFromFireStoreInterFace;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.threads.DirectionsThreads;
import com.example.sharencare.threads.RetriveDetailsFromFireStore;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.Arrays;

public class RiderActivity extends AppCompatActivity implements TripsRetrivedFromFireStoreInterFace,  DirectionsResultInterface {
    private static final String TAG = "RiderActivity";
    String destinationText;
    String sourceText;
    LatLng sourceLatlng;
    LatLng destinationLatlng;
    FirebaseFirestore mDb;
    ArrayList<String> source =new ArrayList<>();
    ArrayList<String>  destination =new ArrayList<>();
    ArrayList<TripDetail> tripDetail =new ArrayList<>();
    private ProgressBar mProgressBar,dProgressBar;
    private  String  tripFrom;
    private  String  tripTo;
    boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        mDb = FirebaseFirestore.getInstance();
        mProgressBar = findViewById(R.id.rider_progressBar);
        dProgressBar = findViewById(R.id.torideActivity_progressBar);
        placesPredictionFrom();
        placesPredictionTo();
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
                getSupportFragmentManager().findFragmentById(R.id.rider_autocomplete_fragment_from);
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
                tripFrom=place.getName();
                Log.d(TAG, "onPlaceSelected: "+tripFrom);
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
                getSupportFragmentManager().findFragmentById(R.id.rider_autocomplete_fragment_to);

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(22.5825856, 88.4452336),
                new LatLng(22.7580747,88.7024556));
        autocompleteFragment.setLocationBias(bounds);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Get info about the selected place.
                tripTo=place.getName();
                Log.d(TAG, "onPlaceSelected: "+tripTo);
                 initThread();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }






    private void initThread(){
        DirectionsThreads directionsThreads=new DirectionsThreads(tripFrom,tripTo,this,this);
        directionsThreads.execute();

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
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView = findViewById(R.id.rider_recyclerview);
        PreviousRidesRecyclerViewAdapter adapter = new PreviousRidesRecyclerViewAdapter(source, destination,this,tripDetail );
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        RetriveDetailsFromFireStore retriveDetailsFromFireStore=new RetriveDetailsFromFireStore(this);
        retriveDetailsFromFireStore.execute();
    }




    @Override
    public void onDirectionsRetrived(DirectionsResult result) {
        try {
            Log.d(TAG, "onDirectionsRetrived: routes: " + result.routes[0].toString());
            Log.d(TAG, "onDirectionsRetrived: duration: " + result.routes[0].legs[0].duration);
            Log.d(TAG, "onDirectionsRetrived: distance: " + result.routes[0].legs[0].distance);
            Intent  intent=new Intent(RiderActivity.this,TripDetailsRider.class);
            StaticPoolClass.directionsResultRider=result;
            intent.putExtra("tripFrom",tripFrom);
            intent.putExtra("tripTo",tripTo);
            intent.putExtra("distance",result.routes[0].legs[0].distance.toString());
            intent.putExtra("duration",result.routes[0].legs[0].duration.toString());
            startActivity(intent);
        } catch (Exception e) {
            Log.d(TAG, "onDirectionsRetrived: " + e.getMessage());
        }
    }
    private void showDialog(){mProgressBar.setVisibility(View.VISIBLE);}
    private void hideDialog(){if(mProgressBar.getVisibility()==View.VISIBLE){mProgressBar.setVisibility(View.INVISIBLE);}}
    private void showToActivityDialog(){dProgressBar.setVisibility(View.VISIBLE);}
    private void hideToActivityDialog(){if(dProgressBar.getVisibility()==View.VISIBLE){mProgressBar.setVisibility(View.INVISIBLE);}}
}
