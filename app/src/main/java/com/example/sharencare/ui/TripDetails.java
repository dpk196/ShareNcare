package com.example.sharencare.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Interfaces.TaskDelegate;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.maps.model.DirectionsResult;

public class TripDetails extends AppCompatActivity  implements View.OnClickListener {
    private static final String TAG = "TripDetail";
    TextView tripStartTime,tripDistance,tripDuration,tripFare,tripStatus,tripFrom,tripTo;
    FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    Intent intent;
    private  static TripDetail  tripDetail;
    Button tripSubmitButton;
    String source;
    String destination;
    String duration;
    String distance;


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
        tripSubmitButton=findViewById(R.id.trip_confirm);
        tripSubmitButton.setOnClickListener(this::onClick);
        intent = getIntent();
        source=intent.getStringExtra("source");
        destination=intent.getStringExtra("destination");
        duration=intent.getStringExtra("duration");
        distance=intent.getStringExtra("distance");
        mAuth=FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
       // getTripsFromDriverActivity();
        startTimeFromSetStartTimeActivity();
        calculateFare();
        setTripDetailsView();
    }



    private void setTripDetailsView() {
        tripStartTime.setText("Set start time");
        tripDuration.setText(duration);
        tripFrom.setText(source);
        tripTo.setText(destination);
        tripStatus.setText("Yet to Start");
        tripDistance.setText(distance);
        tripFare.setText("Rs"+" "+calculateFare());


    }

//    private void getTripsFromDriverActivity(){
//        try{
//            if(tripDetail==null) {
//                tripDetail=new TripDetail();
//                DirectionsResult result = (DirectionsResult) intent.getSerializableExtra("DirectionsResults");
//                tripDetail.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                tripDetail.setStart_time("Yet to Start");
//                tripDetail.setStart_time("12:00");
//                Bundle bundle = getIntent().getParcelableExtra("bundle");
//                LatLng fromPosition = bundle.getParcelable("SourceGeoPoint");
//                LatLng toPosition = bundle.getParcelable("DestinationGeoPoint");
//                tripDetail.setSourceGeoPoint(new GeoPoint(fromPosition.latitude, fromPosition.longitude));
//                tripDetail.setDestinationGeoPoint(new GeoPoint(toPosition.latitude, toPosition.longitude));
//                tripDetail.setTrip_source(intent.getStringExtra("sourceText"));
//                tripDetail.setTrip_destination(intent.getStringExtra("destinationText"));
//                Log.d(TAG, "getTripsFromDriverActivity: " + tripDetail.toString());
//            }
//
//        }catch (Exception e){
//            Log.d(TAG, "setDetailsFromFireStore: "+e.getMessage());
//        }
//
//    }



//
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

        DocumentReference newTripRef= mDb.collection(getString(R.string.collection_trips)).document();
        newTripRef.set(tripDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Details Submitted Successfully");
                    Log.d(TAG, "onComplete: " + task.toString());
                    Toast.makeText(TripDetails.this, "You will be notified when a rider is found", Toast.LENGTH_SHORT).show();
                    tripSubmitButton.setVisibility(View.INVISIBLE);
                }else {
                    Log.d(TAG, "onComplete: Something went wrong");
                }
            }
        });


    }
    public void startTimeFromSetStartTimeActivity(){
        String time=intent.getStringExtra("PickedStartTime");
        Log.d(TAG, "startTimeFromSetStartTimeActivity: called"+time);
        try {
            if (tripDetail != null && !time.equals("")) {
                tripDetail.setStart_time(time);
                Log.d(TAG, "startTimeFromSetStartTimeActivity: TripDetails" + tripDetail.toString());
            }
        }catch (Exception e){
            Log.d(TAG, "startTimeFromSetStartTimeActivity: "+e.getMessage());
        }
    }


    private String calculateFare() {
        String f="";
        for(int i=0;i<distance.length()-3;i++){
            f=f+distance.charAt(i);
        }
        Double  fare= Double.valueOf(f)*7;
        Integer f_re=fare.intValue();
        Log.d(TAG, "calculateFare: Fare:"+f_re.toString());
        return f_re.toString();
    }

}
