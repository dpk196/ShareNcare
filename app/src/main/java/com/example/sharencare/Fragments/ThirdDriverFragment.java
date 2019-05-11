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

import com.example.sharencare.R;
import com.example.sharencare.ui.RidesFoundShowOnMapForDriver;
import com.example.sharencare.utils.StaticPoolClass;

import org.w3c.dom.Text;

public class ThirdDriverFragment extends Fragment {
    private static final String TAG = "ThirdDriverFragment";
    private TextView ridingWith;
    private TextView tripToTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_view_fragment_on_map_three, container, false);
        ridingWith=view.findViewById(R.id.ride_with_fragment_three_driver);
        ridingWith.setText(StaticPoolClass.otherUserDetails.getUsername());
        tripToTextView=view.findViewById(R.id.otp_value_fragment_three_driver);
        tripToTextView.setText(StaticPoolClass.tripDetails.getTrip_destination());
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
}
