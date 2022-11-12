package com.example.runningapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.time.Instant;
import java.util.Date;

public class StatsActivity extends AppCompatActivity {
    //Data variables
    int steps;
    int time;
    double distance;
    double calories;
    double speed;

    //Layout items
    TextView jtvDistance;
    TextView jtvTime;
    TextView jtvSpeed;
    TextView jtvCalories;
    TextView jtvDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        //Link textviews
        jtvDistance = findViewById(R.id.tvValueDist);
        jtvSpeed = findViewById(R.id.tvValueSpeed);
        jtvTime = findViewById(R.id.tvValueTime);
        jtvCalories = findViewById(R.id.tvValueCalorie);
        jtvDate = findViewById(R.id.tvDate);
        //Retrieve data and do calculations
        steps = getIntent().getIntExtra("steps", 0);
        time = getIntent().getIntExtra("time", 0);
        distance = Math.round((steps*0.8)*100.0)/100.0;
        calories = Math.round((steps*0.04)*100.0)/100.0;
        speed = Math.round((distance/time)*100.0)/100.0;
        //Update display
        jtvDistance.setText(String.valueOf(distance) + " m");
        jtvTime.setText(String.valueOf(time) + " s");
        jtvSpeed.setText(String.valueOf(speed) + " m/s");
        jtvCalories.setText(String.valueOf(calories) + " cals");
        jtvDate.setText(String.valueOf(Date.from(Instant.now())));
    }

    //Return to main activity
    public void doBack(View view) {
        finish();
    }
}