package com.example.sharencare.Interfaces;

import com.example.sharencare.Models.TripDetail;

import java.util.ArrayList;

public interface SearchForLaterOnTripsInterface {
    public  void matchedLaterOnTrips(ArrayList<String> userIds, ArrayList<TripDetail> trips);
}
