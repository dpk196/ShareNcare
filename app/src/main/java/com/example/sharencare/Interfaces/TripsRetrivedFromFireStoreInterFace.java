package com.example.sharencare.Interfaces;

import com.example.sharencare.Models.TripDetail;

import java.util.ArrayList;

public interface TripsRetrivedFromFireStoreInterFace {
    void userTripsCollectionFromFirestore(ArrayList<TripDetail> result);
}
