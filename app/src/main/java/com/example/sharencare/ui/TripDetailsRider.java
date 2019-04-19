package com.example.sharencare.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sharencare.Interfaces.SearchForTripsInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.threads.SearchForRides;
import com.example.sharencare.utils.CalculateFare;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TripDetailsRider extends AppCompatActivity implements View.OnClickListener, SearchForTripsInterface {
    private static final String TAG = "TripDetailsRider";
    TextView tripStartTime, tripDistance, tripDuration, tripFare, tripFrom, tripTo,startTripNow,startTripLater;
    FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    Intent intent;
    public static TripDetail tripDetail;
    Button tripSubmitButton;
    String source;
    String destination;
    String duration;
    String distance;
    static  String fare="";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static GeoPoint geoPoint;
    private ArrayList<TripDetail> tripFromFireStore=new ArrayList();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_rider);
        tripDistance = findViewById(R.id.trip_distance);
        tripDuration = findViewById(R.id.trip_duration);
        tripFare = findViewById(R.id.trip_fare);
        tripFrom = findViewById(R.id.trip_from);
        tripTo = findViewById(R.id.trip_to);
        startTripNow = findViewById(R.id.start_trip_button);
        startTripNow.setOnClickListener(this::onClick);
        startTripLater = findViewById(R.id.trip_confirm);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mDb=FirebaseFirestore.getInstance();
        intent = getIntent();
        getLastKnownLocation();
        setTripDetailsView();
    }
    private void setTripDetailsView() {
        tripDuration.setText(intent.getStringExtra("duration"));
        tripFrom.setText(intent.getStringExtra("tripFrom"));
        tripTo.setText(intent.getStringExtra("tripTo"));
        tripDistance.setText(intent.getStringExtra("distance"));
        tripFare.setText(CalculateFare.calculateFare(intent.getStringExtra("distance")));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.start_trip_button:{
                initThread();
                break;
            }
            case R.id.trip_confirm:{
                searchTripsLater();
                break;
            }
        }

    }

    private  void searchForOnTripRides(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference =mDb.collection(getString(R.string.collection_trips));
        Query tripsQuery =tripCollectionReference.whereEqualTo("trip_destination",tripTo.getText().toString()).whereEqualTo("status","On trip");
        tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot :task.getResult()){
                        TripDetail trip=documentSnapshot.toObject(TripDetail.class);
                        tripFromFireStore.add(trip);
                        Log.d(TAG, "onComplete: Matched Trip"+trip.toString());
                    }
                }
            }
        });


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
                    geoPoint=new GeoPoint(location.getLatitude(),location.getLongitude());

                    Log.d(TAG, "onComplete: Location Coordinates:"+geoPoint.toString());
                }
            }
        });
    }
    private  void searchTripsLater(){
        Log.d(TAG, "searchTripsLater: Searching for Trips");
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference =mDb.collection(getString(R.string.collection_trips));
        Query  tripsQuery =tripCollectionReference.whereEqualTo("trip_source",tripFrom.getText().toString()).whereEqualTo("trip_destination",tripTo.getText().toString());
        tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot :task.getResult()){
                        TripDetail trip=documentSnapshot.toObject(TripDetail.class);
                        tripFromFireStore.add(trip);
                        Log.d(TAG, "onComplete: Matched Trip"+trip.toString());
                    }
                }
            }
        });


    }
    private  void initThread(){
        SearchForRides  searchForRides =new SearchForRides(this,this,tripFrom.getText().toString(),tripTo.getText().toString());
        searchForRides.execute();

    }

    @Override
    public void tripsRetrieved(ArrayList<TripDetail> trips) {

    }
}
