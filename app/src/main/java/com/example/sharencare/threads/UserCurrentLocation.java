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
import com.example.sharencare.services.LocationService;
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

public class UserCurrentLocation extends AsyncTask<Void,Void , Void> {
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private static final String TAG = "UserCurrentLocation";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    Context mContext;
    boolean flag=true;
    boolean flagCoordinates=false;
    private FirebaseFirestore mDb;
    GeoPoint geoPoint;


    public UserCurrentLocation(Context mContext,UserCurrentLocationInterface location) {
        this.mContext = mContext;
         mDb=FirebaseFirestore.getInstance();

    }


    @Override
    protected Void doInBackground(Void... voids) {


        return  null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        try {
            Log.d(TAG, "onPostExecute: "+geoPoint.toString());
        }catch (Exception e){
            Log.d(TAG, "onPostExecute: "+e.getMessage());
        }

    }
}
