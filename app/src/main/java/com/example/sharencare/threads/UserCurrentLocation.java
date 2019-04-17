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

import com.example.sharencare.Interfaces.UserCurrentLocationInterface;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nullable;

public class UserCurrentLocation extends AsyncTask<Void,Void , GeoPoint> {
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private static final String TAG = "UserCurrentLocation";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    Context mContext;
    boolean flagLocality=false;
    boolean flagCoordinates=false;
    private FirebaseFirestore mDb;


    public UserCurrentLocation(Context mContext,UserCurrentLocationInterface location) {
        this.mContext = mContext;
         mDb=FirebaseFirestore.getInstance();

    }

    @Override
    protected GeoPoint  doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: called");
        mDb=FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference mCollectionReference=mDb.collection("collection_userlocation");
        Query userLocationQuery=mCollectionReference.whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        userLocationQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
               for(DocumentSnapshot doc:queryDocumentSnapshots){
                   UserLocation userLocation=doc.toObject(UserLocation.class);
                   Log.d(TAG, "onEvent: "+userLocation.toString());
               }
            }
        });


        return null;

    }





}
