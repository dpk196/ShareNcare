package com.example.sharencare.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.sharencare.Interfaces.TripDetailsOfOnTripMatchedTripInterface;
import com.example.sharencare.Interfaces.UserCurrentLocationFromFirestoreInterface;
import com.example.sharencare.Interfaces.UserDetailsOfMatchedTripInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.threads.TripDetailsOfOnTripMatchedTrip;
import com.example.sharencare.threads.UserCurrentLocationFromFireStore;
import com.example.sharencare.threads.UserDetailsOfMatchedTrip;
import com.example.sharencare.ui.RidesFoundShowOnMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.example.sharencare.utils.NotifactionChannel.CHANNEL_Id;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements UserDetailsOfMatchedTripInterface, TripDetailsOfOnTripMatchedTripInterface, UserCurrentLocationFromFirestoreInterface {
    private static final String TAG = "MyFirebaseMessagingServ";
    private static final int BROADCAST_NOTIFICATION_ID = 1;
    private String notificationData="";
    private  String title="";
    private  String fromUserId="";
    private String  myUserId="";
    private String  data_type;
    public static User userRiderDetailsFromMessagingService;
    public static TripDetail tripFromMessagingService;
    public  static UserLocation userLocationFromMessagingService;
    private  String message;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        try {
            notificationData=remoteMessage.getData().toString();
            title = remoteMessage.getData().get(getString(R.string.data_title));
            fromUserId= remoteMessage.getData().get(getString(R.string.fromUserId));
            myUserId= remoteMessage.getData().get(getString(R.string.toUserId));
            data_type=remoteMessage.getData().get(getString(R.string.data_type));
            if(data_type.equals("data_type_ride_request")) {
                Log.d(TAG, "onMessageReceived: A rider Request");
                initRiderRequestThreads(fromUserId, myUserId);
                message="wants to ride with you";
            }

            if(data_type.equals("data_type_ride_accepted")){
                initRiderRequestThreads(fromUserId, myUserId);
                message="accepted your request";

            }
            if(data_type.equals("data_type_ride_rejected")){

            }

        }catch (NullPointerException e){
            Log.d(TAG, "onMessageReceived: Null pointer"+e.getMessage());
        }
        Log.d(TAG, "onMessageReceived: Data:"+notificationData);

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendBroadcastNotification(String title, String message){
        Log.d(TAG, "sendBroadcastNotification: building a  notification");


        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_Id);
        // Creates an Intent for the Activity

        Intent notifyIntent = new Intent(this, RidesFoundShowOnMap.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.drawable.applogo)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.applogo))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(message)
                .setColor(getColor(R.color.blue4))
                .setAutoCancel(true);

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(BROADCAST_NOTIFICATION_ID, builder.build());

    }

    private  void initRiderRequestThreads(String fromUserID,String myUserID){
        Log.d(TAG, "initThreads: called");
        UserDetailsOfMatchedTrip userDetailsOfMatchedTrip=new UserDetailsOfMatchedTrip(fromUserID,this,this);
        TripDetailsOfOnTripMatchedTrip tripDetailsOfOnTripMatchedTrip=new TripDetailsOfOnTripMatchedTrip(myUserID,this,this);
        UserCurrentLocationFromFireStore location=new UserCurrentLocationFromFireStore(fromUserID,this,this);
        location.execute();
        tripDetailsOfOnTripMatchedTrip.execute();
        userDetailsOfMatchedTrip.execute();

    }
    private void initDriverResponseToRiderThread(String fromUserID,String myUserID ) {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void userDetailsReceived(User user) {
        Log.d(TAG, "userDetailsReceived: User Details Received of Rider:"+user.getUsername());
        if(user!=null) {
            userRiderDetailsFromMessagingService =user;
            sendBroadcastNotification(title, user.getUsername() + " "+message);
        }else{
            Log.d(TAG, "userDetailsReceived: User Object is Empty cannot send notification");
        }

    }

    @Override
    public void getOnTripDetail(TripDetail tripDetail) {
        Log.d(TAG, "getOnTripDetail: Called");
        if(tripDetail!=null){
            tripFromMessagingService =tripDetail;
            Log.d(TAG, "getOnTripDetail: Received:"+ tripFromMessagingService.toString());
        }else {
            Log.d(TAG, "getOnTripDetail: Something went wrong");
        }
    }
    @Override
    public void userCurrentLocation(UserLocation location) {
        Log.d(TAG, "userCurrentLocation: Called ");
        userLocationFromMessagingService=location;

    }



   //.............................................................................

    @Override
    public void onNewToken(String s) {
        sendRegistrationTokenToServer(s);
    }
    private void sendRegistrationTokenToServer(String token) {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        try {
            DocumentReference reference = db.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            reference.update("token", token).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Token Refreshed ");
                    }
                }
            });
        }catch (Exception e){
            Log.d(TAG, "sendRegistrationTokenToServer: User is not signed in Token cannt be refreshed "+e.getMessage());
        }


    }
}
