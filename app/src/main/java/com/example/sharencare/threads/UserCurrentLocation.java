package com.example.sharencare.threads;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.sharencare.Interfaces.UserCurrentLocationInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.lang.ref.WeakReference;
import java.util.List;

public class UserCurrentLocation extends AsyncTask<Void,Void , String> {
    private static final String TAG = "UserCurrentLocation";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    Context mContext;
    boolean flagLocality=false;
    boolean flagCoordinates=false;
    private  Location mLocation;
    private WeakReference<UserCurrentLocationInterface> currentLocation;
    String userLocation;
    public UserCurrentLocation(Context mContext,UserCurrentLocationInterface location) {
        this.mContext = mContext;
        currentLocation=new WeakReference<>(location);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    @Override
    protected String  doInBackground(Void... voids) {
        return  getLastKnownLocation();
    }

    private String getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: Getting user last Known Location");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return null;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLocation = task.getResult();
                    Log.d(TAG, "onComplete: Location Coordinates:" + mLocation.toString());
                    Geocoder geocoder=new Geocoder(mContext);
                    try {
                        List<Address>    addresses=geocoder.getFromLocation(mLocation.getLatitude(),mLocation.getLongitude(),1) ;
                        if(addresses.size()>0){
                            flagLocality=true;
                            flagCoordinates=true;
                            userLocation=addresses.get(0).getAddressLine(0);
                            Log.d(TAG, "onComplete: locality:"+addresses.get(0).getLocale());
                        }else {
                            flagLocality=true;
                            flagCoordinates=true;
                            Log.d(TAG, "onComplete: cannot locate the user");
                        }
                    }catch (Exception e){
                        flagLocality=true;
                        Log.d(TAG, "onComplete: "+e.getMessage());
                    }

                    
                } else {
                    flagLocality=flagCoordinates=true;
                    Log.d(TAG, "onComplete: Unable to find User Current Location");

                }
            }
        });
          while(flagCoordinates==false&&flagLocality==false){
              Log.d(TAG, "getLastKnownLocation: Still locting user");
          };
          return  userLocation;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: Called USer Location from onPostExecute:"+s);
       currentLocation.get().userCurrentLocation(s);
    }
}
