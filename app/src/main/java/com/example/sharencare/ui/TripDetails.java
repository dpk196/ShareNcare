package com.example.sharencare.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sharencare.Interfaces.TaskDelegate;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.utils.DatePickerDialogFragment;
import com.example.sharencare.utils.TimePickerDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.maps.model.DirectionsResult;

public class TripDetails extends AppCompatActivity  implements View.OnClickListener , TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "TripDetail";
    TextView tripStartTime, tripDistance, tripDuration, tripFare, tripStatus, tripFrom, tripTo,setTripStartTime,tripStartDate;
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
        setContentView(R.layout.activity_trip_details);
        tripStartTime = findViewById(R.id.trip_start_time);
        tripDistance = findViewById(R.id.trip_distance);
        tripDuration = findViewById(R.id.trip_duration);
        tripFare = findViewById(R.id.trip_fare);
        tripStatus = findViewById(R.id.trip_status);
        tripFrom = findViewById(R.id.trip_from);
        tripTo = findViewById(R.id.trip_to);
        setTripStartTime= findViewById(R.id.trip_start_time);
        setTripStartTime.setOnClickListener(this);
        tripSubmitButton = findViewById(R.id.trip_confirm);
        tripSubmitButton.setOnClickListener(this::onClick);
        tripStartDate=findViewById(R.id.trip_start_date);
        tripStartDate.setOnClickListener(this::onClick);
        intent = getIntent();
        source = intent.getStringExtra("source");
        destination = intent.getStringExtra("destination");
        duration = intent.getStringExtra("duration");
        distance = intent.getStringExtra("distance");
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        prepareTripDetailObject();


    }
    public void prepareTripDetailObject(){
        if(tripDetail==null) {
            Log.d(TAG, "prepareTripDetailObject: TripDetails is Null");
            tripDetail=new TripDetail();
            tripDetail.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            tripDetail.setTrip_source(source);
            tripDetail.setTrip_destination(destination);
            tripDetail.setTrip_duration(duration);
            tripDetail.setTrip_fare(calculateFare());
            tripDetail.setStatus("Yet to Start");
            tripDetail.setTrip_distance(distance);
            Log.d(TAG, "prepareTripDetailObject: " + tripDetail.toString());
            setTripDetailsView(tripDetail);
        }else{
            Log.d(TAG, "prepareTripDetailObject: tripDetails is not null calling setTripDetails");
            setTripDetailsView(tripDetail);


        }
    }


    private void setTripDetailsView(TripDetail tripDetail) {
        tripDuration.setText(tripDetail.getTrip_duration());
        tripFrom.setText(tripDetail.getTrip_source());
        tripTo.setText(tripDetail.getTrip_destination());
        tripStatus.setText(tripDetail.getStatus());
        tripDistance.setText(tripDetail.getTrip_distance());
        tripFare.setText(tripDetail.getTrip_fare());


    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trip_start_time: {
                DialogFragment timePicker= new TimePickerDialogFragment();
                timePicker.show(getSupportFragmentManager(),"time picker");
                break;
            }
            case R.id.trip_start_date:{
                DialogFragment datepicker=new DatePickerDialogFragment();
                datepicker.show(getSupportFragmentManager(),"date picker");
                break;
            }
            case R.id.trip_confirm: {
                try {
                    if (!tripDetail.getStart_time().equals("") && !tripDetail.getTrip_date().equals("")) {
                        submitDetailsToFireStore();
                    } else {
                        Toast.makeText(this, "Please Fill the trip start Time and Date", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d(TAG, "onClick: Submit Details"+e.getMessage());
                }
                break;
            }

        }
    }

    private void submitDetailsToFireStore() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newTripRef = mDb.collection(getString(R.string.collection_trips)).document();
        newTripRef.set(tripDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Details Submitted Successfully");
                    Log.d(TAG, "onComplete: " + task.toString());
                    Toast.makeText(TripDetails.this, "You will be notified when a rider is found", Toast.LENGTH_SHORT).show();
                    tripSubmitButton.setVisibility(View.INVISIBLE);
                } else {
                    Log.d(TAG, "onComplete: Something went wrong");
                }
            }
        });


    }



    private String calculateFare() {
        Log.d(TAG, "calculateFare: Called");
        String f = "";
        try {
            for (int i = 0; i < distance.length() - 3; i++) {
                f = f + distance.charAt(i);
            }
            Double toDf = Double.valueOf(f) * 7;
            Integer f_re = toDf.intValue();
            fare=f_re.toString();
            Log.d(TAG, "calculateFare: Fare:" + f_re.toString());

        } catch (Exception e) {

        }
        return fare;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(hourOfDay>11)
        tripDetail.setStart_time(hourOfDay+":"+minute+" "+"PM");
        else
            tripDetail.setStart_time(hourOfDay+":"+minute+" "+"AM");
        tripStartTime.setText(tripDetail.getStart_time());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        tripDetail.setTrip_date(dayOfMonth+"/"+month+"/"+year);
        Log.d(TAG, "onDateSet: "+tripDetail.getTrip_date());
        tripStartDate.setText(tripDetail.getTrip_date());
    }
}
