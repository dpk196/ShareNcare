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
import android.view.View;

import com.example.sharencare.Interfaces.DirectionsResultInterface;
import com.example.sharencare.Interfaces.TripDetailsOfOnTripMatchedTripInterface;
import com.example.sharencare.Interfaces.UserCurrentLocationFromFirestoreInterface;
import com.example.sharencare.Interfaces.UserDetailsOfMatchedTripInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.threads.DirectionsThreads;
import com.example.sharencare.threads.TripDetailsOfOnTripMatchedTrip;
import com.example.sharencare.threads.UserCurrentLocationFromFireStore;
import com.example.sharencare.threads.UserDetailsOfMatchedTrip;
import com.example.sharencare.ui.DriveFoundShowOnMapForRider;
import com.example.sharencare.ui.MainActivity;
import com.example.sharencare.ui.RidesFoundShowOnMapForDriver;
import com.example.sharencare.ui.ScheduleDriveViewDriver;
import com.example.sharencare.ui.ScheduleDriveViewRider;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;

import static com.example.sharencare.utils.StaticPoolClass.currentUserDetails;
import static com.example.sharencare.utils.StaticPoolClass.currentUserLocation;
import static com.example.sharencare.utils.StaticPoolClass.otherUserDetails;
import static com.example.sharencare.utils.StaticPoolClass.otherUserLocation;
import static com.example.sharencare.utils.StaticPoolClass.recevied_otp;
import static com.example.sharencare.utils.StaticPoolClass.rideAcceptedFlag;
import static com.example.sharencare.utils.StaticPoolClass.tripDetails;
import static com.example.sharencare.utils.NotifactionChannel.CHANNEL_Id;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements UserDetailsOfMatchedTripInterface,
        TripDetailsOfOnTripMatchedTripInterface, UserCurrentLocationFromFirestoreInterface, DirectionsResultInterface {
    private static final String TAG = "MyFirebaseMessagingServ";
    private static final int BROADCAST_NOTIFICATION_ID = 1;
    private String notificationData = "";
    private String title = "";
    private String fromUserId = "";
    private String myUserId = "";
    private String data_type;
    private String message;
    private Intent notifyIntent;
    private boolean flag = false;
    private boolean currentUserDetailsFlag = false, otherUserDetailsFlag = false, otherUserlocationFlag = false, currentUserlocationFlag = false;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            notificationData = remoteMessage.getData().toString();
            Log.d(TAG, "onMessageReceived: " + notificationData);
            data_type = remoteMessage.getData().get(getString(R.string.data_type));
            title = remoteMessage.getData().get(getString(R.string.data_title));
            fromUserId = remoteMessage.getData().get(getString(R.string.fromUserId));
            myUserId = remoteMessage.getData().get(getString(R.string.toUserId));
            message = remoteMessage.getData().get("message");
            if (data_type.equals("data_type_ride_request")) {
                StaticPoolClass.fare = remoteMessage.getData().get("fare");
                Log.d(TAG, "onMessageReceived: lat :" + remoteMessage.getData().get("dlat"));
                StaticPoolClass.tripDestinationLatLng = new LatLng(Double.parseDouble(remoteMessage.getData().get("dlat")), Double.parseDouble(remoteMessage.getData().get("dlng")));
                Log.d(TAG, "onMessageReceived: Destination Coordinates:" + StaticPoolClass.tripDestinationLatLng.toString());
                StaticPoolClass.fare = remoteMessage.getData().get("fare");
                notifyIntent = new Intent(this, RidesFoundShowOnMapForDriver.class);
                Log.d(TAG, "onMessageReceived: A rider Request");
                Log.d(TAG, "onMessageReceived: A rider Request:" + notifyIntent.toString());
                initThreadsDriver(fromUserId, myUserId);
            }
            if (data_type.equals("data_type_ride_accepted")) {
                flag = true;
                StaticPoolClass.tripDestinationLatLng = new LatLng(Double.parseDouble(remoteMessage.getData().get("dlat")), Double.parseDouble(remoteMessage.getData().get("dlng")));
                StaticPoolClass.fare = remoteMessage.getData().get("fare");
                Log.d(TAG, "onMessageReceived: " + StaticPoolClass.fare);
                notifyIntent = new Intent(this, DriveFoundShowOnMapForRider.class);
                Log.d(TAG, "onMessageReceived: Trip accepted");
                recevied_otp = remoteMessage.getData().get(getString(R.string.otp));
                initThreadsRider(fromUserId, myUserId);
                Log.d(TAG, "onMessageReceived: otp:" + recevied_otp);
                notifyIntent = new Intent(this, DriveFoundShowOnMapForRider.class);
                Log.d(TAG, "onMessageReceived:A driver Ride accepted " + notifyIntent.toString());
            }
            if (data_type.equals("data_type_ride_rejected")) {
                Log.d(TAG, "onMessageReceived: Trip Rejected");
                sendNoIntentBroadcastNotification(title, message);

            }
            if (data_type.equals("data_type_ride_started")) {
                Log.d(TAG, "onMessageReceived: data_type_ride_started");
                sendNoIntentBroadcastNotification(title, message);
            }
            if (data_type.equals("chat_message")) {
                Log.d(TAG, "onMessageReceived: Chat message");
                sendNoIntentBroadcastNotification(title, message);
            }
            if (data_type.equals("data_type_ride_completed")) {
                Log.d(TAG, "onMessageReceived: Trip Completed");
                notifyIntent = new Intent(this, MainActivity.class);
                sendBroadcastNotification(title, message);
            }
            if (data_type.equals("data_type_schedule_trip")) {
                notifyIntent = new Intent(getApplicationContext(), ScheduleDriveViewDriver.class);
                UserDetailsOfMatchedTrip otherUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(fromUserId, this, this);
                otherUserDetailsOfMatchedTrip.execute();
                String trip_id = remoteMessage.getData().get("trip_id");
                getTripFromFireStore(trip_id);
            }
            if(data_type.equals("allowed_to_show_trip_details")){
                UserDetailsOfMatchedTrip otherUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(fromUserId, this, this);
                otherUserDetailsOfMatchedTrip.execute();
                notifyIntent = new Intent(getApplicationContext(), ScheduleDriveViewRider.class);
                String trip_id = remoteMessage.getData().get("trip_id");
                Log.d(TAG, "onMessageReceived: "+trip_id);
                getTripFromFireStore(trip_id);
            }
            if(data_type.contains("allowed_to_show_trip_details_rejected")){
                Log.d(TAG, "onMessageReceived: Trip Rejected");
                sendNoIntentBroadcastNotification(title,message);
            }
            if(data_type.equals("allowed_to_show_trip_details_accepted_driver")){
                UserDetailsOfMatchedTrip otherUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(fromUserId, this, this);
                otherUserDetailsOfMatchedTrip.execute();
                Log.d(TAG, "onMessageReceived: Trip accepted by Driver");
                sendNoIntentBroadcastNotification(title,message);
            }
            if(data_type.equals("allowed_to_show_trip_details_accepted_rider")){
                UserDetailsOfMatchedTrip otherUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(fromUserId, this, this);
                otherUserDetailsOfMatchedTrip.execute();
                Log.d(TAG, "onMessageReceived: Trip accepted by rider");
                StaticPoolClass.acceptSchuledRideFlag=true;
                sendNoIntentBroadcastNotification(title,message);

            }
        } catch (NullPointerException e) {
            Log.d(TAG, "onMessageReceived: Null pointer" + e.getMessage());
        }
        Log.d(TAG, "onMessageReceived: Data:" + notificationData);

    }

    private void getTripFromFireStore(String trip_id) {
        Log.d(TAG, "getTripFromFireStore: called:" + trip_id);
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference = mDb.collection("collection_trips");
        Query tripsQuery = tripCollectionReference.whereEqualTo("tripId", trip_id);
        tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    TripDetail trip = documentSnapshot.toObject(TripDetail.class);
                    Log.d(TAG, "onComplete: Rider Trip Details:" + trip.toString());
                    StaticPoolClass.tripDetailsForScheduleRide = trip;
                    sendBroadcastNotification(title, message);

                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendBroadcastNotification(String title, String message) {
        Log.d(TAG, "sendBroadcastNotification: building a  notification");


        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_Id);
        // Creates an Intent for the Activity


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

    private void initThreadsDriver(String fromUserID, String myUserID) {
        Log.d(TAG, "initThreads: called");
        UserDetailsOfMatchedTrip otherUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(fromUserID, this, this);
        otherUserDetailsOfMatchedTrip.execute();
        //...............
        UserDetailsOfMatchedTrip currentUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(myUserID, this, this);
        currentUserDetailsOfMatchedTrip.execute();
        //...............
        UserCurrentLocationFromFireStore otherUserlocation = new UserCurrentLocationFromFireStore(fromUserID, this, this);
        otherUserlocation.execute();
        //.........
        UserCurrentLocationFromFireStore currentUserlocation = new UserCurrentLocationFromFireStore(myUserID, this, this);
        currentUserlocation.execute();
        //.....
        while (currentUserDetailsFlag != true && otherUserDetailsFlag != true && otherUserlocationFlag != true && currentUserlocationFlag != true) {
            Log.d(TAG, "initThreads: Getting otherUserDetailsOfMatchedTrip currentUserDetailsOfMatchedTrip otherUserlocation currentUserlocation ");
        }
        TripDetailsOfOnTripMatchedTrip tripDetailsOfOnTripMatchedTrip = new TripDetailsOfOnTripMatchedTrip(myUserID, this, this);
        tripDetailsOfOnTripMatchedTrip.execute();
    }

    private void initThreadsRider(String fromUserID, String myUserID) {
        Log.d(TAG, "initThreads: called");
        UserDetailsOfMatchedTrip otherUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(fromUserID, this, this);
        otherUserDetailsOfMatchedTrip.execute();
        //...............
        UserDetailsOfMatchedTrip currentUserDetailsOfMatchedTrip = new UserDetailsOfMatchedTrip(myUserID, this, this);
        currentUserDetailsOfMatchedTrip.execute();
        //...............
        UserCurrentLocationFromFireStore otherUserlocation = new UserCurrentLocationFromFireStore(fromUserID, this, this);
        otherUserlocation.execute();
        //.........
        UserCurrentLocationFromFireStore currentUserlocation = new UserCurrentLocationFromFireStore(myUserID, this, this);
        currentUserlocation.execute();
        //.....
        while (currentUserDetailsFlag != true && otherUserDetailsFlag != true && otherUserlocationFlag != true && currentUserlocationFlag != true) {
            Log.d(TAG, "initThreads: Getting otherUserDetailsOfMatchedTrip currentUserDetailsOfMatchedTrip otherUserlocation currentUserlocation ");
        }
        TripDetailsOfOnTripMatchedTrip tripDetailsOfOnTripMatchedTrip = new TripDetailsOfOnTripMatchedTrip(fromUserID, this, this);
        tripDetailsOfOnTripMatchedTrip.execute();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendNoIntentBroadcastNotification(String title, String message) {

        Log.d(TAG, "sendBroadcastNotification: building a  notification");
        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_Id);
        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent(this, RidesFoundShowOnMapForDriver.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0, new Intent(),
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void userDetailsReceived(User user) {
        Log.d(TAG, "userDetailsReceived: User Details Received of Rider:" + user.getUsername());
        if (user.getUser_id().equals(fromUserId)) {
            otherUserDetails = user;
            otherUserDetailsFlag = true;
        }
        if (user.getUser_id().equals(myUserId)) {
            currentUserDetails = user;
            currentUserlocationFlag = true;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void getOnTripDetail(TripDetail tripDetail) {
        Log.d(TAG, "getOnTripDetail: Called");
        if (tripDetail != null) {
            tripDetails = tripDetail;
            Log.d(TAG, "getOnTripDetail: Received:" + tripDetails.toString());
            sendBroadcastNotification(title, message);
            DirectionsThreads directionsThreads = new DirectionsThreads(tripDetails.getTrip_source() + " " + "Kolkata", tripDetails.getTrip_destination() + " " + "Kolkata", getApplicationContext(), this);
            directionsThreads.execute();
            if (flag == true) {
                submitDetailsToFireStore(tripDetails);
            }
        } else {
            Log.d(TAG, "getOnTripDetail: Something went wrong");
        }
    }

    private void submitDetailsToFireStore(TripDetail tripDetails) {
        tripDetails.setUser_id(currentUserDetails.getUser_id());
        tripDetails.setStatus("On Trip with" + " " + otherUserDetails.getUsername());
        Log.d(TAG, "submitDetailsToFireStore: Before send to fireStore" + tripDetails.toString());
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        DocumentReference newTripRef = mDb.collection(getString(R.string.collection_trips)).document();
        tripDetails.setTripId(newTripRef.getId());
        newTripRef.set(tripDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: successfully submitted");
            }
        });

    }

    @Override
    public void userCurrentLocation(UserLocation location) {
        Log.d(TAG, "userCurrentLocation: Called ");
        if (location.getUser_id().equals(fromUserId)) {
            otherUserLocation = location;
            otherUserlocationFlag = true;
            Log.d(TAG, "userCurrentLocation: OtherUserLocation" + location.toString());
        }
        if (location.getUser_id().equals(myUserId)) {
            currentUserLocation = location;
            currentUserlocationFlag = true;
            Log.d(TAG, "userCurrentLocation: CurrentUserLocation:" + location.toString());
        }


    }

    //.............................................................................
    @Override
    public void onNewToken(String s) {
        sendRegistrationTokenToServer(s);
    }

    private void sendRegistrationTokenToServer(String token) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        } catch (Exception e) {
            Log.d(TAG, "sendRegistrationTokenToServer: User is not signed in Token cannt be refreshed " + e.getMessage());
        }


    }


    @Override
    public void onCreate() {
        super.onCreate();
        rideAcceptedFlag = false;

    }


    @Override
    public void onDirectionsRetrived(DirectionsResult result) {
        Log.d(TAG, "onDirectionsRetrived: Called");
        StaticPoolClass.directionsResultDriver = result;
    }
}
