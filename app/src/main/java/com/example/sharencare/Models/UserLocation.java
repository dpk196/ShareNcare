package com.example.sharencare.Models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation {
    private  String user_id;
    private @ServerTimestamp  Date timeStamp;
    private GeoPoint  geoPoint;

    public UserLocation(String user_id, Date timeStamp, GeoPoint geoPoint) {
        this.user_id = user_id;
        this.timeStamp = timeStamp;
        this.geoPoint = geoPoint;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user_id='" + user_id + '\'' +
                ", timeStamp=" + timeStamp +
                ", geoPoint=" + geoPoint +
                '}';
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
