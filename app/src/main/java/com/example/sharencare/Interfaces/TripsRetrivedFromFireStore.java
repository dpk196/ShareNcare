package com.example.sharencare.Interfaces;

import com.example.sharencare.Models.TripDetail;

import java.util.ArrayList;

public interface TripsRetrivedFromFireStore {
    void userTripsCollectionFromFirestore(ArrayList<TripDetail> result);
}
