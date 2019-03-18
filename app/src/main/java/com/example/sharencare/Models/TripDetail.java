package com.example.sharencare.Models;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;

public class TripDetail {
    String tripStartTime;
    String tripDuration;
    String tripFare;
    String tripDistance;
    String tripStatus;
    String tripSource;
    String tripDestination;
    String userID;
    @ServerTimestamp java.util.Date time_stamp;

    public TripDetail(String tripStartTime, String tripDuration, String tripFare, String tripDistance, String tripStatus, String tripSource, String tripDestination, String userID, java.util.Date time_stamp) {
        this.tripStartTime = tripStartTime;
        this.tripDuration = tripDuration;
        this.tripFare = tripFare;
        this.tripDistance = tripDistance;
        this.tripStatus = tripStatus;
        this.tripSource = tripSource;
        this.tripDestination = tripDestination;
        this.userID = userID;
        this.time_stamp = time_stamp;
    }

    public TripDetail() {
    }

    public String getTripStartTime() {
        return tripStartTime;
    }

    public void setTripStartTime(String tripStartTime) {
        this.tripStartTime = tripStartTime;
    }

    public String getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(String tripDuration) {
        this.tripDuration = tripDuration;
    }

    public String getTripFare() {
        return tripFare;
    }

    public void setTripFare(String tripFare) {
        this.tripFare = tripFare;
    }

    public String getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(String tripDistance) {
        this.tripDistance = tripDistance;
    }

    public String getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(String tripStatus) {
        this.tripStatus = tripStatus;
    }

    public String getTripSource() {
        return tripSource;
    }

    public void setTripSource(String tripSource) {
        this.tripSource = tripSource;
    }

    public String getTripDestination() {
        return tripDestination;
    }

    public void setTripDestination(String tripDestination) {
        this.tripDestination = tripDestination;
    }

    @Override
    public String toString() {
        return "TripDetail{" +
                "tripStartTime='" + tripStartTime + '\'' +
                ", tripDuration='" + tripDuration + '\'' +
                ", tripFare='" + tripFare + '\'' +
                ", tripDistance='" + tripDistance + '\'' +
                ", tripStatus='" + tripStatus + '\'' +
                ", tripSource='" + tripSource + '\'' +
                ", tripDestination='" + tripDestination + '\'' +
                ", userID='" + userID + '\'' +
                ", time_stamp=" + time_stamp +
                '}';
    }

    public java.util.Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(java.util.Date time_stamp) {
        this.time_stamp = time_stamp;
    }



    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}
