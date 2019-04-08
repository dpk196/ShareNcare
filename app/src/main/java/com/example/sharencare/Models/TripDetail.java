package com.example.sharencare.Models;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.List;


public class TripDetail  {
    String start_time;
    String status;
    String trip_source;
    String trip_destination;
    String user_id;

    public TripDetail(String start_time, String status, GeoPoint sourceGeoPoint, GeoPoint destinationGeoPoint, String trip_source, String trip_destination, String user_id) {
        this.start_time = start_time;
        this.status = status;

        this.trip_source = trip_source;
        this.trip_destination = trip_destination;
        this.user_id = user_id;
    }

    public TripDetail() {
    }


    @Override
    public String toString() {
        return "TripDetail{" +
                "start_time='" + start_time + '\'' +
                ", status='" + status + '\'' +
                ", trip_source='" + trip_source + '\'' +
                ", trip_destination='" + trip_destination + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrip_source() {
        return trip_source;
    }

    public void setTrip_source(String trip_source) {
        this.trip_source = trip_source;
    }

    public String getTrip_destination() {
        return trip_destination;
    }

    public void setTrip_destination(String trip_destination) {
        this.trip_destination = trip_destination;
    }

}
