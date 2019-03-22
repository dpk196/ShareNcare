package com.example.sharencare.ui;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.sharencare.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiderActivity extends AppCompatActivity {
    private static final String TAG = "RiderActivity";
    String destinationText;
    String sourceText;
    LatLng sourceLatlng;
    LatLng destinationLatlng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
    }
    ////.......Places Prediction  from.................
    private void placesPredictionFrom() {
        Log.d(TAG, "placesPrediction: Called");
        if(!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }
        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_from);
        autocompleteFragment.isHidden();
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(22.5825856, 88.4452336),
                new LatLng(22.7580747,88.7024556));
        autocompleteFragment.setLocationBias(bounds);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Get info about the selected place.
                Address address=geoLocate(place.getName());
                if(address!=null) {
                    Log.i(TAG, "Place Source:" + place.getName());
                    sourceText=place.getName();
                    sourceLatlng = new LatLng(address.getLatitude(), address.getLongitude());
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    //.....Place Prediction To.......

    private void placesPredictionTo() {
        Log.d(TAG, "placesPrediction: Called");
        if(!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }
        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_to);

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(22.5825856, 88.4452336),
                new LatLng(22.7580747,88.7024556));
        autocompleteFragment.setLocationBias(bounds);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Get info about the selected place.
                Address address=geoLocate(place.getName());
                if(address!=null) {
                    Log.i(TAG, "Place Destination:" + place.getName());
                    destinationText=place.getName();
                    destinationLatlng=new LatLng(address.getLatitude(), address.getLongitude());

                }


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
    private Address geoLocate(String name) {
        Address address=null;
        Log.d(TAG, "geoLocate: Getting the latitude and Longitude of the Source and Destination for:"+name);
        Geocoder geocoder= new Geocoder(RiderActivity.this);
        List<Address> addressList= new ArrayList<>();
        try{
            Log.d(TAG, "geoLocate: inside try");
            addressList= geocoder.getFromLocationName(name,1);
        }catch (IOException e){
            Log.d(TAG, "geoLocate: "+e.getMessage());
        }
        if(addressList.size()>0){
            Log.d(TAG, "geoLocate: inside size>0");
            address =addressList.get(0);
            if(address.hasLatitude()&&address.hasLongitude()){

                Log.d(TAG, "geoLocate: lat:"+address.getLatitude());
                Log.d(TAG, "geoLocate: long:"+address.getLongitude());
            }
            else {
                Toast.makeText(this, "cant go that location", Toast.LENGTH_SHORT).show();
            }
        }
        return  address;
    }
    private void searchForRides(){


    }
}
