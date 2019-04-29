package com.example.sharencare.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.sharencare.R;
import com.example.sharencare.threads.RetriveDetailsFromFireStore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
       findViewById(R.id.rider).setOnClickListener(this);
       findViewById(R.id.driver).setOnClickListener(this);
       findViewById(R.id.sign_out).setOnClickListener(this::onClick);
       setupFirebaseListener();


    }

    @Override
    protected void onResume() {
        super.onResume();
        RetriveDetailsFromFireStore.mLastQuery=null;
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
           case R.id.sign_out:{
               FirebaseAuth.getInstance().signOut();
               break;
           }
       }
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener!=null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }
    private void setupFirebaseListener(){
        Log.d(TAG, "setupFirebaseListener: setting up the auth state listener.");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                }else{
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(HomeActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }
}
