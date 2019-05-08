package com.example.sharencare.utils;

import android.util.Log;

public class CalculateDistance  {
    private static final String  TAG = "CalculateDistance";
    static  String fare="";
    static Double toDf;
    public  static  Double calculateDistance(String distance) {
        Log.d(TAG, "calculateFare: Called");
        String f = "";
        try {
            for (int i = 0;i<distance.length() - 3; i++) {
                f = f + distance.charAt(i);
            }
           toDf =Double.valueOf(f);
        } catch (Exception e) {
            Log.d(TAG, "calculateDistance: "+e.getMessage());
        }
        return  toDf;
    }
}
