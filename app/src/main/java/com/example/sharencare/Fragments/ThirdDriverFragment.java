package com.example.sharencare.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.ui.MainActivity;
import com.example.sharencare.ui.RidesFoundShowOnMapForDriver;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import static com.example.sharencare.utils.StaticPoolClass.otherUserDetails;

public class ThirdDriverFragment extends Fragment {
    private static final String TAG = "ThirdDriverFragment";
    private TextView ridingWith;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_view_fragment_on_map_three, container, false);
        ridingWith=view.findViewById(R.id.ride_with_fragment_three_driver);
        ridingWith.setText(StaticPoolClass.otherUserDetails.getUsername());
        view.findViewById(R.id.navigate_trip_three_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Click on the Destination Marker to start Navigation")
                        .setCancelable(true);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }

        });
        view.findViewById(R.id.end_trip_three_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendFCMRequest sendFCMRequest = new SendFCMRequest("Tap on this notification to Complete", StaticPoolClass.otherUserDetails.getToken(), "data_type_ride_completed", "","Trip completed with "+StaticPoolClass.otherUserDetails.getUsername(),"");
                sendFCMRequest.sendRequest();
                submitTripDetailsForRider();
                submitEndTripDetailsForDriver("Trip Completed with "+StaticPoolClass.otherUserDetails.getUsername());

            }
        });
        view.findViewById(R.id.sos_fragment_three_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri u = Uri.parse("tel:" +"100");
                Intent i = new Intent(Intent.ACTION_DIAL, u);
                try{
                    startActivity(i);
                }catch (SecurityException e){
                    Log.d(TAG, "onClick: Exception"+e.getMessage());
                }
            }
        });
        return view;
    }

    private void submitEndTripDetailsForDriver(String s) {
        StaticPoolClass.tripDetails.setStatus(s);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection(getString(R.string.collection_trips)).document(StaticPoolClass.tripDetails.getTripId());
        reference.update("status",s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Details submitted successfully");
                }
            }
        });
    }
    private void submitTripDetailsForRider(){
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);
        CollectionReference tripCollectionReference = mDb.collection("collection_trips");
        Query tripsQuery = tripCollectionReference.whereEqualTo("status", "On Trip with"+" "+StaticPoolClass.currentUserDetails.getUsername()).whereEqualTo("user_id", otherUserDetails.getUser_id());
        tripsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    TripDetail trip = documentSnapshot.toObject(TripDetail.class);
                    Log.d(TAG, "onComplete: Rider Trip Details:"+trip.toString());
                    String trip_id=trip.getTripId();
                    String status="Trip Completed  with"+" "+StaticPoolClass.currentUserDetails.getUsername();
                    submit(trip_id,status);
                }
            }
        });
    }

    private void submit(String trip_id, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection(getString(R.string.collection_trips)).document(trip_id);
        reference.update("status",status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Details submitted successfully");
                    startActivity(new Intent(getContext(), MainActivity.class));
                }
            }
        });
    }

}
