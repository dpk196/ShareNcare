package com.example.sharencare.Models;

public class FCMData {
    private String title;
    private String fromUserId;
    private String toUserId;
    private String data_type;
    private String otp;
    private String message;

    public FCMData(String title, String fromUserId, String toUserId, String data_type, String otp, String message) {
        this.title = title;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.data_type = data_type;
        this.otp = otp;
        this.message = message;
    }

    public FCMData() {
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "FCMData{" +
                "title='" + title + '\'' +
                ", fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", data_type='" + data_type + '\'' +
                ", otp='" + otp + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
