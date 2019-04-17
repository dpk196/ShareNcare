package com.example.sharencare.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.ui.TripDetailsDriver;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> source =new ArrayList<>();
    private ArrayList<String> destination=new ArrayList<>();
    private Context mContext;
    private ArrayList<TripDetail> tripDetail;


    public RecyclerViewAdapter(ArrayList<String> source, ArrayList<String> destination, Context mContext, ArrayList<TripDetail> tripDetail) {
        this.source = source;
        this.destination = destination;
        this.mContext = mContext;
        this.tripDetail=tripDetail;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_listitem,viewGroup,false);
        ViewHolder holder =new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Log.d(TAG, "onBindViewHolder: called");
        viewHolder.src.setText(source.get(i));
        viewHolder.des_nation.setText(destination.get(i));
        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, TripDetailsDriver.class);
                intent.putExtra("tripDetails", tripDetail);
                intent.putExtra("Source",source.get(i));
                intent.putExtra("Destination",destination.get(i));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return destination.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        TextView src;
        TextView  des_nation;
        RelativeLayout parentLayout;
        private TextView tripDetails;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            src=itemView.findViewById(R.id.source);
            des_nation=itemView.findViewById(R.id.destination);
            parentLayout=itemView.findViewById(R.id.parent_layout);



        }
    }
}

