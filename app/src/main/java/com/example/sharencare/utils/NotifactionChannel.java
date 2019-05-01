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

import com.example.sharencare.Interfaces.TripDetailsOfOnTripMatchedTripInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.threads.TripDetailsOfOnTripMatchedTrip;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class NotifactionChannel extends Application {
    private static final String TAG = "NotifactionChannel";
    public static final String CHANNEL_Id="Notifiaction";


    private  FirebaseAuth mAuth;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificatioChannel();
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





}
