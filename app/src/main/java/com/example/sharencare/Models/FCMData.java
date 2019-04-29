package com.example.sharencare.Models;

public class FCMData {
    private String title;
    private String fromUserId;
    private String toUserId;
    private String data_type;

    public FCMData(String title, String fromUserId, String toUserId, String data_type) {
        this.title = title;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.data_type = data_type;
    }


    public FCMData() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Data{" +
                "title='" + title + '\'' +
                ", fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", data_type='" + data_type + '\'' +
                '}';
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
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
}
