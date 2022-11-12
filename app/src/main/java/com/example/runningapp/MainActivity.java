package com.example.runningapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //-------Step Counting--------
    // experimental values for hi and lo magnitude limits
    private final double HI_STEP = 11.0;     // upper mag limit
    private final double LO_STEP = 8.0;      // lower mag limit
    boolean highLimit = false;      // detect high limit
    int counter = 0;
    TextView tvMag, tvSteps;
    private SensorManager mSensorManager;
    private Sensor mSensor;// step counter

    //--------Timer---------
    CountUpTimer timer;
    TextView counterTime;
    int pauseTime = 0;
    boolean paused = true;
    boolean startingRun = true;
    boolean endOfRun = false;

    //---Buttons----
    Button btnStartStop;
    Button btnStats;
    Button btnReset;
    Button btnEnd;

    //Text to speech
    TextToSpeech TextSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //-------Step Counting--------
        tvMag = findViewById(R.id.tvTime);
        tvSteps = findViewById(R.id.tvSteps);

        // we are going to use the sensor service
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //-------Timer--------
        timer = new CountUpTimer(300000) {  // should be high for the run (ms)
            public void onTick(int second) {
                counterTime.setText(String.valueOf(second + pauseTime));
            }
        };
        //----Buttons---
        //Link buttons
        btnStartStop = findViewById(R.id.btnStart);
        btnStats = findViewById(R.id.btnStats);
        btnReset = findViewById(R.id.btnReset);
        btnEnd = findViewById(R.id.btnEnd);
        //Set long click listener for reset button
        btnReset.setOnLongClickListener
                (
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                ResetAll();
                                return true;
                            }
                        }
                );
        //Set long click listener for end run button
        btnEnd.setOnLongClickListener
                (
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                doEndRun();
                                return true;
                            }
                        }
                );
        //Link timer
        counterTime = findViewById(R.id.tvTime);

        //Text to speech
        TextSpeaker = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            TextSpeaker.setLanguage(Locale.UK);
                        }
                    }
                });
    }

    //region ------StepCounting------
    //Commented out as these cause timer to start immediately
    /*
     * When the app is brought to the foreground - using app on screen

    protected void onResume() {
        super.onResume();
        resumeCounting();
        TimerStart();
    }*/

    /*
     * App running but not on screen - in the background

    protected void onPause() {
        super.onPause();// turn off listener to save power
        pauseCounting();
    }*/


    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        /*tvx.setText(String.valueOf(x));
        tvy.setText(String.valueOf(y));
        tvz.setText(String.valueOf(z));*/

        // get a magnitude number using Pythagorus's Theorem
        double mag = round(Math.sqrt((x * x) + (y * y) + (z * z)), 2);
        //tvMag.setText(String.valueOf(mag));

        // for me! if msg > 11 and then drops below 9, we have a step
        // you need to do your own mag calculating
        if ((mag > HI_STEP) && (highLimit == false)) {
            highLimit = true;
        }
        if ((mag < LO_STEP) && (highLimit == true)) {
            // we have a step
            counter++;
            tvSteps.setText(String.valueOf(counter));
            highLimit = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not used
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public void pauseCounting() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void resumeCounting() {
        super.onResume();
        // turn on the sensor
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    //endregion


    //region ------Timer------
    //Start the timer
    public void TimerStart() {
        timer.start();
        //Toast.makeText(this, "Started counting", Toast.LENGTH_LONG).show();
    }

    //Pause the timer
    public void TimerStop() {
        pauseTime = Integer.parseInt(counterTime.getText().toString());
        timer.cancel();
        //Toast.makeText(this, "Stopped Run", Toast.LENGTH_LONG).show();
    }

    //Reset the timer
    public void TimerReset() {
        timer.cancel();
        counterTime.setText("0");
        pauseTime = 0;
        //Toast.makeText(this, "Reset", Toast.LENGTH_LONG).show();
    }
    //endregion

    //region --------Buttons----------
    //Paused timer and step counting
    public void doPause() {
        //Pauses Step Counting
        pauseCounting();
        //Pauses Timer
        TimerStop();
    }

    //Starts or resumes timer and step counting
    public void doResume() {
        //Resume Step Counting
        resumeCounting();
        //Resume Timer
        TimerStart();
    }

    //Handle the stop start button
    public void doStartStop(View view) {
        if(endOfRun == true)
        {
            //If user has ended a run, dont start new run and display tip to reset activity
            Toast.makeText(this, "Reset before starting a new run", Toast.LENGTH_LONG).show();
        }
        else
        {
            //If app is paused: resume app, disable stats and reset button
            if (paused) {
                //Only fires at the start of run
                if (startingRun) {
                    //Reset on screen values, then announce countdown
                    TextSpeaker.speak("3, 2, 1, go", TextToSpeech.QUEUE_FLUSH, null);
                    startingRun = false;
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Start or resume counting
                doResume();
                //Update start stop button
                btnStartStop.setBackground(getDrawable(R.drawable.segment_top_left_red));
                btnStartStop.setText("Pause");
                paused = false;
                //Disable and visually update stats and reset
                btnStats.setClickable(false);
                btnStats.setBackground(getDrawable(R.drawable.segment_bottom_right_disabled));
                btnReset.setClickable(false);
                btnReset.setBackground(getDrawable(R.drawable.segment_bottom_left_disabled));

            }
            //If app is running: pause app, enable stats and reset button
            else {
                doPause();
                btnStartStop.setBackground(getDrawable(R.drawable.segment_top_left_green));
                btnStartStop.setText("Resume");
                paused = true;
            }
        }
    }

    //Move to the statistics page
    public void doStats(View view) {
        //doPause();
        Intent StatsAct = new Intent(view.getContext(), StatsActivity.class);
        StatsAct.putExtra("time", pauseTime);
        StatsAct.putExtra("steps", counter);
        startActivity(StatsAct);

    }

    //Display info on how to reset
    public void doReset(View view) {
        Toast.makeText(this, "Hold to reset", Toast.LENGTH_LONG).show();
    }

    //Reset everything if reset is pressed
    //Reset without deleting values if End Run is pressed
    public void ResetAll() {
        counterTime.setText("0");
        tvSteps.setText("0");
        counter = 0;
        TimerReset();
        pauseCounting();
        paused = true;
        btnStartStop.setBackground(getDrawable(R.drawable.segment_top_left_green));
        btnStartStop.setText("Start");
        startingRun = true;
        endOfRun = false;
    }

    //Display info on how to end run
    public void doEnd(View view) {
        Toast.makeText(this, "Hold to end run", Toast.LENGTH_LONG).show();
    }

    //Handle end of run
    public void doEndRun() {
        //Enable stats
        btnStats.setClickable(true);
        btnStats.setBackground(getDrawable(R.drawable.segment_bottom_right));
        //Enable reset
        btnReset.setClickable(true);
        btnReset.setBackground(getDrawable(R.drawable.segment_bottom_left));
        //Pause tracking
        doPause();
        endOfRun = true;
        btnStartStop.setBackground(getDrawable(R.drawable.segment_top_left_green_disabled));
        btnStartStop.setText("Start");
    }
    //endregion
}

