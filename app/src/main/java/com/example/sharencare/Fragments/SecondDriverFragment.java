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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sharencare.Models.User;
import com.example.sharencare.R;
import com.example.sharencare.ui.MainActivity;
import com.example.sharencare.ui.RidesFoundShowOnMapForDriver;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessagingService;

public class SecondDriverFragment extends Fragment {
    private static final String TAG = "SecondDriverFragment";
    private TextView otp,ridingWith;
    private User user= MainActivity.currentUser;
    private String  m_Text="";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_view_fragment_on_map_two, container, false);

        Log.d(TAG, "onCreateView: Created");
        otp=view.findViewById(R.id.otp_value_fragment_two_driver);
        ridingWith=view.findViewById(R.id.ride_with_fragment_two_driver);
        ridingWith.setText(StaticPoolClass.otherUserDetails.getUsername());
        Log.d(TAG, "onCreateView: "+FirstDriverFragment.otp);
        otp.setText(FirstDriverFragment.otp);
        view.findViewById(R.id.start_trip_fragment_two_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              ((RidesFoundShowOnMapForDriver)getActivity()).setViewPager(2,new ThirdDriverFragment());
                SendFCMRequest sendFCMRequest = new SendFCMRequest("Have a Safe Journey", StaticPoolClass.otherUserDetails.getToken(), "data_type_ride_started", "","Trip has Started","");
                sendFCMRequest.sendRequest();
                changeStatusOfTheTrip();

            }

        });
        view.findViewById(R.id.call_him_fragment_two_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri u = Uri.parse("tel:" +StaticPoolClass.otherUserDetails.getMobile_number());
                Intent i = new Intent(Intent.ACTION_DIAL, u);
                try{
                    startActivity(i);
                }catch (SecurityException e){
                    Log.d(TAG, "onClick: Exception"+e.getMessage());
                }
            }
        });
        view.findViewById(R.id.chat_fragment_two_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Chat with"+" "+StaticPoolClass.otherUserDetails.getUsername());
                final EditText input = new EditText(getContext());
                builder.setView(input);
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().equals("")) {
                            m_Text = input.getText().toString();
                            SendFCMRequest sendFCMRequest = new SendFCMRequest(m_Text, StaticPoolClass.otherUserDetails.getToken(), "chat_message", "",StaticPoolClass.currentUserDetails.getUsername()+" "+"says","");
                            sendFCMRequest.sendRequest();
                            Toast.makeText(getContext(), "message send", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();

            }

        });



        return view;
    }
    private void changeStatusOfTheTrip(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String status="On Trip with"+" "+StaticPoolClass.otherUserDetails.getUsername();
        DocumentReference reference = db.collection(getString(R.string.collection_trips)).document(StaticPoolClass.tripDetails.getTripId());
         reference.update("status",status).addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {
                 if(task.isSuccessful()){
                     Log.d(TAG, "onComplete: Details submitted successfully");
                 }
             }
         });
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }
}
