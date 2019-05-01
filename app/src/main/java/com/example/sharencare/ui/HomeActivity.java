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
import android.widget.Toast;

import com.example.sharencare.Interfaces.TripDetailsOfOnTripMatchedTripInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.threads.RetriveDetailsFromFireStore;
import com.example.sharencare.threads.TripDetailsOfOnTripMatchedTrip;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, TripDetailsOfOnTripMatchedTripInterface {
    private static final String TAG = "HomeActivity";
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public  static boolean onTripAlreayPresent=false;
    public static UserLocation userLocationFromNotificationChannelApplication;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewById(R.id.rider).setOnClickListener(this);
        findViewById(R.id.driver).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this::onClick);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setupFirebaseListener();
        initThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RetriveDetailsFromFireStore.mLastQuery=null;
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.driver:{
                               startActivity(new Intent(HomeActivity.this, RiderActivity.class));
               Log.d(TAG, "onClick: directing to Drivers Activity");
               break;
           }
           case R.id.rider:{
               startActivity(new Intent(HomeActivity.this, DriverActivity.class));
               Log.d(TAG, "onClick: directing to Riders Activity");
               break;
           }
           case R.id.sign_out:{
               FirebaseAuth.getInstance().signOut();
               break;
           }
       }
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener!=null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }
    private void setupFirebaseListener(){
        Log.d(TAG, "setupFirebaseListener: setting up the auth state listener.");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                }else{
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(HomeActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }
    private void initThread(){
        Log.d(TAG, "initThread: "+FirebaseAuth.getInstance().getCurrentUser().getUid());
        TripDetailsOfOnTripMatchedTrip trip = new TripDetailsOfOnTripMatchedTrip(FirebaseAuth.getInstance().getCurrentUser().getUid(), this, this);
        trip.execute();
        getLastKnownLocation();

    }

    @Override
    public void getOnTripDetail(TripDetail tripDetail) {
        if(tripDetail!=null){
            Log.d(TAG, "getOnTripDetail:Driver is already onTrip "+tripDetail.toString());
            onTripAlreayPresent=true;
        }else {
            Log.d(TAG, "getOnTripDetail: Not  Ontrip ");
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: Getting user last Known Location");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        userLocationFromNotificationChannelApplication = new UserLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), null, geoPoint);
                        saveUserLocation(userLocationFromNotificationChannelApplication);
                        Log.d(TAG, "onComplete: Location Coordinates:" + geoPoint.toString());
                    }
                }
            });
        }catch (Exception e){
            Log.d(TAG, "getLastKnownLocation: Cannot get user last known location");
            Toast.makeText(this, "Cannot get last Known Location", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveUserLocation(UserLocation userLocation) {

        try {
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_userlocation))
                    .document(FirebaseAuth.getInstance().getUid());
            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: Successfully updated userlocation");
                    }
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
        }
    }
}
