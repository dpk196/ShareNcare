package com.example.sharencare.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sharencare.Interfaces.SearchForLaterOnTripsInterface;
import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.threads.SearchForRideLater;
import com.example.sharencare.utils.DatePickerDialogFragment;
import com.example.sharencare.utils.StaticPoolClass;
import com.example.sharencare.utils.TimePickerDialogFragment;

import java.util.ArrayList;

import static com.example.sharencare.ui.TripDetailsRider.destination;

public class ScheduleTripRequiredDetails extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, SearchForLaterOnTripsInterface {
    private static final String TAG = "ScheduleTripRequiredDet";
    private TextView setTripStartTime, setTripStartDate;
    private String time = "";
    private String date = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_trip_required_details);
        setTripStartDate = findViewById(R.id.trip_start_date);
        setTripStartTime = findViewById(R.id.trip_start_time);
        setTripStartTime.setOnClickListener(this);
        setTripStartDate.setOnClickListener(this);
        findViewById(R.id.schedule_ride).setOnClickListener(this);
        findViewById(R.id.Reschedule_ride).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trip_start_time: {
                DialogFragment timePicker = new TimePickerDialogFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
                break;
            }
            case R.id.trip_start_date: {
                DialogFragment datepicker = new DatePickerDialogFragment();
                datepicker.show(getSupportFragmentManager(), "date picker");
                break;
            }
            case R.id.schedule_ride: {
                if (!date.equals("") && !time.equals("")) {
                    initThreadTripLater();
                }else {
                    Toast.makeText(this, "Cannot be blank", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.Reschedule_ride: {
                Log.d(TAG, "onClick: Redirecting to home activity");
                startActivity(new Intent(ScheduleTripRequiredDetails.this, HomeActivity.class));
                break;
            }
        }
    }

    private void initThreadTripLater() {
        Log.d(TAG, "initThreadTripLater: called");
        SearchForRideLater search = new SearchForRideLater(this, this, TripDetailsRider.source, destination, date);
        search.execute();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (hourOfDay > 11) {
            time = hourOfDay + ":" + minute + " " + "PM";
            setTripStartTime.setText(time);

        } else {
            time = hourOfDay + ":" + minute + " " + "AM";
            setTripStartTime.setText(time);
        }

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = dayOfMonth + "/" + month + "/" + year;
        Log.d(TAG, "onDateSet: " + date);
        setTripStartDate.setText(date);

    }

    @Override
    public void matchedLaterOnTrips(ArrayList<String> userIds, ArrayList<TripDetail> trips) {
        Log.d(TAG, "matchedLaterOnTrips: Called");
        if (userIds.size() > 0) {
            Intent intent = new Intent(ScheduleTripRequiredDetails.this, AvailableRides.class);
            intent.putExtra("matchedRides", userIds);
            StaticPoolClass.IsSchedule =true;
            TripDetailsRider.mCollectionTrips=trips;
            startActivity(intent);
        } else {
            Toast.makeText(this, "No Rides Available to:" + destination, Toast.LENGTH_LONG).show();
        }

    }


}
