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
import java.net.HttpURLConnection;
import java.net.URL;


public class History extends Fragment {
    // Declaring Textviews
    TextView textView;
    TextView owners;
    TextView prevaccident;
    TextView finance;
    TextView stolen;
    TextView imported;
    TextView hpi;
    Button back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        // Defining the TextViews
        textView = view.findViewById(R.id.regnum);
        owners = view.findViewById(R.id.owners);
        prevaccident = view.findViewById(R.id.prevaccident);
        finance = view.findViewById(R.id.finance);
        stolen = view.findViewById(R.id.stolen);
        imported = view.findViewById(R.id.imported);
        hpi = view.findViewById(R.id.hpi);
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

        // Obtaining the Regestration Number from the Home fragment
        Bundle bundle = this.getArguments();
        String data = bundle.getString("key");
        textView.setText(data);
        //Calling the Methods passing the registration number
        getVdiCheckFull(textView.getText().toString());
        getOwnersCount(textView.getText().toString());
        return view;
    }

    private void getOwnersCount(String reg) {
        //Creating a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number)
                    // Using the API key Obtained from ukvehicledata and the datapackage VehicleAndMotHistory
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/VehicleAndMotHistory?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_VRM="+reg);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Creating a buffer tp read the data
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();
                        // Parse JSON response to obtain the relevant data and update TextViews
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        String ownerscnt = dataItems.getJSONObject("VehicleHistory").getString("NumberOfPreviousKeepers");
                        //Set Textviews with the corresponding variable
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                owners.setText("Previous Owners Count: " + ownerscnt);

                            }
                        });


                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    private void getVdiCheckFull(String reg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number)
                    //Use the apikey from ukvehicledata using the VdiCheckFull data package to gain access to the data and add it to URL
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/VdiCheckFull?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_VRM="+reg);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Creating a buffer to read the data
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();
                        // Parse JSON response get the relevant data and update TextViews
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        String writtenOff = dataItems.getString("WrittenOff");
                        String outfinance = dataItems.getString("FinanceRecordCount");
                        String stolen1 = dataItems.getString("Stolen");
                        String imports = dataItems.getString("Imported");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Setting the TextViews
                                prevaccident.setText("Previous Accident: " + writtenOff);
                                finance.setText("Outstanding Finance: " + outfinance);
                                stolen.setText("Stolen: " + stolen1);
                                imported.setText("Imported: " + imports);
                                // Perfoming Calulcations to check if Vehicle is HPI clear
                                String hpicheck = "";
                                if (outfinance.equals("0") && writtenOff.equals("false") && stolen1.equals("false") && imports.equals("false")) {
                                    hpicheck = "True";
                                }else  {
                                    hpicheck =  "false";
                                }
                                hpi.setText("Hpi Clear: " + hpicheck);

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