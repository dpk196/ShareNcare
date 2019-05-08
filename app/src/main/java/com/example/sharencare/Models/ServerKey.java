package com.example.sharencare.Models;

public class ServerKey {
    String key;


    public ServerKey(String key) {
        this.key = key;
    }

    public ServerKey() {
    }

    @Override
    public String toString() {
        return "ServerKey{" +
                "key='" + key + '\'' +
                '}';
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
