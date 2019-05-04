package com.example.sharencare.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.sharencare.R;
import com.example.sharencare.utils.DatePickerDialogFragment;
import com.example.sharencare.utils.TimePickerDialogFragment;

public class ScheduleTripDriver extends AppCompatActivity  implements View.OnClickListener , TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "ScheduleTripDriver";
    private TextView setTripStartTime,setTripStartDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_trip_driver);
        setTripStartDate=findViewById(R.id.trip_start_date);
        setTripStartTime=findViewById(R.id.trip_start_time);
        setTripStartTime.setOnClickListener(this);
        setTripStartDate.setOnClickListener(this);



    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(hourOfDay>11)
            TripDetailsDriver.tripDetail.setStart_time(hourOfDay+":"+minute+" "+"PM");
        else
            TripDetailsDriver.tripDetail.setStart_time(hourOfDay+":"+minute+" "+"AM");
        setTripStartTime.setText(TripDetailsDriver.tripDetail.getStart_time());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        TripDetailsDriver.tripDetail.setTrip_date(dayOfMonth+"/"+month+"/"+year);
        Log.d(TAG, "onDateSet: "+TripDetailsDriver.tripDetail.getTrip_date());
        setTripStartDate.setText(TripDetailsDriver.tripDetail.getTrip_date());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trip_start_time: {
                DialogFragment timePicker= new TimePickerDialogFragment();
                timePicker.show(getSupportFragmentManager(),"time picker");
                break;
            }
            case R.id.trip_start_date:{
                DialogFragment datepicker=new DatePickerDialogFragment();
                datepicker.show(getSupportFragmentManager(),"date picker");
                break;
            }


        }
    }
}
