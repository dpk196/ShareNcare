package com.example.sharencare.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.sharencare.Models.TripDetail;
import com.example.sharencare.R;

public class SetStartTime extends AppCompatActivity {
    TextView textview1;
    TimePicker timepicker;
    Button changetime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_start_time);

        //textview1=(TextView)findViewById(R.id.textView1);
        timepicker=(TimePicker)findViewById(R.id.timePicker);
        //Uncomment the below line of code for 24 hour view
        timepicker.setIs24HourView(true);
        changetime=(Button)findViewById(R.id.button1);

       // textview1.setText(getCurrentTime());

        changetime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //textview1.setText(getCurrentTime());
               Intent intent= new Intent(SetStartTime.this,TripDetails.class);
               intent.putExtra("PickedStartTime",getCurrentTime());
                startActivity(intent);
            }
        });

    }

    public String getCurrentTime(){
        String currentTime=timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute();
        return currentTime;
    }
}

