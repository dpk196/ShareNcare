package com.example.sharencare.threads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Interfaces.FCMTokenInterface;
import com.example.sharencare.Models.User;
import com.example.sharencare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GetMatchedFCMToken extends AsyncTask<Void,Void, ArrayList<String>> {
    private static final String TAG = "GetMatchedFCMToken";
    private  ArrayList<String> userIds;
    private Context mContext;
    ArrayList<String> tokenList=new ArrayList<>();
    private WeakReference<FCMTokenInterface> fcmTokens;
    public GetMatchedFCMToken(ArrayList<String> userIds, Context mContext,FCMTokenInterface token) {
        this.userIds = userIds;
        this.mContext = mContext;
        fcmTokens=new WeakReference<>(token);
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        for(String user: userIds){
            Log.d(TAG, "doInBackground: Getting Token for UserId"+user);
          getFcmToken(user);
        }
        while(userIds.size()!=tokenList.size()){
            Log.d(TAG, "doInBackground: Still getting Tokens");
        };
        return tokenList;
    }

    private  void getFcmToken(String userId){
        Log.d(TAG, "getFcmToken: Called");
        FirebaseFirestore mDb =FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference mReference=mDb.collection("Users");
        Query mQuery =mReference.whereEqualTo("user_id",userId);
        mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot usr : task.getResult()) {
                    User user=usr.toObject(User.class);
                    tokenList.add(user.getToken());
                }
            }
        });

    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        Log.d(TAG, "onPostExecute: Called");
        fcmTokens.get().getToken(strings);
    }
}
