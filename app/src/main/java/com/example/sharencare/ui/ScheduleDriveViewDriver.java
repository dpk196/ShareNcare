package com.example.sharencare.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.R;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.sharencare.utils.StaticPoolClass.otherUserDetails;

public class ScheduleDriveViewDriver extends AppCompatActivity  {
    private static final String TAG = "ScheduleDriveViewDriver";
    private  Button TripDetails;
    private TextView tripStartTime,tripDistance,tripDuration,tripFare,tripFrom,tripTo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_drive_view);
        tripStartTime=findViewById(R.id.trip_start_time);
        tripDistance=findViewById(R.id.trip_distance);
        tripDuration=findViewById(R.id.trip_duration);
        tripFare=findViewById(R.id.trip_fare);
        tripFrom=findViewById(R.id.trip_from);
        tripTo=findViewById(R.id.trip_to);
        tripStartTime.setText(StaticPoolClass.tripDetailsForScheduleRide.getStart_time());
        tripDistance.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_distance());
        tripDuration.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_duration());
        tripFare.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_fare());
        tripFrom.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_source());
        tripTo.setText(StaticPoolClass.tripDetailsForScheduleRide.getTrip_destination());
        TripDetails=findViewById(R.id.trip_allow_to_view_details);
        findViewById(R.id.trip_allow_to_view_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    SendFCMRequest sendFCMRequest = new SendFCMRequest("Tab to see Trip Details", StaticPoolClass.otherUserDetails.getToken(), "allowed_to_show_trip_details", "","Trip Details","");
                    sendFCMRequest.sendRequest();
                }catch (Exception e){
                    Log.d(TAG, "onClick: Someting went wrong:"+e.getMessage());
                    Toast.makeText(ScheduleDriveViewDriver.this, "Try again", Toast.LENGTH_SHORT).show();
                }


            }
        });
        findViewById(R.id.call_rider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri u = Uri.parse("tel:" +StaticPoolClass.otherUserDetails.getMobile_number());
                Intent i = new Intent(Intent.ACTION_DIAL, u);
                try{
                    startActivity(i);
                }catch (SecurityException e){
                    Log.d(TAG, "onClick: Exception"+e.getMessage());
                }
            }
        });
        findViewById(R.id.accept_trip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StaticPoolClass.acceptSchuledRideFlag==true){
                    try{
                        TripDetails.setVisibility(View.INVISIBLE);
                        SendFCMRequest sendFCMRequest = new SendFCMRequest("Trip accepted", StaticPoolClass.otherUserDetails.getToken(), "allowed_to_show_trip_details_accepted_driver", "","Car owner says","");
                        sendFCMRequest.sendRequest();
                        submitTripTOFireStore(StaticPoolClass.tripDetailsForScheduleRide.getTripId());
                    }catch (Exception e){
                        Log.d(TAG, "onClick: Someting went wrong:"+e.getMessage());
                        Toast.makeText(ScheduleDriveViewDriver.this, "Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.d(TAG, "onClick: Cannot accept");
                    Toast.makeText(ScheduleDriveViewDriver.this, "Wait for rider to confirm", Toast.LENGTH_SHORT).show();
                }


            }
        });
        findViewById(R.id.cancel_trip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    SendFCMRequest sendFCMRequest = new SendFCMRequest("Currently unable to ride", StaticPoolClass.otherUserDetails.getToken(), "allowed_to_show_trip_details_rejected", "","Car Owner says","");
                    sendFCMRequest.sendRequest();
                    startActivity(new Intent(ScheduleDriveViewDriver.this,MainActivity.class));
                }catch (Exception e){
                    Log.d(TAG, "onClick: Someting went wrong:"+e.getMessage());
                    Toast.makeText(ScheduleDriveViewDriver.this, "Try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void submitTripTOFireStore(String tripId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection(getString(R.string.collection_trips)).document(tripId);
        reference.update("status","On Trip with"+" "+otherUserDetails.getUsername()+" "+"on"+" "+StaticPoolClass.tripDetailsForScheduleRide.getTrip_date()+" "+"at"+" "+StaticPoolClass.tripDetailsForScheduleRide.getStart_time()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Details submitted successfully");
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
            }
        });
    }


}
