package com.example.vehiclechecker;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;


public class Performance extends Fragment {
    // Declaring TextViews
    TextView textView;
    TextView aspiration1;
    TextView cylinders1;
    TextView axle1;
    TextView torque1;
    TextView power1;
    TextView maxspeed1;
    Button back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_performance, container, false);
        // Defining TextViews and obtaining the Regestration Number from the home fragment
        textView = view.findViewById(R.id.regnum);
        textView = view.findViewById(R.id.regnum);
        Bundle bundle = this.getArguments();
        String data = bundle.getString("key");
        textView.setText(data);
        aspiration1 = view.findViewById(R.id.aspiration);
        cylinders1 = view.findViewById(R.id.cylinders);
        axle1 = view.findViewById(R.id.axle);
        torque1 = view.findViewById(R.id.torque);
        power1 = view.findViewById(R.id.power);
        maxspeed1 = view.findViewById(R.id.maxspeed);
        // Setting up the back Button
        back = view.findViewById(R.id.backbtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                // Finish the hosting Activity
                getActivity().finish();
            }
        });
        //Calling the method passing the Vehicle Registration Number
        getVehicleSpecVEHICLEDATA(textView.getText().toString());



        return view;
    }

    private void getVehicleSpecVEHICLEDATA(String reg) {
        //Creating a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number) using the apikey from ukvehicledata and the datapackage VehicleData
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/VehicleData?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_vrm="+reg);
                    // Sending HTTP request using the URL
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Using Buffer to obtain the data from the request
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        // Parse JSON response and get the data needed and assign to variables
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        JSONObject ukvdEnhancedData = dataItems.getJSONObject("UkvdEnhancedData");
                        String Aspiration = dataItems.getJSONObject("TechnicalDetails").getJSONObject("General").getJSONObject("Engine").getString("Aspiration");
                        String Cylinders = dataItems.getJSONObject("TechnicalDetails").getJSONObject("General").getJSONObject("Engine").getString("NumberOfCylinders");
                        String Axle = dataItems.getJSONObject("TechnicalDetails").getJSONObject("General").getString("DrivingAxle");
                        String Torque = dataItems.getJSONObject("TechnicalDetails").getJSONObject("Performance").getJSONObject("Torque").getString("Nm");
                        String Power = dataItems.getJSONObject("TechnicalDetails").getJSONObject("Performance").getJSONObject("Power").getString("Bhp");
                        String Speed = dataItems.getJSONObject("TechnicalDetails").getJSONObject("Performance").getJSONObject("MaxSpeed").getString("Mph");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Setting TextViews and concatenating with the variables declared above
                                aspiration1.setText("Aspiration:  " + Aspiration);
                                cylinders1.setText("Cylinders:  " + Cylinders);
                                axle1.setText("Driving Axle:  " + Axle);
                                torque1.setText("Torque(Nm):  " + Torque);
                                power1.setText("Power(Bhp):  " + Power);
                                maxspeed1.setText("Top Speed(Mph):  " + Speed);
                            }
                        });


                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}