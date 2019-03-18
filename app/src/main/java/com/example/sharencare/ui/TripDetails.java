package com.example.sharencare.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class TripDetails extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TripDetail";
    TextView tripStartTime,tripDistance,tripDuration,tripFare,tripStatus,tripFrom,tripTo;
    FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        tripStartTime=findViewById(R.id.trip_start_time);
        tripDistance=findViewById(R.id.trip_distance);
        tripDuration=findViewById(R.id.trip_duration);
        tripFare=findViewById(R.id.trip_fare);
        tripStatus=findViewById(R.id.trip_status);
        tripFrom=findViewById(R.id.trip_from);
        tripTo=findViewById(R.id.trip_to);
        findViewById(R.id.set_trip_start_time).setOnClickListener(this);
        findViewById(R.id.trip_confirm).setOnClickListener(this);
        intent = getIntent();
        mAuth=FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        setTripDetails();
    }

    private void setTripDetails() {
        Log.d(TAG, "setTripDetails: setting trip details");
        String sourceText =intent.getStringExtra("sourceText");
        String destinationText=intent.getStringExtra("destinationText");
        String distanceText=intent.getStringExtra("distanceText");
        String durationText=intent.getStringExtra("durationText");
        String fareText=intent.getStringExtra("fareText");
        tripFrom.setText(sourceText);
        tripTo.setText(destinationText);
        tripDistance.setText(distanceText);
        tripDuration.setText(durationText);
        tripFare.setText(fareText);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_trip_start_time :{
                startActivity(new Intent(TripDetails.this,SetStartTime.class));
                break;
            }
            case R.id.trip_confirm:{
                 if(!tripStartTime.getText().toString().equals("")&&!tripDistance.getText().toString().equals("")&&
                     !tripDuration.getText().toString().equals("")&&!tripStatus.getText().toString().equals("")&&
                         !tripFare.getText().toString().equals("")){
                  submitDetailsToFireStore();
                 }
                 else{
                     Toast.makeText(this, "Please Fill the trip start Time ", Toast.LENGTH_SHORT).show();
                 }
                 break;
            }

        }
    }
    private void submitDetailsToFireStore(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        TripDetail userTripDetail = new TripDetail();
        userTripDetail.setTripDistance(tripDistance.getText().toString());
        userTripDetail.setTripDuration(tripDuration.getText().toString());
        userTripDetail.setTripFare(tripFare.getText().toString());
        userTripDetail.setTripStatus(tripStatus.getText().toString());
        userTripDetail.setTripStartTime(tripStartTime.getText().toString());
        userTripDetail.setTripSource(tripFrom.getText().toString());
        userTripDetail.setTripDestination(tripTo.getText().toString());
        userTripDetail.setUserID(FirebaseAuth.getInstance().getUid());
        DocumentReference newTripRef= mDb.collection(getString(R.string.collection_trips)).document();
        newTripRef.set(userTripDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Details Submitted Successfully");
                    Log.d(TAG, "onComplete: "+task.toString());
                    Toast.makeText(TripDetails.this, "You will be notified when a rider is found", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onComplete: Failed to submit details");
                }
            }
        });

    }
}
