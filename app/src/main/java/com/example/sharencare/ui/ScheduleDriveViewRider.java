package com.example.sharencare.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.R;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static com.example.sharencare.utils.StaticPoolClass.currentUserDetails;
import static com.example.sharencare.utils.StaticPoolClass.ifRideRejected;
import static com.example.sharencare.utils.StaticPoolClass.otherUserDetails;

public class ScheduleDriveViewRider extends AppCompatActivity {
    private static final String TAG = "ScheduleDriveViewRider";
    private TextView tripStartTime, tripDistance, tripDuration, tripFare, tripFrom, tripTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_drive_view_rider);
        tripStartTime = findViewById(R.id.trip_start_time);
        tripDistance = findViewById(R.id.trip_distance);
        tripDuration = findViewById(R.id.trip_duration);
        tripFare = findViewById(R.id.trip_fare);
        tripFrom = findViewById(R.id.trip_from);
        tripTo = findViewById(R.id.trip_to);
        tripStartTime.setText(StaticPoolClass.tripDetailsForScheduleRide.getStart_time());
        tripDistance.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_distance());
        tripDuration.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_duration());
        tripFare.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_fare());
        tripFrom.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_source());
        tripTo.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_destination());
        findViewById(R.id.accept_ride).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (ifRideRejected == false) {
                        SendFCMRequest sendFCMRequest = new SendFCMRequest("Ready to ride", StaticPoolClass.otherUserDetails.getToken(), "allowed_to_show_trip_details_accepted_rider", "", "Rider says", "");
                        sendFCMRequest.sendRequest();
                        submitRideTOFireStore();
                    } else {
                        Log.d(TAG, "onClick: Driver is unable to ride with you");
                        Toast.makeText(ScheduleDriveViewRider.this, "Sorry can't share details now", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.d(TAG, "onClick: Someting went wrong:" + e.getMessage());
                    Toast.makeText(ScheduleDriveViewRider.this, "Try again", Toast.LENGTH_SHORT).show();
                }

            }
        });
        findViewById(R.id.call_rider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri u = Uri.parse("tel:" + StaticPoolClass.otherUserDetails.getMobile_number());
                Intent i = new Intent(Intent.ACTION_DIAL, u);
                try {
                    startActivity(i);
                } catch (SecurityException e) {
                    Log.d(TAG, "onClick: Exception" + e.getMessage());
                }
            }
        });
        findViewById(R.id.cancel_trip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SendFCMRequest sendFCMRequest = new SendFCMRequest("Currently unable to ride", StaticPoolClass.otherUserDetails.getToken(), "allowed_to_show_trip_details_rejected", "", "Rider says", "");
                    sendFCMRequest.sendRequest();
                    startActivity(new Intent(ScheduleDriveViewRider.this, MainActivity.class));
                } catch (Exception e) {
                    Log.d(TAG, "onClick: Someting went wrong:" + e.getMessage());
                    Toast.makeText(ScheduleDriveViewRider.this, "Try again", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void submitRideTOFireStore() {
        StaticPoolClass.tripDetailsForScheduleRide.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        StaticPoolClass.tripDetailsForScheduleRide.setStatus("On Trip with" + " " + otherUserDetails.getUsername() + " " + "on" + " " + StaticPoolClass.tripDetailsForScheduleRide.getTrip_date() + " " + "at" + " " + StaticPoolClass.tripDetailsForScheduleRide.getStart_time());
        Log.d(TAG, "submitDetailsToFireStore: Before send to fireStore" + StaticPoolClass.tripDetailsForScheduleRide.toString());
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        DocumentReference newTripRef = mDb.collection(getString(R.string.collection_trips)).document();
        StaticPoolClass.tripDetailsForScheduleRide.setTripId(newTripRef.getId());
        newTripRef.set(StaticPoolClass.tripDetailsForScheduleRide).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: successfully submitted");
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

    }
}
