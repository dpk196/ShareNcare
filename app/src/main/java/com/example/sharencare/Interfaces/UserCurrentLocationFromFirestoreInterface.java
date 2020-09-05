package com.example.sharencare.Interfaces;

import android.location.Address;
import android.location.Location;

import com.example.sharencare.Models.UserLocation;
import com.google.firebase.firestore.GeoPoint;

public interface UserCurrentLocationFromFirestoreInterface {
    void userCurrentLocation(UserLocation location);
}
