package com.example.vehiclechecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class MainActivity extends AppCompatActivity {
    // Declaring the Fragments, and the NavigationView
    BottomNavigationView bottomNavigationView;
    Home homeFragment = new Home();
    Garage garageFragment = new Garage();
    Fuel fuelFragment = new Fuel();
    Profile profileFragment = new Profile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Intializing the Layout
        setContentView(R.layout.activity_main);
        // Defining the navigation view and adding a listener
        bottomNavigationView = findViewById(R.id.bottomnav);
        // Changing the page to the home fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, homeFragment).commit();

        // Setting up the different items on the Navigation View
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    //If home clicked open home fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, homeFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.garage) {
                    //If garage clicked open garage fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, garageFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.fuel) {
                    //If fuel clicked open fuel fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, fuelFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.profile) {
                    //If profile clicked open profile fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, profileFragment).commit();
                    return true;
                }
                return false;            }


        });
    }
}