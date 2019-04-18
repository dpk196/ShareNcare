package com.example.sharencare.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
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
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.utils.DatePickerDialogFragment;
import com.example.sharencare.utils.TimePickerDialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;

import static com.example.sharencare.utils.CalculateFare.calculateFare;

public class TripDetailsDriver extends AppCompatActivity  implements View.OnClickListener , TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
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
   public static UserLocation userLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_driver);
        tripStartTime = findViewById(R.id.trip_start_time);
        tripDistance = findViewById(R.id.trip_distance);
        tripDuration = findViewById(R.id.trip_duration);
        tripFare = findViewById(R.id.trip_fare);
        tripFrom = findViewById(R.id.trip_from);
        tripTo = findViewById(R.id.trip_to);
        setTripStartTime= findViewById(R.id.trip_start_time);
        setTripStartTime.setOnClickListener(this);
        tripSubmitButton = findViewById(R.id.trip_confirm_later);
        findViewById(R.id.start_trip_button).setOnClickListener(this);
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        prepareTripDetailObject();
        getLastKnownLocation();


    }
    public void prepareTripDetailObject(){
        if(tripDetail==null) {
            Log.d(TAG, "prepareTripDetailObject: TripDetails is Null");
            tripDetail=new TripDetail();
            tripDetail.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            tripDetail.setTrip_source(source);
            tripDetail.setTrip_destination(destination);
            tripDetail.setTrip_duration(duration);
            tripDetail.setTrip_fare(calculateFare(distance));
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
            case R.id.trip_confirm_later: {
                try {
                    if (!tripDetail.getStart_time().equals("") && !tripDetail.getTrip_date().equals("")) {
                        submitDetailsToFireStore();
                    } else {
                        Toast.makeText(this, "Please Fill the trip start Time and Date", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d(TAG, "onClick: Submit Details"+e.getMessage());
                    Toast.makeText(this, "Please Fill the trip start Time and Date", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.start_trip_button:{
                Intent mapsActIntent=new Intent(TripDetailsDriver.this,MapsActivity.class);
                startActivity(mapsActIntent);
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
                    Toast.makeText(TripDetailsDriver.this, "You will be notified when a rider is found", Toast.LENGTH_SHORT).show();
                    tripSubmitButton.setVisibility(View.INVISIBLE);
                    findViewById(R.id.start_trip_button).setVisibility(View.INVISIBLE);
                } else {
                    Log.d(TAG, "onComplete: Something went wrong");
                }
            }
        });


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
                    GeoPoint geoPoint=new GeoPoint(location.getLatitude(),location.getLongitude());
                    userLocation= new UserLocation();
                    userLocation.setGeoPoint(geoPoint);
                    Log.d(TAG, "onComplete: Location Coordinates:"+userLocation.toString());
                }
            }
        });
    }
}

