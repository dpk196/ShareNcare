package com.example.sharencare.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class TripDetail implements Parcelable {
    String start_time;
    String status;
    String trip_source;
    String trip_destination;
    String user_id;
    String trip_duration;
    String trip_fare;
    String trip_distance;
    String trip_date;
    String riderOrDriver;
    @ServerTimestamp Date timestamp;


    protected TripDetail(Parcel in) {
        start_time = in.readString();
        status = in.readString();
        trip_source = in.readString();
        trip_destination = in.readString();
        user_id = in.readString();
        trip_duration = in.readString();
        trip_fare = in.readString();
        trip_distance = in.readString();
        trip_date = in.readString();
        riderOrDriver=in.readString();
    }

    public static final Creator<TripDetail> CREATOR = new Creator<TripDetail>() {
        @Override
        public TripDetail createFromParcel(Parcel in) {
            return new TripDetail(in);
        }

        @Override
        public TripDetail[] newArray(int size) {
            return new TripDetail[size];
        }
    };

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TripDetail{" +
                "start_time='" + start_time + '\'' +
                ", status='" + status + '\'' +
                ", trip_source='" + trip_source + '\'' +
                ", trip_destination='" + trip_destination + '\'' +
                ", user_id='" + user_id + '\'' +
                ", trip_duration='" + trip_duration + '\'' +
                ", trip_fare='" + trip_fare + '\'' +
                ", trip_distance='" + trip_distance + '\'' +
                ", trip_date='" + trip_date + '\'' +
                ", riderOrDriver='" + riderOrDriver + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public String getRiderOrDriver() {
        return riderOrDriver;
    }

    public void setRiderOrDriver(String riderOrDriver) {
        this.riderOrDriver = riderOrDriver;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public TripDetail(String start_time, String status, String trip_source, String trip_destination, String user_id, String trip_duration, String trip_fare, String trip_distance, String trip_date, String riderOrDriver, Date timestamp) {
        this.start_time = start_time;
        this.status = status;
        this.trip_source = trip_source;
        this.trip_destination = trip_destination;
        this.user_id = user_id;
        this.trip_duration = trip_duration;
        this.trip_fare = trip_fare;
        this.trip_distance = trip_distance;
        this.trip_date = trip_date;
        this.riderOrDriver = riderOrDriver;
        this.timestamp = timestamp;
    }

    public String getTrip_distance() {
        return trip_distance;
    }

    public void setTrip_distance(String trip_distance) {
        this.trip_distance = trip_distance;
    }

    public String getTrip_date() {
        return trip_date;
    }

    public void setTrip_date(String trip_date) {
        this.trip_date = trip_date;
    }

    public String getTrip_fare() {
        return trip_fare;
    }

    public void setTrip_fare(String trip_fare) {
        this.trip_fare = trip_fare;
    }

    public TripDetail() {
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

    public String getTrip_duration() {
        return trip_duration;
    }

    public void setTrip_duration(String trip_duration) {
        this.trip_duration = trip_duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(start_time);
        dest.writeString(status);
        dest.writeString(trip_source);
        dest.writeString(trip_destination);
        dest.writeString(user_id);
        dest.writeString(trip_duration);
        dest.writeString(trip_fare);
        dest.writeString(trip_distance);
        dest.writeString(trip_date);
        dest.writeString(riderOrDriver);
    }
}
