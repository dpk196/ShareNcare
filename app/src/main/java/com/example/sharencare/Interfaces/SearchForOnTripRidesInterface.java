package com.example.sharencare.Interfaces;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.UserLocation;

import java.util.ArrayList;

public interface SearchForOnTripRidesInterface {
    void  matchedOnTripRides(ArrayList<UserLocation> matchedRides,ArrayList<TripDetail> trips);
}
