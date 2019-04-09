package com.example.sharencare.threads;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RetriveDetailsFromFireStore extends AsyncTask<Void,Void, TripDetail> {
    private static final String TAG = "RetriveDetailsFromFireS";
    FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    ArrayList<TripDetail> tripCollection=new ArrayList<>();
    public RetriveDetailsFromFireStore() {
        mDb = FirebaseFirestore.getInstance();
    }

    @Override
    protected TripDetail doInBackground(Void... voids) {
        queryDatabase();
        return null;
    }

    private void queryDatabase(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference mTripsReference=mDb.collection("collection_trips");
        Query mTripsQuery = mTripsReference.whereEqualTo("user_id",FirebaseAuth.getInstance().getCurrentUser().getUid());
        mTripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                      TripDetail result=document.toObject(TripDetail.class);
                      tripCollection.add(result);
                        Log.d(TAG, "onComplete: "+result.toString());
                    }

                }else {
                    Log.d(TAG, "onComplete: Query Failed");
                }
            }
        });
    }


}
