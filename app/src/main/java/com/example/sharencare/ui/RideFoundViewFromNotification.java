package com.example.sharencare.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.sharencare.R;

import static com.example.sharencare.services.MyFirebaseMessagingService.tripFromMessagingService;
import static com.example.sharencare.services.MyFirebaseMessagingService.userLocationFromMessagingService;
import static com.example.sharencare.services.MyFirebaseMessagingService.userRiderDetailsFromMessagingService;

public class RideFoundViewFromNotification extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RideFoundViewFromNotifi";
      Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_found_view_from_notification);
        findViewById(R.id.view_onMap) .setOnClickListener(this);
        findViewById(R.id.reject_trip).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.view_onMap :{
                Intent intent=new Intent(RideFoundViewFromNotification.this,RidesFoundShowOnMap.class);
                startActivity(intent);
                break;
            }
            case R.id.reject_trip :{
                break;
            }
        }

    }
}
