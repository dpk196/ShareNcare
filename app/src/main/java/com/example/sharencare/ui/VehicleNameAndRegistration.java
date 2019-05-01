package com.example.sharencare.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Interfaces.RegistrationAndVehicleNameInterFace;
import com.example.sharencare.Interfaces.UserDetailsOfMatchedTripInterface;
import com.example.sharencare.Models.User;
import com.example.sharencare.R;
import com.example.sharencare.threads.UpdateRegistrationNumberAndVehicleName;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class VehicleNameAndRegistration extends AppCompatActivity implements View.OnClickListener , UserDetailsOfMatchedTripInterface, RegistrationAndVehicleNameInterFace {
    private TextView vehicle_registration, vehicle_name;
    private static final String TAG = "VehicleNameAndRegistrat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_name_and_registration);
        vehicle_name=findViewById(R.id.vehicle_name_activity_vehicle_name_and_registration);
        vehicle_registration=findViewById(R.id.registration_number_activity_vehicle_name_and_registration);
        findViewById(R.id.update_vehicle_name_registration_activity_vehicle_name_and_registration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!vehicle_name.getText().toString().equals("")&&!vehicle_registration.getText().toString().equals(""))
                update(vehicle_name.getText().toString(),vehicle_registration.getText().toString());
                else{
                    Toast.makeText(VehicleNameAndRegistration.this, "Fields cannot be left blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void update(String vehicle, String registration) {
        UpdateRegistrationNumberAndVehicleName numberAndVehicleName=new UpdateRegistrationNumberAndVehicleName(FirebaseAuth.getInstance().getCurrentUser().getUid(),vehicle,registration,this);
        numberAndVehicleName.execute();
        Intent intent=new Intent(this,DriverActivity.class);
        startActivity(intent);
        finish();

    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void userDetailsReceived(User user) {
        Log.d(TAG, "userDetailsReceived: current user details updated");
        MainActivity.currentUser=user;
    }

    @Override
    public void registrationNumberAndVehicleNameUpdated(User user) {
        Log.d(TAG, "registrationNumberAndVehicleNameUpdated: Updated UserObject Received");
        MainActivity.currentUser=user;

    }
}
