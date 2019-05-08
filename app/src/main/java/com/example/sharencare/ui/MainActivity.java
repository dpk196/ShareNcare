package com.example.sharencare.ui;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sharencare.Interfaces.UserDetailsOfMatchedTripInterface;
import com.example.sharencare.Models.ServerKey;
import com.example.sharencare.Models.User;
import com.example.sharencare.R;
import com.example.sharencare.threads.UserDetailsOfMatchedTrip;
import com.example.sharencare.utils.StaticPoolClass;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import static com.example.sharencare.ui.HomeActivity.user_name;
import static com.example.sharencare.utils.StaticPoolClass.serverKey;

public class MainActivity extends AppCompatActivity implements UserDetailsOfMatchedTripInterface {
    private ProgressBar mProgressBar;
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    public static User currentUser;
    boolean mLocationPermissionGranted=false;
    private static  final int PERMISSIONS_REQUEST_ENABLE_GPS=991;
    private static  final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=992;
    private static  final int ERROR_DIALOG_REQUEST=993;
    String token="" ;
    public  static String  server_key="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.main_progressBar);
        showDialog();
        mAuth=FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        getServerKey();

    }

    private void getCurrrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            if (!user.getUid().equals("")) {
                UserDetailsOfMatchedTrip details =new UserDetailsOfMatchedTrip(FirebaseAuth.getInstance().getCurrentUser().getUid(),this,this);
                details.execute();
            }
        }else{
            navigateToNextActivity();
        }

    }


    private  void navigateToNextActivity(){
        Log.d(TAG, "navigateToNextActivity: Called");
        final FirebaseUser user = mAuth.getCurrentUser();

        try{
            if(!user.getUid().equals("")){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i=new Intent(MainActivity.this,HomeActivity.class);
                        i.putExtra(getString(R.string.user_obj),currentUser);
                        hideDialog();
                        startActivity(i);
                        finish();
                    }
                }, 500);

            }
        } catch (Exception e){
            Log.d(TAG, "onCreate: "+e.getMessage());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                    Log.d(TAG, "onCreate: Redirecting to Register Activity");
                    hideDialog();
                    startActivity(intent);
                    finish();
                }
            }, 4000);
        }
    }

    private void showDialog(){mProgressBar.setVisibility(View.VISIBLE);}
    private void hideDialog(){if(mProgressBar.getVisibility()==View.VISIBLE){mProgressBar.setVisibility(View.INVISIBLE);}}

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getCurrrentUser();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                   getCurrrentUser();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
               getCurrrentUser();
            }else
                getLocationPermission();
        }
    }

    public   void getToken(){
        Log.d(TAG, "getToken: called");
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(task.isSuccessful()){
                        String token=task.getResult().getToken();
                        currentUser.setToken(token);
                        Log.d(TAG, "onComplete: User Token: "+currentUser.getToken());
                        sendResgistrationTokenToServer(token);
                    }
                }
            });
    }
    private void sendResgistrationTokenToServer(String token){
     try {
         FirebaseFirestore db = FirebaseFirestore.getInstance();
         DocumentReference reference = db.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());

         reference.update("token", token).addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {
                 if (task.isSuccessful()) {
                     Log.d(TAG, "onComplete: Token Send to FireStore ");
                 }
             }
         });
     }catch (Exception e){
         Log.d(TAG, "sendResgistrationTokenToServer: Error "+e.getMessage());
     }
    }
    private  void getServerKey(){
        Log.d(TAG, "getServerKey: Called");
       FirebaseFirestore db= FirebaseFirestore.getInstance();
       DocumentReference ref=db.collection("server").document("key");
       ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if(task.isSuccessful()){
                  ServerKey key=task.getResult().toObject(ServerKey.class);
                  serverKey=key.getKey();
                  Log.d(TAG, "onComplete: Server Key="+serverKey);
              }
           }
       });

    }
    @Override
    public void userDetailsReceived(User user) {
        if(user!=null){
            Log.d(TAG, "userDetailsReceived: Current User:"+user);
            currentUser=user;
            getToken();
            Log.d(TAG, "userDetailsReceived: Navigating to Next Activity");
            navigateToNextActivity();
        }else{
            Log.d(TAG, "userDetailsReceived: No registered user");
            Toast.makeText(this, "Please Reinstall the application", Toast.LENGTH_LONG).show();
        }

    }
}