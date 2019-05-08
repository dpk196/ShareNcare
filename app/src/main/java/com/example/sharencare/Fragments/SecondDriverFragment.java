package com.example.sharencare.Fragments;

import android.content.Context;
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

import com.example.sharencare.Models.User;
import com.example.sharencare.R;
import com.example.sharencare.ui.MainActivity;
import com.example.sharencare.ui.RidesFoundShowOnMapForDriver;
import com.google.firebase.messaging.FirebaseMessagingService;

public class SecondDriverFragment extends Fragment {
    private static final String TAG = "SecondDriverFragment";
    private TextView otp;
    private User user= MainActivity.currentUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_view_fragment_on_map_two, container, false);

        Log.d(TAG, "onCreateView: Created");
        otp=view.findViewById(R.id.otp_value_fragment_two_driver);
        Log.d(TAG, "onCreateView: "+FirstDriverFragment.otp);
        otp.setText(FirstDriverFragment.otp);
        view.findViewById(R.id.start_trip_fragment_two_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RidesFoundShowOnMapForDriver)getActivity()).setViewPager(2,new ThirdDriverFragment());

            }
        });
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }
}
