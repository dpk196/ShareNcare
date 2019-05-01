package com.example.sharencare.threads;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;


import com.example.sharencare.Interfaces.UserCurrentLocationFromFirestoreInterface;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.LocationService;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nullable;

public class UserCurrentLocationFromFireStore extends AsyncTask<Void,Void , UserLocation> {
    private static final String TAG = "LocationFromFireStore";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private WeakReference<UserCurrentLocationFromFirestoreInterface> fromFirestoreInterface;
    private Context mContext;
    boolean flag=false;
    private UserLocation userLocation;
    private  String userId;

    public UserCurrentLocationFromFireStore( String userId,Context mContext,UserCurrentLocationFromFirestoreInterface location) {
        this.mContext = mContext;
        this.userId = userId;
        fromFirestoreInterface=new WeakReference<>(location);
    }

    @Override
    protected UserLocation doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: Called");
        getLocationFromFirestore();
        return userLocation;
    }

    private void getLocationFromFirestore(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        FirebaseFirestore mDb  =FirebaseFirestore.getInstance();
        mDb.setFirestoreSettings(settings);
        CollectionReference locationCollectionReference = mDb.collection("UserLocation");
        Query locationQuery = locationCollectionReference.whereEqualTo("user_id", userId);
        locationQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
               if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot :task.getResult()){
                        userLocation=snapshot.toObject(UserLocation.class);
                       Log.d(TAG, "onComplete: UserLocation:"+userLocation.toString());
                       break;
                   }

               }
                flag=true;
            }
        });
        while(flag!=true){
            Log.d(TAG, "getLocationFromFirebase: Waiting for userLocation");
        };
    }

    @Override
    protected void onPostExecute(UserLocation userLocation) {
        if(userLocation!=null){
            fromFirestoreInterface.get().userCurrentLocation(userLocation);
        }else{
            Toast.makeText(mContext, "User Location not Found", Toast.LENGTH_SHORT).show();
        }
    }
}
