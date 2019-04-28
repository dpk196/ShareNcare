package com.example.sharencare.threads;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.sharencare.Interfaces.SearchForOnTripRidesInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class SearchForOnTripRides extends AsyncTask<Void ,Void, ArrayList<UserLocation>> {
    private static final String TAG = "SearchForRides";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static GeoPoint geoPoint;
    private ArrayList<TripDetail> tripFromFireStore = new ArrayList();
    private WeakReference<SearchForOnTripRidesInterface> searchForRidesInterface;
    private Context mContext;
    private FirebaseFirestore mDb;
    private String tripTo;
    private String tripFrom;
    boolean onTripFlag = false;
    boolean laterTripFlag = false;
    boolean backGroundReturn = false;
    ArrayList<String> driverUserIDs = new ArrayList<>();
    ArrayList<UserLocation> locations = new ArrayList<>();
    ArrayList<UserLocation> matchedDriverLocations=new ArrayList<>();
    int count = 0;


    public SearchForOnTripRides(SearchForOnTripRidesInterface searchForRides, Context mContext, String tripFrom, String tripTo) {
        searchForRidesInterface=new WeakReference<>(searchForRides);
        this.mContext = mContext;
        this.tripTo = tripTo;
        this.tripFrom = tripFrom;
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    @Override
    protected ArrayList<UserLocation> doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: Called");
        getLastKnownLocation();
        searchForOnTripRides();
        while (backGroundReturn == false) {
            Log.d(TAG, "doInBackground: Waiting on the background thread");
        }

        return locations;
    }

    private void searchForOnTripRides() {
        Log.d(TAG, "searchForOnTripRides: Searching For rides");
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference = mDb.collection("collection_trips");
        Query tripsQuery = tripCollectionReference.whereEqualTo("trip_destination", tripTo).whereEqualTo("status", "On trip");
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
                        onTripFlag = true;
                    } else {
                        Log.d(TAG, "onComplete: Result is empty");
                        onTripFlag = true;
                    }
                } else {
                    onTripFlag = true;
                    Log.d(TAG, "onComplete: Query Unsuccesfull");
                }
            }
        });
        while (onTripFlag == false) {
            Log.d(TAG, "searchForOnTripRides: Still searching for rides");
        }
        if (tripFromFireStore.size() > 0) {
            Log.d(TAG, "searchForOnTripRides:Search Completed" + tripFromFireStore.get(0).toString());
        } else {
            Log.d(TAG, "searchForOnTripRides: No Trips Foundd......");
        }
        findDriversLocation(driverUserIDs);
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: Getting user last Known Location");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onComplete: Location Coordinates:" + geoPoint.toString());
                }
            }
        });
    }

    private void findDriversLocation(ArrayList<String> userIds) {
        onTripFlag = false;


        Log.d(TAG, "findDriversLocation: Number of Drivers Going to: " + tripTo + " in the riders area are:" + userIds.size());
        if (userIds.size() > 0) {
            for (String id : userIds) {
                onTripFlag = true;
                Log.d(TAG, "matchRiderAndDriver: Getting loaction with userId(Start):" + id);
                DocumentReference mUserLocationReference = FirebaseFirestore.getInstance().collection("UserLocation").document(id);
                mUserLocationReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().getData().isEmpty()) {
                            try {
                                UserLocation loc = task.getResult().toObject(UserLocation.class);
                                locations.add(loc);
                                Log.d(TAG, "onComplete: " + loc.toString());
                                count++;
                            } catch (Exception e) {
                                Log.d(TAG, "onComplete: inside retrieveUserLocations error Occured:" + e.getMessage());
                            }

                        } else {
                            Log.d(TAG, "onComplete: Query Result is Empty");
                        }
                    }
                });
            }
        }
        while (count != userIds.size()) {
            Log.d(TAG, "findDriversLocation: waiting for drivers location");
        }
        Log.d(TAG, "findDriversLocation:Before Returning: " + locations.size());
        backGroundReturn = true;

    }

    @Override
    protected void onPostExecute(ArrayList<UserLocation> userLocations) {
        LatLngBounds bounds;
        double bottomBoundary = geoPoint.getLatitude() - 1.1;
        double leftBoundary = geoPoint.getLongitude() - 1.1;
        double topBoundary = geoPoint.getLatitude() + 1.1;
        double rightBoundary = geoPoint.getLongitude() +1.1;
        bounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
        Log.d(TAG, "onPostExecute: called");
        if (userLocations.size() > 0) {
            for (UserLocation location : userLocations) {
                 Log.d(TAG, "onPostExecute: " + location.toString());
                 LatLng latLng=new LatLng(location.getGeoPoint().getLatitude(),location.getGeoPoint().getLongitude());
                Log.d(TAG, "onPostExecute: "+bounds.getCenter());
                 if(bounds.contains(latLng)){
                     matchedDriverLocations.add(location);
                     Log.d(TAG, "onPostExecute: Driver currently near you"+location.toString());
                 }else{
                     Log.d(TAG, "onPostExecute: This driver is not currently near you");
                 }
            }
        }
        Log.d(TAG, "onPostExecute: matchedDriverLocations size before sending"+matchedDriverLocations.size());
        searchForRidesInterface.get().matchedOnTripRides(matchedDriverLocations);
    }

    @Override
    protected void onPreExecute() {
        tripFromFireStore.clear();
        driverUserIDs.clear();
        locations.clear();
        matchedDriverLocations.clear();

    }
}
