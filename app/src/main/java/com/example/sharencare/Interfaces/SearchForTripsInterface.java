package com.example.sharencare.Interfaces;

import com.example.sharencare.Models.TripDetail;

import java.util.ArrayList;

public interface SearchForTripsInterface  {
    void  tripsRetrieved (ArrayList<TripDetail> trips);
}
