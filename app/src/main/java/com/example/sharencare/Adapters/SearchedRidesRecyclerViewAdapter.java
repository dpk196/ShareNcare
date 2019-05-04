package com.example.sharencare.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharencare.Models.FCMData;
import com.example.sharencare.Models.FirebaseCloudMessage;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.ui.MainActivity;
import com.example.sharencare.Interfaces.FCM;
import com.example.sharencare.ui.TripDetailsRider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchedRidesRecyclerViewAdapter extends RecyclerView.Adapter<SearchedRidesRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "SearchedRidesRecycler";
    ArrayList<String > userIDS =new ArrayList<>();
    ArrayList<String > tokenList=new ArrayList<>();
    Context mContext;
    public   static  String  BASE_URL="https://fcm.googleapis.com/fcm/";

    public SearchedRidesRecyclerViewAdapter(ArrayList<String> userLocations, ArrayList<String> tokenList, Context mContext) {
        this.userIDS = userLocations;
        this.tokenList = tokenList;
        this.mContext = mContext;
        Log.d(TAG, "SearchedRidesRecyclerViewAdapter:Server Key: "+MainActivity.server_key);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rides_recyclerview,viewGroup,false);
        ViewHolder holder =new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        int j=i+1;
        String rideNum= String.valueOf(j);
        viewHolder.rideNumber.setText("#"+rideNum);
        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "onClick: UserId:"+userIDS.get(i).toString());
                    Log.d(TAG, "onClick: Token:"+tokenList.get(i).toString());
                    sendRequest(tokenList.get(i),userIDS.get(i), TripDetailsRider.mCollectionTrips.get(i));
                    Toast.makeText(mContext, "Please wait for 2-3 minutes for response", Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    Log.d(TAG, "onClick: Something went wrong please try again");
                    Toast.makeText(mContext, "Something went wrong please try again", Toast.LENGTH_LONG).show();

                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return userIDS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
       TextView rideNumber;
       RelativeLayout parentLayout;
      public ViewHolder(@NonNull View itemView) {
          super(itemView);
          rideNumber=itemView.findViewById(R.id.ride_number);
          parentLayout=itemView.findViewById(R.id.searched_rides_parent_layout);
      }
  }

  private  void sendRequest(String token, String userId, TripDetail trip){
      Log.d(TAG, "sendRequest: Sending request to user:"+userId);
      Log.d(TAG, "sendRequest: Sending request for the token:"+token);


      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(BASE_URL)
              .addConverterFactory(GsonConverterFactory.create())
              .build();
      FCM  fcmAPI=retrofit.create(FCM.class);
      HashMap<String, String> headers=new HashMap<>();
      //attaching the headers
      headers.put("Content-Type","application/json");
      headers.put("Authorization","key="+"AAAApCdkCU8:APA91bFfJApMQKhvYQS-R0P7R9eVcUgd7R2a6iwOe37zPQ5aD2YJY2OMMc6Yx5Utg2HkT9CB9HhKCUWvZkbD4aXBlQN5AJwsfSVfVVc52q-7-mCWuuOh9d4OamKhjuE1PmaP8Lek80w_");

      //token
      FCMData data=new FCMData();
      data.setToUserId(userId);
      data.setFromUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
      data.setData_type("data_type_ride_request");
      data.setTitle("Rider found");

      data.setMessage(MainActivity.currentUser.getUsername()+" "+"wants to ride with you");
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



    }



}
