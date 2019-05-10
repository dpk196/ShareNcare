package com.example.sharencare.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Interfaces.FCM;
import com.example.sharencare.Models.FCMData;
import com.example.sharencare.Models.FirebaseCloudMessage;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.services.MyFirebaseMessagingService;
import com.example.sharencare.ui.HomeActivity;
import com.example.sharencare.ui.MainActivity;
import com.example.sharencare.ui.RidesFoundShowOnMapForDriver;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sharencare.Adapters.SearchedRidesRecyclerViewAdapter.BASE_URL;


public class FirstDriverFragment extends Fragment {
    private static final String TAG = "FirstDriverFragment";
    private TextView rideToFragmentOne;
    private UserLocation riderLocation = StaticPoolClass.otherUserLocation;
    private User currentUser = StaticPoolClass.currentUserDetails;
    private User riderDetails = StaticPoolClass.otherUserDetails;
    private TripDetail tripDetail = StaticPoolClass.tripDetails;
    public static String otp;
    private String m_Text = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.driver_view_fragment_on_map_one, container, false);
        rideToFragmentOne =view.findViewById(R.id.ride_to_fragment_one_driver);
        rideToFragmentOne.setText(tripDetail.getTrip_destination());
        view.findViewById(R.id.accept_ride_fragment_one_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked on accept_ride_fragment_one_driver");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Do you want to charge for this trip");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Max Chargeable fare Rs"+" "+StaticPoolClass.fare);
                        final EditText input = new EditText(getContext());
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        builder.setView(input);
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!input.getText().toString().equals("")){
                                    String m_fare= input.getText().toString();
                                    if(Double.valueOf(m_fare)<Double.valueOf(StaticPoolClass.fare)){
                                        Random rand = new Random();
                                        otp=String.format("%04d", rand.nextInt(10000));
                                        Log.d(TAG, "onClick: Otp:" + otp);
                                        SendFCMRequest sendFCMRequest = new SendFCMRequest(riderLocation, currentUser,"Tap for Details" , riderDetails.getToken(), "data_type_ride_accepted", otp,currentUser.getUsername()+" "+"has accepted your Request",m_fare);
                                        boolean result = sendFCMRequest.sendRequest();
                                        if (result==true) {
                                            ((RidesFoundShowOnMapForDriver) getActivity()).setViewPager(1, new SecondDriverFragment());
                                            Log.d(TAG, "onClick: Request send successfull");
                                        } else {
                                            Log.d(TAG, "onClick: Something went wrong");
                                        }
                                    }else {
                                        Toast.makeText(getContext(), "Fare Cannot be more than"+" "+StaticPoolClass.fare, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        });
                        builder.show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SendFCMRequest sendFCMRequest = new SendFCMRequest(riderLocation, currentUser,"Tap for Details" , riderDetails.getToken(), "data_type_ride_accepted", otp,currentUser.getUsername()+" "+"has accepted your Request","Free Ride");
                        sendFCMRequest.sendRequest();
                        ((RidesFoundShowOnMapForDriver) getActivity()).setViewPager(1, new SecondDriverFragment());
                        Log.d(TAG, "onClick: Free Ride");
                        Toast.makeText(getContext(), "You are On trip with"+" "+StaticPoolClass.otherUserDetails.getUsername(), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();



            }
        });
        view.findViewById(R.id.reject_ride_fragment_one_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked on reject_ride_fragment_one_driver");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Tell"+" "+StaticPoolClass.otherUserDetails.getUsername()+" "+"the Reason");
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!input.getText().toString().equals("")){
                            m_Text = input.getText().toString();
                            Log.d(TAG, "onClick: "+m_Text);
                            SendFCMRequest sendFCMRequest = new SendFCMRequest(riderLocation, currentUser,m_Text , riderDetails.getToken(), "data_type_ride_rejected", otp,"Car owner says-","");
                            boolean result = sendFCMRequest.sendRequest();
                            if (result==true) {
                                Intent intent=new Intent(getActivity(),HomeActivity.class);
                                startActivity(intent);
                                Log.d(TAG, "onClick: Request send successfull");
                            } else {
                                Log.d(TAG, "onClick: Something went wrong");
                            }
                        }else{
                            Toast.makeText(getContext(), "Cannot be blank", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                builder.show();


            }
        });
        view.findViewById(R.id.ride_details_fragment_one_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked on ride_details_fragment_one_driver");

            }
        });
        return view;
    }

}

