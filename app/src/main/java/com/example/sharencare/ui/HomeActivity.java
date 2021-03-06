package com.example.sharencare.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Interfaces.TripDetailsOfOnTripMatchedTripInterface;
import com.example.sharencare.Interfaces.UserDetailsOfMatchedTripInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.example.sharencare.threads.RetriveDetailsFromFireStore;
import com.example.sharencare.threads.TripDetailsOfOnTripMatchedTrip;
import com.example.sharencare.threads.UserDetailsOfMatchedTrip;
import com.example.sharencare.utils.NotifactionChannel;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static UserLocation userLocationFromHomeScreen;
    private Runnable mRunnable;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public static TextView user_name;
    private boolean flag = false;

    private static final int LOCATION_UPDATE_INTERVAL = 1000;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        user_name = findViewById(R.id.user_name);
        findViewById(R.id.rider).setOnClickListener(this);
        findViewById(R.id.driver).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this::onClick);
        findViewById(R.id.about_us).setOnClickListener(this::onClick);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setupFirebaseListener();
        user_name = findViewById(R.id.user_name);
        user_name.setText(MainActivity.currentUser.getUsername());
        getLastKnownLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RetriveDetailsFromFireStore.mLastQuery = null;
    }

    @Override
    public void onClick(View v) {
        try {
            Log.d(TAG, "onClick: Inside on Click" + MainActivity.currentUser.toString());
            switch (v.getId()) {
                case R.id.driver: {
                    String vehicle = MainActivity.currentUser.getVehicle_name();
                    String registration = MainActivity.currentUser.getRegistration_number();

                    if (vehicle.equals("") || registration.equals("")) {
                        startActivity(new Intent(HomeActivity.this, VehicleNameAndRegistration.class));
                        Log.d(TAG, "onClick: directing to  VehicleNameAndRegistration Activity");

                    } else {
                        startActivity(new Intent(HomeActivity.this, DriverActivity.class));
                        Log.d(TAG, "onClick: directing to Drivers Activity");
                    }
                    break;
                }
                case R.id.rider: {
                    startActivity(new Intent(HomeActivity.this, RiderActivity.class));
                    Log.d(TAG, "onClick: directing to Riders Activity");
                    break;
                }
                case R.id.sign_out: {
                    sendResgistrationTokenToServer();
                    break;
                }
                case R.id.about_us:{
                     startActivity(new Intent(HomeActivity.this,AboutUs.class));
                    break;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "onClick: Something went Wrong");
            Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
            ;
        }
    }
    private void sendResgistrationTokenToServer(){
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference reference = db.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            reference.update("token", "").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Token Send to FireStore ");
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }catch (Exception e){
            Log.d(TAG, "sendResgistrationTokenToServer: Error "+e.getMessage());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        try {
            StaticPoolClass.rideAcceptedFlag = false;
        } catch (Exception e) {
            Log.d(TAG, "onStart: Error!!!!" + e.getMessage());
        }

        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }

    private void setupFirebaseListener() {
        Log.d(TAG, "setupFirebaseListener: setting up the auth state listener.");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(HomeActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }

    private void saveUserLocation(UserLocation userLocation) {


        try {
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_userlocation))
                    .document(FirebaseAuth.getInstance().getUid());
            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Successfully updated userlocation");
                    }
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: " + e.getMessage());
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: Getting user last Known Location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    try{
                        Location location = task.getResult();
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        UserLocation userLocation = new UserLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), null, geoPoint);
                        saveUserLocation(userLocation);
                        Log.d(TAG, "onComplete: Location Coordinates:" + geoPoint.toString());
                    }catch(Exception e){
                        Log.d(TAG, "onComplete: Unable to get user Location");
                        Toast.makeText(HomeActivity.this, "Error while retriving your location. Please Restart the app", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

}
