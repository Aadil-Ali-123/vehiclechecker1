package com.example.vehiclechecker;

import android.content.Intent;
import android.os.AsyncTask;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Specification extends Fragment {
    // Declaring TextViews and Buttons.
    TextView textView;
    TextView make;
    TextView model;
    TextView year;
    TextView enginesize;
    TextView fueltype;
    TextView colour;
    TextView euro;
    TextView bodytype;
    Button back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_specification, container, false);
        // Getting the textview from home fragment and Setting the textview to display to the user
        textView = view.findViewById(R.id.regnum);
        Bundle bundle = this.getArguments();
        String data = bundle.getString("key");
        textView.setText(data);
        // Defining the TextViews
        make = view.findViewById(R.id.make);
        model = view.findViewById(R.id.model);
        colour = view.findViewById(R.id.colour);
        year = view.findViewById(R.id.year);
        enginesize = view.findViewById(R.id.enginesize);
        fueltype = view.findViewById(R.id.fueltype);
        euro = view.findViewById(R.id.eurostatus);
        bodytype = view.findViewById(R.id.bodytype);

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

        // Calling the Methods passing the Registration Number
        getVehicleSpecDVLA(textView.getText().toString());
        getVehicleSpecVEHICLEDATA(textView.getText().toString());
        return view;
    }

    private void getVehicleSpecVEHICLEDATA(String reg) {
        // Creating a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number), API KEY, And data package which is VehicleData
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/VehicleData?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_vrm="+reg);
                    // Sending the HTTP URL request
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    // Obtaining the data from the HTML Request using Buffered Reader
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        // Parse JSON response and add the relevant data to variables
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        JSONObject ukvdEnhancedData = dataItems.getJSONObject("UkvdEnhancedData");
                        String model1 = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("VehicleRegistration").getString("Model");
                        String euro1 = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("TechnicalDetails").getJSONObject("General").getString("EuroStatus");
                        String body = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("SmmtDetails").getString("BodyStyle");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Setting the TextView and Concatenating the variables to portray to the user
                                model.setText("Model:  " + model1);
                                euro.setText("Euro Status:  " + euro1);
                                bodytype.setText("Body Type:  " + body);
                            }
                        });



                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getVehicleSpecDVLA(String reg) {
        // Creating a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number)
                    URL url = new URL("https://driver-vehicle-licensing.api.gov.uk/vehicle-enquiry/v1/vehicles");

                    // Sending the Request using Api Key
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("x-api-key", "RA8DuzbuYU7eUq6vDQw5r6V5TmKG8VPc2rNzL2bD");
                    conn.setDoOutput(true);

                    // Creating JSON object for the request body
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("registrationNumber", reg);

                    // Writing request body to output stream
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(requestBody.toString().getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Reading the data using a Buffered Reader
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Parse JSON response and Setting Textviews and concatenating the relevant data accordingly
                        JSONObject jsonObject = new JSONObject(response.toString());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                make.setText("Make:  " + jsonObject.optString("make"));
                                year.setText("Year:  " + jsonObject.optString("yearOfManufacture"));
                                enginesize.setText("Engine Size  :  " + jsonObject.optString("engineCapacity") + "cc");
                                fueltype.setText("Fuel Type:  " + jsonObject.optString("fuelType"));
                                colour.setText("Colour:  " + jsonObject.optString("colour"));
                            }
                        });
                    }
                    conn.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}