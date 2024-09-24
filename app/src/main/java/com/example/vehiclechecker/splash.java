package com.example.vehiclechecker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class splash extends AppCompatActivity {
    // Defining how long the screen stays
    private long ms=0;
    private static long splashTime = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setting the layout
        setContentView(R.layout.activity_splash);
        // Creating a thread
        Thread mythread = new Thread() {
            public void run(){
                try {
                    while (ms < splashTime) {
                        // Displaying the splash layout until the time limit has been reached
                        ms = ms+100;
                        sleep(100);
                    }
                } catch (Exception e) {}
                finally {
                    // Once timer has finished start the intent for The MainActivity page
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        // Start the thread
        mythread.start();
    }
}