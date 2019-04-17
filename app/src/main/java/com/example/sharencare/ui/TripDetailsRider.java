package com.example.sharencare.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.utils.CalculateFare;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TripDetailsRider extends AppCompatActivity {

    TextView tripStartTime, tripDistance, tripDuration, tripFare, tripFrom, tripTo;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_rider);
        tripDistance = findViewById(R.id.trip_distance);
        tripDuration = findViewById(R.id.trip_duration);
        tripFare = findViewById(R.id.trip_fare);
        tripFrom = findViewById(R.id.trip_from);
        tripTo = findViewById(R.id.trip_to);
        intent = getIntent();
        setTripDetailsView();
    }
    private void setTripDetailsView() {
        tripDuration.setText(intent.getStringExtra("duration"));
        tripFrom.setText(intent.getStringExtra("tripFrom"));
        tripTo.setText(intent.getStringExtra("tripTo"));
        tripDistance.setText(intent.getStringExtra("distance"));
        tripFare.setText(CalculateFare.calculateFare(intent.getStringExtra("distance")));
    }
}
