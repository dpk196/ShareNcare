package com.example.sharencare.Interfaces;

import android.location.Address;
import android.location.Location;

import com.google.firebase.firestore.GeoPoint;

public interface UserCurrentLocationInterface {
    void userCurrentLocation(GeoPoint geoPoint);
}
