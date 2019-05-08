package com.example.sharencare.threads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Interfaces.SearchForLaterOnTripsInterface;
import com.example.sharencare.Interfaces.SearchForOnTripRidesInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class SearchForRideLater extends AsyncTask<Void, Void, ArrayList<String>> {
    private static final String TAG = "SearchForRides";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static GeoPoint geoPoint;
    private ArrayList<TripDetail> tripFromFireStore = new ArrayList();
    private WeakReference<SearchForLaterOnTripsInterface> reference;
    private Context mContext;
    private FirebaseFirestore mDb;
    private String tripTo;
    private String tripFrom;
    boolean onTripFlag = false;
    boolean laterTripFlag = false;
    boolean backGroundReturn = false;
    ArrayList<String> driverUserIDs = new ArrayList<>();
    ArrayList<UserLocation> locations = new ArrayList<>();
    ArrayList<UserLocation> matchedDriverLocations = new ArrayList<>();
    int count = 0;

    public SearchForRideLater(SearchForLaterOnTripsInterface searchForRides, Context mContext, String tripFrom, String tripTo) {
        reference = new WeakReference<>(searchForRides);
        this.mContext = mContext;
        this.tripTo = tripTo;
        this.tripFrom = tripFrom;
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: Called");
        return searchForLaterOnRides();
    }

    private ArrayList<String> searchForLaterOnRides() {
        Log.d(TAG, "searchForOnTripRides: Searching For rides");
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference = mDb.collection("collection_trips");
        Query tripsQuery = tripCollectionReference.whereEqualTo("trip_destination", tripTo).whereEqualTo("trip_source", tripFrom).
                whereEqualTo("status", "Yet to Start");
        tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            TripDetail trip = documentSnapshot.toObject(TripDetail.class);
                            if (!trip.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                tripFromFireStore.add(trip);
                                driverUserIDs.add(trip.getUser_id());
                            }
                        }
                    }
                }
                onTripFlag = true;
            }
        });
        while (onTripFlag == false) {
            Log.d(TAG, "searchForOnTripRides: Still searching for rides");
        }
        if (tripFromFireStore.size() > 0) {
            for (TripDetail tripDetail : tripFromFireStore) {
                Log.d(TAG, "searchForOnTripRides:Search Completed" + tripDetail.toString());
            }
        } else {
            Log.d(TAG, "searchForOnTripRides: No Trips Foundd......");
        }
        return driverUserIDs;
    }


    @Override
    protected void onPreExecute() {
        tripFromFireStore.clear();
        driverUserIDs.clear();
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        reference.get().matchedLaterOnTrips(strings);
    }
}
