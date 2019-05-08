package com.example.sharencare.utils;

import android.util.Log;

import com.example.sharencare.Interfaces.FCM;
import com.example.sharencare.Models.FCMData;
import com.example.sharencare.Models.FirebaseCloudMessage;
import com.example.sharencare.Models.User;
import com.example.sharencare.Models.UserLocation;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.sharencare.Adapters.SearchedRidesRecyclerViewAdapter.BASE_URL;
import static com.example.sharencare.utils.StaticPoolClass.serverKey;

public class SendFCMRequest {
    private static final String TAG = "SendFCMRequest";
    private UserLocation location;
    private User  currentUser;
    private  String message;
    private  String token;
    private  String data_type;
    private String otp;
    boolean result;
    boolean flag=false;
    private String title;


    public SendFCMRequest(UserLocation location, User currentUser, String message, String token, String data_type, String otp, String title) {
        this.location = location;
        this.currentUser = currentUser;
        this.message = message;
        this.token = token;
        this.data_type = data_type;
        this.otp = otp;
        this.title = title;
    }

    public   boolean sendRequest(){
        Log.d(TAG, "sendRequest: Sending request for the token:"+token);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FCM fcmAPI=retrofit.create(FCM.class);
        HashMap<String, String> headers=new HashMap<>();
        //attaching the headers
        headers.put("Content-Type","application/json");
        headers.put("Authorization","key="+serverKey);
        FCMData data=new FCMData();
        data.setOtp(otp);
        data.setToUserId(location.getUser_id());
        data.setFromUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.setData_type(data_type);
        data.setTitle(title);
        data.setMessage(message);
        FirebaseCloudMessage firebaseCloudMessage =new FirebaseCloudMessage();
        firebaseCloudMessage.setData(data);
        firebaseCloudMessage.setTo(token);
        Call<ResponseBody> call =fcmAPI.send(headers,firebaseCloudMessage);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: Server Response:"+response.toString());

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: Unable to send message:"+t.getMessage());

            }
        });

      return true;
    }
}
