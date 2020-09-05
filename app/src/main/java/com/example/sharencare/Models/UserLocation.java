package com.example.sharencare.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation implements Parcelable {
    private  String user_id;
    private @ServerTimestamp  Date timeStamp;
    private GeoPoint  geoPoint;

    public UserLocation(String user_id, Date timeStamp, GeoPoint geoPoint) {
        this.user_id = user_id;
        this.timeStamp = timeStamp;
        this.geoPoint = geoPoint;
    }

    public UserLocation() {
    }

    protected UserLocation(Parcel in) {
        user_id = in.readString();
    }

    public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
    }
}
