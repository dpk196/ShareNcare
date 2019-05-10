package com.example.sharencare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.example.sharencare.ui.MainActivity;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;

public class FourthDriverFragment extends Fragment {
    private UserLocation riderLocation = StaticPoolClass.otherUserLocation;
    private User currentUser = StaticPoolClass.currentUserDetails;
    private User riderDetails = StaticPoolClass.otherUserDetails;
    private TripDetail tripDetail = StaticPoolClass.tripDetails;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.driver_view_fragment_on_map_four,container,false);
        TextView text=view.findViewById(R.id.input_text);

        view.findViewById(R.id.submit_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!text.getText().toString().equals("")) {
                    String message = text.getText().toString();
                    SendFCMRequest sendFCMRequest = new SendFCMRequest(riderLocation, currentUser, message, riderDetails.getToken(), "data_type_ride_rejected", "","Sorry","");
                    sendFCMRequest.sendRequest();
                }else{
                    Toast.makeText(getContext(), "Cannot be left blank", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return  view;
    }
}
