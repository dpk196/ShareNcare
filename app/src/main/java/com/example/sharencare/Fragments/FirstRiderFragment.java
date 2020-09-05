package com.example.sharencare.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.R;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;

public class FirstRiderFragment extends Fragment {
    private static final String TAG = "FirstRiderFragment";
    private TextView otpTextView, vehicleNameView, registrationNumberTextView, fareTextView, ridingWithTextView;
    private String m_Text = "";

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rider_view_fragment_on_map_one, container, false);
        otpTextView = view.findViewById(R.id.otp_value_VehicleDetailsForRider);
        vehicleNameView = view.findViewById(R.id.start_trip_VehicleDetailsForRider);
        registrationNumberTextView = view.findViewById(R.id.vehicle_registration_number_VehicleDetailsForRider);
        fareTextView = view.findViewById(R.id.fare_value_rider);
        ridingWithTextView = view.findViewById(R.id.ride_with_VehicleDetailsForRider);
        ridingWithTextView.setText(StaticPoolClass.otherUserDetails.getUsername());
        otpTextView.setText(StaticPoolClass.recevied_otp);
        vehicleNameView.setText(StaticPoolClass.otherUserDetails.getVehicle_name());
        registrationNumberTextView.setText(StaticPoolClass.otherUserDetails.getRegistration_number());
        fareTextView.setText(StaticPoolClass.fare);
        view.findViewById(R.id.call_him_VehicleDetailsForRider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                Uri u = Uri.parse("tel:" + StaticPoolClass.otherUserDetails.getMobile_number());
                Intent i = new Intent(Intent.ACTION_DIAL, u);
                try {
                    startActivity(i);
                } catch (SecurityException e) {
                    Log.d(TAG, "onClick: Exception" + e.getMessage());
                }
            }
        });
        view.findViewById(R.id.chat_with_VehicleDetailsForRider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Chat with" + " " + StaticPoolClass.otherUserDetails.getUsername());
                final EditText input = new EditText(getContext());
                builder.setView(input);
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().equals("")) {
                            m_Text = input.getText().toString();
                            SendFCMRequest sendFCMRequest = new SendFCMRequest(m_Text, StaticPoolClass.otherUserDetails.getToken(), "chat_message", "", StaticPoolClass.currentUserDetails.getUsername() + " " + "says", "");
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
}
