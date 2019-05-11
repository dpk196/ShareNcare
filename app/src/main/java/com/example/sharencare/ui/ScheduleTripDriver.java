package com.example.sharencare.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;
import com.example.sharencare.utils.CalculateFare;
import com.example.sharencare.utils.DatePickerDialogFragment;
import com.example.sharencare.utils.SendFCMRequest;
import com.example.sharencare.utils.StaticPoolClass;
import com.example.sharencare.utils.TimePickerDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class ScheduleTripDriver extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "ScheduleTripDriver";
    private String m_Text = "Free Ride";
    private TextView setTripStartTime, setTripStartDate, fareValueTextView, maxChargeableFare;
    private String tripStatus = "Trip on";
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_trip_driver);
        Log.d(TAG, "onCreate: TripDetails At start:" + TripDetailsDriver.tripDetail.toString());
        TripDetailsDriver.tripDetail.setStatus("");
        setTripStartDate = findViewById(R.id.trip_start_date);
        setTripStartTime = findViewById(R.id.trip_start_time);
        setTripStartTime.setOnClickListener(this);
        setTripStartDate.setOnClickListener(this);
        findViewById(R.id.schedule_ride).setOnClickListener(this);
        findViewById(R.id.Reschedule_ride).setOnClickListener(this);
        findViewById(R.id.change_fare_button).setOnClickListener(this);
        fareValueTextView = findViewById(R.id.fare_id_value);
        fareValueTextView.setText(TripDetailsDriver.tripDetail.getTrip_fare());
        maxChargeableFare = findViewById(R.id.max_charge_value);
        maxChargeableFare.setText("Rs" + " " + CalculateFare.calculateFare(TripDetailsDriver.tripDetail.getTrip_distance()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (hourOfDay > 11) {
            TripDetailsDriver.tripDetail.setStart_time(hourOfDay + ":" + minute + " " + "PM");
            setTripStartTime.setText(TripDetailsDriver.tripDetail.getStart_time());

        } else {
            TripDetailsDriver.tripDetail.setStart_time(hourOfDay + ":" + minute + " " + "AM");
            setTripStartTime.setText(TripDetailsDriver.tripDetail.getStart_time());
        }

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        TripDetailsDriver.tripDetail.setTrip_date(dayOfMonth + "/" + month + "/" + year);
        Log.d(TAG, "onDateSet: " + TripDetailsDriver.tripDetail.getTrip_date());
        setTripStartDate.setText(TripDetailsDriver.tripDetail.getTrip_date());
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
            case R.id.change_fare_button: {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleTripDriver.this);
                builder.setTitle("Enter Fare");
                final EditText input = new EditText(getApplicationContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().equals("")) {
                            m_Text = input.getText().toString();
                            if (Double.valueOf(CalculateFare.calculateFare(TripDetailsDriver.tripDetail.getTrip_distance())) <= Double.valueOf(m_Text) || m_Text.equals("")) {
                                Toast.makeText(ScheduleTripDriver.this, "Fare canot be greater than " + CalculateFare.calculateFare(TripDetailsDriver.tripDetail.getTrip_distance()), Toast.LENGTH_SHORT).show();
                                m_Text = "Free Ride";
                                TripDetailsDriver.tripDetail.setTrip_fare(m_Text);
                                fareValueTextView.setText(TripDetailsDriver.tripDetail.getTrip_fare());
                                Log.d(TAG, "onClick: TripDetails Changed to:" + TripDetailsDriver.tripDetail.toString());
                            } else {
                                TripDetailsDriver.tripDetail.setTrip_fare(m_Text);
                                fareValueTextView.setText("Rs" + " " + TripDetailsDriver.tripDetail.getTrip_fare());
                                Log.d(TAG, "onClick: Fare set to:" + m_Text);
                                Log.d(TAG, "onClick: TripDetails Changed to:" + TripDetailsDriver.tripDetail.toString());
                            }
                        }
                    }

                });
                builder.show();
                break;
            }
            case R.id.schedule_ride: {
                if (!TripDetailsDriver.tripDetail.getStart_time().equals("") && !TripDetailsDriver.tripDetail.getStart_time().equals("")) {
                    TripDetailsDriver.tripDetail.setStatus("Trip On" + " " + TripDetailsDriver.tripDetail.getTrip_date() + " " + TripDetailsDriver.tripDetail.getStart_time());
                    Log.d(TAG, "onClick: TripDetail object before sending to firestore:" + TripDetailsDriver.tripDetail.toString());
                    submitDetailsToFireStore(TripDetailsDriver.tripDetail);
                } else {
                    Toast.makeText(this, "Please Fill in all the details", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.Reschedule_ride: {
                Log.d(TAG, "onClick: Redirecting to home activity");
                startActivity(new Intent(ScheduleTripDriver.this, HomeActivity.class));
                break;
            }
        }
    }

    private void submitDetailsToFireStore(TripDetail tripDetail) {
        Log.d(TAG, "suubmitDetailsToFirestore: Submitting details to FireStore");

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newTripRef = mDb.collection(getString(R.string.collection_trips)).document();
        Log.d(TAG, "submitDetailsToFireStore: id of the document:"+newTripRef.getId());
        tripDetail.setTripId(newTripRef.getId());
        newTripRef.set(tripDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Details Submitted Successfully");
                    Log.d(TAG, "onComplete: " + task.toString());
                    Toast.makeText(ScheduleTripDriver.this, "You will be notified when a rider is found", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleTripDriver.this);
                    builder.setMessage("You will be Notified when a rider is found");
                    builder.setTitle("Have a safe Journey");
                    builder.setCancelable(false);
                    builder
                            .setPositiveButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            Intent intent = new Intent(ScheduleTripDriver.this, HomeActivity.class);
                                            startActivity(intent);

                                        }
                                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                } else {
                    Log.d(TAG, "onComplete: Something went wrong");
                }
            }
        });


    }
}
