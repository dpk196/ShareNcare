package com.example.sharencare.threads;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Interfaces.TripsRetrivedFromFireStoreInterFace;
import com.example.sharencare.Models.TripDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RetriveDetailsFromFireStore extends AsyncTask<Void,Void, ArrayList<TripDetail>> {
    private static final String TAG = "RetriveDetailsFromFireS";
    FirebaseAuth mAuth;
    public static DocumentSnapshot mLastQuery=null;
    WeakReference<TripsRetrivedFromFireStoreInterFace> tripsRetrivedFromFireStore;
    private FirebaseFirestore mDb;
    boolean flag=false;
    ArrayList<TripDetail> tripCollection=new ArrayList<>();
    public RetriveDetailsFromFireStore(TripsRetrivedFromFireStoreInterFace retrivedFromFireStore) {
        mDb = FirebaseFirestore.getInstance();
        tripsRetrivedFromFireStore=new WeakReference<>(retrivedFromFireStore);
    }



    private ArrayList<TripDetail> queryDatabase(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference mTripsReference=mDb.collection("collection_trips");
        Query mTripsQuery=null;
        if(mLastQuery!=null){
            Log.d(TAG, "queryDatabase: lastquery is not null");
            mTripsQuery = mTripsReference.whereEqualTo("user_id",FirebaseAuth.getInstance().getCurrentUser().getUid()).
                    orderBy("timestamp",Query.Direction.DESCENDING).
                    startAfter(mLastQuery);
        }else {
            Log.d(TAG, "queryDatabase: lastquery is  null");
            mTripsQuery = mTripsReference.whereEqualTo("user_id",FirebaseAuth.getInstance().getCurrentUser().getUid()).
                    orderBy("timestamp",Query.Direction.DESCENDING);
        }

        mTripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){
                        Log.d(TAG, "onComplete: No Trips corresponding to the User ");
                        Log.d(TAG, "onComplete: Returning to doInBackground");
                        flag=true;
                        return;
                    }
                    for(QueryDocumentSnapshot document : task.getResult()){
                      TripDetail result=document.toObject(TripDetail.class);
                      tripCollection.add(result);
                       // Log.d(TAG, "onComplete: "+result.toString());
                    }
                    if(task.getResult().size()!=0){
                        Log.d(TAG, "onComplete: Size greater than zero");
                       mLastQuery=task.getResult().getDocuments().get(task.getResult().size()-1);
                    }
                    flag=true;
                }else {
                    Log.d(TAG, "onComplete: Query Failed");
                    flag=true;
                }
            }
        });
        while(flag==false){
            Log.d(TAG, "queryDatabase: waiting for result");
        };
        return tripCollection;
    }


    @Override
    protected ArrayList<TripDetail> doInBackground(Void... voids) {
        return  queryDatabase();
    }

    @Override
    protected void onPostExecute(ArrayList<TripDetail> tripDetail) {
        Log.d(TAG, "onPostExecute: Called");
        tripsRetrivedFromFireStore.get().userTripsCollectionFromFirestore(tripDetail);
    }
}
