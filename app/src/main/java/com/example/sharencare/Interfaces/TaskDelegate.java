package com.example.sharencare.Interfaces;

import com.google.maps.model.DirectionsResult;

public interface TaskDelegate {
    void onDirectionsRetrived(DirectionsResult result);
}
