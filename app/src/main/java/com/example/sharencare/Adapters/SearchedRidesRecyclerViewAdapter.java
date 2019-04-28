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
import com.example.sharencare.R;

import java.util.ArrayList;

public class SearchedRidesRecyclerViewAdapter extends RecyclerView.Adapter<SearchedRidesRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "SearchedRidesRecycler";
    ArrayList<String > userLocations =new ArrayList<>();
    Context mContext;

    public SearchedRidesRecyclerViewAdapter(ArrayList<String> userLocations, Context mContext) {
        this.userLocations = userLocations;
        this.mContext = mContext;
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
                Log.d(TAG, "onClick: Ride"+userLocations.get(i).toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userLocations.size();
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


}
