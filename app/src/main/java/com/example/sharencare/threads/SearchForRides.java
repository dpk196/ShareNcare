package com.example.sharencare.threads;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Interfaces.SearchForTripsInterface;
import com.example.sharencare.Models.TripDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class SearchForRides extends AsyncTask<Void ,Void, ArrayList<TripDetail>> {
    private static final String TAG = "SearchForRides";
    String tripFrom;
    String tripTo;
    WeakReference<SearchForTripsInterface> searchForTripsInterface;
    private  boolean flag=false;
    private FirebaseFirestore mDb;
    private  ArrayList<TripDetail> trips;

    public SearchForRides(String tripFrom, String tripTo,SearchForTripsInterface rides) {
        this.tripFrom = tripFrom;
        this.tripTo = tripTo;
        searchForTripsInterface=new WeakReference<>(rides);

        Log.d(TAG, "SearchForRides: Inside Constructor:"+tripFrom+" "+tripTo);
    }

    

    @Override
    protected ArrayList<TripDetail> doInBackground(Void... voids) {
        return searchForRides();
    }

    private  ArrayList<TripDetail> searchForRides(){
        mDb=FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference mTripsReference=mDb.collection("collection_trips");
        Query tripsQuery =mTripsReference.whereEqualTo("trip_source",tripFrom).whereEqualTo("trip_destination",tripTo);
         tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
             @Override
             public void onComplete(@NonNull Task<QuerySnapshot> task) {
                 if(task.isSuccessful()){
                     Log.d(TAG, "onComplete: Query SuccessFull");
                     for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                         TripDetail tripDetail=documentSnapshot.toObject(TripDetail.class);

                             trips.add(tripDetail);
                             Log.d(TAG, "onComplete: Query from FireStore" + tripDetail.toString());

                     }
                     flag=true;
                 }
             }
         });
//        while (flag==false){
//            Log.d(TAG, "searchForRides: Still searching for rides");
//        };
        return  trips;
    }

    @Override
    protected void onPostExecute(ArrayList<TripDetail> tripDetails) {
        super.onPostExecute(tripDetails);
        Log.d(TAG, "onPostExecute: Called");
        searchForTripsInterface.get().tripsRetrieved(tripDetails);

    }
}
