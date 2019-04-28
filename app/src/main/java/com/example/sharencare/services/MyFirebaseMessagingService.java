package com.example.sharencare.services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String notificationBody="";
        String notificationTitle="";
        String notificationData="";
        try {
            notificationBody=remoteMessage.getNotification().getBody();
            notificationData=remoteMessage.getData().toString();
            notificationTitle=remoteMessage.getNotification().getTitle();

        }catch (NullPointerException e){
            Log.d(TAG, "onMessageReceived: Null pointer"+e.getMessage());
        }
        Log.d(TAG, "onMessageReceived: Data:"+notificationData);
        Log.d(TAG, "onMessageReceived: Body:"+notificationBody);
        Log.d(TAG, "onMessageReceived: Title:"+notificationTitle);
    }

    @Override
    public void onNewToken(String s) {
        sendResgistrationTokenToServer(s);
    }

    private void sendResgistrationTokenToServer(String token) {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference reference= db.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.update("token",token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Token Refreshed ");
                }
            }
        });


    }
}
