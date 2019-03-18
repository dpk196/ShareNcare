package com.example.sharencare.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.sharencare.R;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
       findViewById(R.id.rider).setOnClickListener(this);
       findViewById(R.id.driver).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.driver:{
               startActivity(new Intent(HomeActivity.this, RiderActivity.class));
               Log.d(TAG, "onClick: directing to Drivers Activity");
               break;
           }
           case R.id.rider:{
               startActivity(new Intent(HomeActivity.this, DriverActivity.class));
               Log.d(TAG, "onClick: directing to Riders Activity");
               break;
           }
       }
    }
}
