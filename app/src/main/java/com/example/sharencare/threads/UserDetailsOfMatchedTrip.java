package com.example.sharencare.threads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Interfaces.UserDetailsOfMatchedTripInterface;
import com.example.sharencare.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;

public class UserDetailsOfMatchedTrip extends AsyncTask<Void,Void, User> {
    private static final String TAG = "UserDetailsOfMatchedTri";
    private  User user;
    private String userId;
    private Context mContext;
    private  boolean flag=false;
    private WeakReference<UserDetailsOfMatchedTripInterface> details;

    public UserDetailsOfMatchedTrip(String userId, Context mContext, UserDetailsOfMatchedTripInterface detail) {
        this.userId = userId;
        this.mContext = mContext;
        details=new WeakReference<>(detail);
    }

    @Override
    protected User doInBackground(Void... voids) {
        getUserDetails(userId);
        return user;
    }

    private void getUserDetails(String userId) {
        FirebaseFirestore mDb=FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference = mDb.collection("Users");
        Query tripsQuery = tripCollectionReference.whereEqualTo("user_id", userId);
        tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                 if(task.isSuccessful()) {
                     if (!task.getResult().isEmpty()) {
                         for (QueryDocumentSnapshot userSnapshot : task.getResult()) {
                             user = userSnapshot.toObject(User.class);
                             Log.d(TAG, "onComplete: " + user.toString());
                         }
                     }else{
                         Log.d(TAG, "onComplete: User Not found ");
                     }
                 }else{
                     Log.d(TAG, "onComplete: User not Found");
                 }
                 flag=true;
            }
        });
        while(flag!=true){
            Log.d(TAG, "getUserDetails: Getting user Details from fireStore");
        };
    }

    @Override
    protected void onPostExecute(User user) {
        Log.d(TAG, "onPostExecute: Called");
        Log.d(TAG, "onPostExecute: "+user.toString());
        details.get().userDetailsReceived(user);
    }
}
