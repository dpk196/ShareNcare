package com.example.sharencare.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String email;
    private String user_id;
    private String username;
    private String avatar;
    private String registration_number;
    private String  mobile_number;
    private String driver_or_rider;
    private String token;
    private String vehicle_name;

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        avatar = in.readString();
        registration_number = in.readString();
        mobile_number = in.readString();
        driver_or_rider = in.readString();
        token = in.readString();
        vehicle_name = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", registration_number='" + registration_number + '\'' +
                ", mobile_number='" + mobile_number + '\'' +
                ", driver_or_rider='" + driver_or_rider + '\'' +
                ", token='" + token + '\'' +
                ", vehicle_name='" + vehicle_name + '\'' +
                '}';
    }

    public String getVehicle_name() {
        return vehicle_name;
    }

    public void setVehicle_name(String vehicle_name) {
        this.vehicle_name = vehicle_name;
    }

    public User(String email, String user_id, String username, String avatar, String registration_number, String mobile_number, String driver_or_rider, String token, String vehicle_name) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.avatar = avatar;
        this.registration_number = registration_number;
        this.mobile_number = mobile_number;
        this.driver_or_rider = driver_or_rider;
        this.token = token;
        this.vehicle_name = vehicle_name;
    }

    public User() {

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }




    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getRegistration_number() {
        return registration_number;
    }

    public void setRegistration_number(String registration_number) {
        this.registration_number = registration_number;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getDriver_or_rider() {
        return driver_or_rider;
    }

    public void setDriver_or_rider(String driver_or_rider) {
        this.driver_or_rider = driver_or_rider;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(username);
        dest.writeString(avatar);
        dest.writeString(registration_number);
        dest.writeString(mobile_number);
        dest.writeString(driver_or_rider);
        dest.writeString(token);
        dest.writeString(vehicle_name);
    }
}

