package com.example.sharencare.utils;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.google.maps.model.DirectionsResult;

public class StaticPoolClass {
    public static String recevied_otp;
    public static boolean rideAcceptedFlag=false;
    public static User otherUserDetails;
    public static User currentUserDetails;
    public static TripDetail tripDetails;
    public static UserLocation otherUserLocation;
    public static UserLocation currentUserLocation;
    public static DirectionsResult directionsResultRider;
    public static DirectionsResult directionsResultDriver;

}
