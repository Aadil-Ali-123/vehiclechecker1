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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;


public class RunningCost extends Fragment {
    // Declaring the TextViews, Buttons and Variables
    TextView textView;
    TextView tanksize;
    TextView range;
    TextView co2;
    TextView co2rating;
    TextView mpg;
    TextView cost;
    TextView roadTax6;
    TextView roadTax12;
    String fuelType;
    Button back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_cost, container, false);
        // Obtaining the regestration number from the home fragment and displaying it to the user
        textView = view.findViewById(R.id.regnum);
        Bundle bundle = this.getArguments();
        String data = bundle.getString("key");
        textView.setText(data);
        //Defining the TextViews
        tanksize = view.findViewById(R.id.fueltank);
        range = view.findViewById(R.id.fuelrange);
        co2 = view.findViewById(R.id.co2emissions);
        mpg = view.findViewById(R.id.mpg);
        cost = view.findViewById(R.id.fuelcost);
        roadTax6 = view.findViewById(R.id.roadtax6);
        roadTax12 = view.findViewById(R.id.roadtax12);
        co2rating = view.findViewById(R.id.band);

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
        //Calling the Methods
        getVehicleSpecVEHICLEDATA(textView.getText().toString());
        getVehicleSpecDVLA(textView.getText().toString());
        return view;
    }

    private void getVehicleSpecVEHICLEDATA(String reg) {
        //Creating a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number) using the apikey, datapackage as VehicleData and the regestration number
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/VehicleData?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_vrm="+reg);
                    // Sending the HTTP URL Request
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Creating a buffer reader to receive the data from the api
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        // Parse JSON response assign the relevant information obtained into variables
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        JSONObject ukvdEnhancedData = dataItems.getJSONObject("UkvdEnhancedData");
                        String fueltank1 = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("TechnicalDetails").getJSONObject("Dimensions").getString("FuelTankCapacity");
                        String mpg1 = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("TechnicalDetails").getJSONObject("Consumption").getJSONObject("Combined").getString("Mpg");
                        String roadtax6 = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("VehicleStatus").getJSONObject("MotVed")
                                .getJSONObject("VedRate").getJSONObject("Standard").getString("SixMonth");
                        String roadtax12 = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("VehicleStatus").getJSONObject("MotVed")
                                .getJSONObject("VedRate").getJSONObject("Standard").getString("TwelveMonth");
                        String co2rating1 = jsonObject.getJSONObject("Response").getJSONObject("DataItems").getJSONObject("VehicleStatus").getJSONObject("MotVed")
                                .getString("VedBand");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Setting the TextViews and concatenating the data from the variable to display to the user
                                tanksize.setText("Fuel Tank Size(Litres):  " + fueltank1);
                                mpg.setText("MPG Combined:  " + mpg1);
                                roadTax6.setText("Road Tax 6 Months:  " + roadtax6);
                                roadTax12.setText("Road Tax 12 Months:  " + roadtax12);
                                co2rating.setText("CO2 Rating:  " + co2rating1);
                                // Calculating the Vehicle Range and setting the text view
                                float range1 = ((Float.parseFloat(mpg1)) / 4.54609188f) * (Float.parseFloat(fueltank1));
                                range.setText("Range(Miles):  " + Math.round(range1));
                                // Calculating the fuel cost for 100 miles and setting the textview
                                float mpl = Float.parseFloat(mpg1) / 4.54609188f;
                                float costoffuel = 0f;
                                if (Objects.equals(fuelType, "PETROL")) {
                                    costoffuel = 1.50f;
                                } else if (Objects.equals(fuelType, "DIESEL")) {
                                    costoffuel = 1.60f;
                                }
                                float totalfuelcost = 100 * costoffuel;
                                int miles100 = Math.round(totalfuelcost/mpl);
                                cost.setText("Fuel Cost for 100Miles:  " + miles100 + "£");
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
                    // Sending HTTP request using the API Key
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("x-api-key", "RA8DuzbuYU7eUq6vDQw5r6V5TmKG8VPc2rNzL2bD");
                    conn.setDoOutput(true);

                    // Creating a JSON object for the request body
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("registrationNumber", reg);

                    // Write request body to output stream
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(requestBody.toString().getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Getting the data from the request using a BufferReader
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Parse JSON response and update TextViews
                        JSONObject jsonObject = new JSONObject(response.toString());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                co2.setText("CO2 Emissions:  " + jsonObject.optString("co2Emissions"));
                                fuelType = (jsonObject.optString("fuelType"));
                            }
                        });


                    }
                    conn.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
}}