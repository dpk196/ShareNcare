package com.example.sharencare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sharencare.R;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.example.sharencare.utils.StaticPoolClass;

public class FirstRiderFragment extends Fragment {
    private static final String TAG = "FirstRiderFragment";
     private TextView otpTextView,vehicleNameView,registrationNumberTextView,fareTextView,ridingWithTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
          View view=inflater.inflate(R.layout.rider_view_fragment_on_map_one,container,false);
          otpTextView=view.findViewById(R.id.otp_value_VehicleDetailsForRider);
          vehicleNameView=view.findViewById(R.id.start_trip_VehicleDetailsForRider);
          registrationNumberTextView=view.findViewById(R.id.vehicle_registration_number_VehicleDetailsForRider);
          fareTextView=view.findViewById(R.id.fare_value_rider);
          ridingWithTextView=view.findViewById(R.id.ride_with_VehicleDetailsForRider);
          ridingWithTextView.setText(StaticPoolClass.otherUserDetails.getUsername());
          otpTextView.setText(StaticPoolClass.recevied_otp);
          vehicleNameView.setText(StaticPoolClass.otherUserDetails.getVehicle_name());
          registrationNumberTextView.setText(StaticPoolClass.otherUserDetails.getRegistration_number());


          view.findViewById(R.id.call_him_VehicleDetailsForRider).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Log.d(TAG, "onClick: ");
              }
          });
          view.findViewById(R.id.chat_with_VehicleDetailsForRider).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Log.d(TAG, "onClick: clicked");
                  
              }
          });

          return view;
    }
}
