package com.example.vehiclechecker;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;


public class Mot extends Fragment {
    // Declaring TextViews
    TextView textView;

    TextView roadtaxexpire;
    TextView motExpire;
    TextView motDate;
    TextView result;
    TextView mileage;
    TextView anamaly;
    TextView advisory;
    Button back;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mot, container, false);
        //Defining the TextViews
        textView = view.findViewById(R.id.regnum);

        roadtaxexpire = view.findViewById(R.id.roadtaxexp);
        motExpire = view.findViewById(R.id.motexpire);
        motDate = view.findViewById(R.id.recentmot);
        result = view.findViewById(R.id.result);
        mileage = view.findViewById(R.id.mileage);
        anamaly = view.findViewById(R.id.milageanomaly);
        advisory = view.findViewById(R.id.advisories);

        // Using Bundele to obtain the Regestration Number passed from home fragment
        Bundle bundle = this.getArguments();
        String data = bundle.getString("key");
        // Setting TextView as the Regestration Number
        textView.setText(data);
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

        // Calling the Methods whilst passing the Regestration Number
        getVehicletaxdata(textView.getText().toString());
        getVehicleMotData(textView.getText().toString());

        return view;
    }

    private void getVehicleMotData(String reg) {
        // Creating a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number) APIKEY for ukvehicledata and the datapackage MotHistoryAndTaxStatusData
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/MotHistoryAndTaxStatusData?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_VRM="+reg);
                    // Sending Http request using the URL
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Obtaining the data received from the request
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        // Parse JSON response and assign the information needed to variables
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        String motexpiry = dataItems.getJSONObject("VehicleStatus").getString("NextMotDueDate");
                        JSONArray recordlist = dataItems.getJSONObject("MotHistory").getJSONArray("RecordList");
                        JSONObject firstrecord = recordlist.getJSONObject(0);
                        String motdate = firstrecord.getString("TestDate");
                        String results = firstrecord.getString("TestResult");
                        String mileage1 = firstrecord.getString("OdometerReading");
                        String mileageinc = firstrecord.getString("MileageSinceLastPass");
                        String anomaly = firstrecord.getString("MileageAnomalyDetected");
                        String advisoaryies = firstrecord.getString("AdvisoryNoticeList");
                        String failreason = firstrecord.getString("FailureReasonList");



                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Setting the TextViews with the variables created above accordingly
                                motExpire.setText("Mot Expiry Date: " + motexpiry);
                                motDate.setText("Recent Mot Date: " + motdate);
                                result.setText("Result: " + results);
                                mileage.setText("Mileage: " + mileage1 + " Mileage Increase: " + mileageinc);
                                anamaly.setText("Mileage Anomaly: " + anomaly);
                                // Checking if vehicle passes display advisories, if fail then display the failure Reasons
                                if (results.equals("Pass")) {
                                    advisory.setText("Advisories: " + advisoaryies);
                                } else {
                                    advisory.setText("Fail Reason(s): " + failreason);
                                }
                            }
                        });


                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getVehicletaxdata(String reg) {
        //Creating Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input using the APIKEY from ukvehicledata and datapackage VehicleTaxData
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/VehicleTaxData?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_VRM="+reg);
                    // Sending HTTP request using URL
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Obtaining information from the request using Buffer
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();
                        // Parse JSON response and get relevent information and assign to variables
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        String roadtaxexp = dataItems.getJSONObject("VehicleStatus").getJSONObject("MotVed").getString("VedExpiryDate");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Set the TextView and concatenate with variable defined above
                                roadtaxexpire.setText("Road Tax Expire Date: " + roadtaxexp);
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