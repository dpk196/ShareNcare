package com.example.sharencare.utils;

import android.util.Log;

public class CalculateFare {
    private static final String TAG = "CalculateFare";
    static  String fare="";
    public  static  String calculateFare(String distance) {
        Log.d(TAG, "calculateFare: Called");
        String f = "";
        try {
            for (int i = 0;i<distance.length() - 3; i++) {
                f = f + distance.charAt(i);
            }
            Double toDf=Double.valueOf(f) * 7;
            Integer f_re = toDf.intValue();
            fare=f_re.toString();
            Log.d(TAG, "calculateFare: Fare:" + f_re.toString());

        } catch (Exception e) {

        }
        return fare;
    }
}
