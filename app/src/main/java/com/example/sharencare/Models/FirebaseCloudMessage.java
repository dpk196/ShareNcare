package com.example.sharencare.Models;

public class FirebaseCloudMessage {
    private  String to;
    private FCMData data;

    public FirebaseCloudMessage(String to, FCMData data) {
        this.to = to;
        this.data = data;
    }

    public FirebaseCloudMessage() {
    }

    @Override
    public String toString() {
        return "FirebaseCloudMessage{" +
                "to='" + to + '\'' +
                ", data=" + data +
                '}';
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public FCMData getData() {
        return data;
    }

    public void setData(FCMData data) {
        this.data = data;
    }
}
