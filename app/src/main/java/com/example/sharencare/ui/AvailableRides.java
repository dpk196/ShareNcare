package com.example.sharencare.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.sharencare.Adapters.SearchedRidesRecyclerViewAdapter;
import com.example.sharencare.Interfaces.FCMTokenInterface;
import com.example.sharencare.Models.UserLocation;
import com.example.sharencare.R;
import com.example.sharencare.threads.GetMatchedFCMToken;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class AvailableRides extends AppCompatActivity implements FCMTokenInterface {
    private static final String TAG = "AvailableRides";
    ArrayList<String> ridesWithoutDuplicates = new ArrayList<>();
    private ArrayList<String> tokenList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_rides);
        Bundle bundle = getIntent().getExtras();
        ArrayList<String> matchedRides = bundle.getStringArrayList("matchedRides");

        if (matchedRides.size() > 0) {

            for (String userid : matchedRides) {
                Log.d(TAG, "onCreate:Userid :" + userid);
            }
            initSearchedRidesRecyclerViewAdapter(matchedRides);
            matchedRides.clear();
        } else {
            Log.d(TAG, "onCreate: No rides Found");
        }

    }

    private void initSearchedRidesRecyclerViewAdapter(ArrayList<String> rides) {
        ridesWithoutDuplicates.clear();
        Log.d(TAG, "initSearchedRidesRecyclerViewAdapter: Called");
        for (String id : rides) {
            ridesWithoutDuplicates.add(id);
        }
        Log.d(TAG, "initSearchedRidesRecyclerViewAdapter:ridesWithoutDuplicates size: " + ridesWithoutDuplicates.size());
        Log.d(TAG, "initSearchedRidesRecyclerViewAdapter:rides size: " + rides.size());
        RecyclerView searchedRidesRecyclerView = findViewById(R.id.available_rides_recycleview_adapter);
        SearchedRidesRecyclerViewAdapter adapter = new SearchedRidesRecyclerViewAdapter(ridesWithoutDuplicates, tokenList, this);
        searchedRidesRecyclerView.setAdapter(adapter);
        searchedRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        initTokenThread(ridesWithoutDuplicates);
        rides.clear();
    }

    private void initTokenThread(ArrayList<String> userIds) {
        Log.d(TAG, "initTokenThread: Called");
        GetMatchedFCMToken getMatchedFCMToken = new GetMatchedFCMToken(userIds, this, this);
        getMatchedFCMToken.execute();
    }


    @Override
    public void getToken(ArrayList<String> tokens) {
        Log.d(TAG, "getToken: Token Recived");
        for (String token : tokens) {
            tokenList.add(token);
            Log.d(TAG, "Token: " + token);
        }
    }
}
