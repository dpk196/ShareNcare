package com.example.sharencare.utils;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class NotifactionChannelAndLocationUpdate extends Application {
    private static final String TAG = "NotifactionChannel";
    public static final String CHANNEL_Id="Notifiaction";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificatioChannel();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastKnownLocation();
    }

    private void createNotificatioChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defaultNotifiaction = new NotificationChannel(
                    CHANNEL_Id,
                    "Notifiaction",
                    NotificationManager.IMPORTANCE_HIGH
            );
            defaultNotifiaction.setDescription("Default Notification Channel");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(defaultNotifiaction);
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
                if(task.isSuccessful()){
                    Location location=task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    UserLocation userLocation = new UserLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), null, geoPoint);
                    saveUserLocation(userLocation);
                    Log.d(TAG, "onComplete: Location Coordinates:"+geoPoint.toString());
                }
            }
        });
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
