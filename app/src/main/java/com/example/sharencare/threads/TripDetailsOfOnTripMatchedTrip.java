package com.example.sharencare.threads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Interfaces.TripDetailsOfOnTripMatchedTripInterface;
import com.example.sharencare.Models.TripDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;

public class TripDetailsOfOnTripMatchedTrip extends AsyncTask<Void,Void, TripDetail> {
    private static final String TAG = "TripDetailsOfOnTripMatc";
    private TripDetail trip;
    private  WeakReference<TripDetailsOfOnTripMatchedTripInterface> details;
    private String userId;
    private Context mContext;
    private  boolean flag=false;

    public TripDetailsOfOnTripMatchedTrip(String userId, Context mContext,TripDetailsOfOnTripMatchedTripInterface detail) {
        details=new WeakReference<>(detail);
        this.userId = userId;
        this.mContext = mContext;
    }

    @Override
    protected TripDetail doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: called");
        getCurrentTripDetails();
        return trip;
    }

    private void getCurrentTripDetails(){
        Log.d(TAG, "getCurrentTripDetails: Called");
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        FirebaseFirestore mDb  =FirebaseFirestore.getInstance();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference = mDb.collection("collection_trips");
        Query tripsQuery = tripCollectionReference.whereEqualTo("status", "On trip").whereEqualTo("user_id", userId);
        tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        trip = documentSnapshot.toObject(TripDetail.class);
                        Log.d(TAG, "onComplete: received tripFromMessagingService:"+trip.toString());
                        break;
                    }
                }
                flag=true;
            }
        });
        while(flag!=true){
            Log.d(TAG, "getCurrentTripDetails: getting tripFromMessagingService details");
        }
    }

    @Override
    protected void onPostExecute(TripDetail tripDetail) {
        Log.d(TAG, "onPostExecute: called");
        details.get().getOnTripDetail(tripDetail);

    }
}
