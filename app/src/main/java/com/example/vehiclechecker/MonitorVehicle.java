package com.example.vehiclechecker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MonitorVehicle extends Fragment {
    // Declaring the EditText, Buttons, TextViews, Firebase RealTimeDatabase
    EditText editText;
    Button monitorButton;
    DatabaseReference databaseReference;
    TextView email;
    TextView roadtax;
    TextView mot;
    TextView textView;
    String motday;
    String Roadtaxday;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating the layout
        View view = inflater.inflate(R.layout.fragment_monitor_vehicle, container, false);
        // Defining the Buttons, EditText, and TextViews
        editText = view.findViewById(R.id.mileageinput);
        monitorButton = view.findViewById(R.id.confirm);
        email = view.findViewById(R.id.textView2);
        roadtax = view.findViewById(R.id.roadtaxdays);
        textView = view.findViewById(R.id.regnum);
        mot = view.findViewById(R.id.motdays);

        // Setting the email TextView to display to user's email
        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        // Using bundle to get the Regestration Number from the home fragment
        Bundle bundle = this.getArguments();
        String data = bundle.getString("key").trim();
        String data1 = bundle.getString("key");
        // Setting the TextView to the Regestration Number
        textView.setText(data);
        // Calling the method passing the Regestration Number
        getVehicleMotData(data1);


        // Initialize Firebase Database reference and get the reference of the database where it says users
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        // Intializing a listener for the Button to perform action when clicked
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtaining the miles from EditText from user
                String miles = editText.getText().toString();
                //Converting into Integer
                int intMiles = Integer.parseInt(miles);

                // Defining when service is due for the vehicle based on the mileage entered by user
                int service = 10000 + intMiles;

                // Check if the registration number is not empty
                if ((!TextUtils.isEmpty(data)) && (!TextUtils.isEmpty(miles))) {
                    // Get the current user's ID
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    // Create a unique key for the new data entry
                    String entryId = databaseReference.child(userId).push().getKey();
                    // Creating HashMap to store the data to be inserted into database
                    Map<String, Object> datavehicle = new HashMap<>();
                    datavehicle.put("serviceDue", service);
                    datavehicle.put("motData", motday);
                    datavehicle.put("roadtaxData", Roadtaxday);
                    // Write the data to the database under the user's node
                    databaseReference.child(userId).child("Vehicle").child(data).setValue(datavehicle);
                    //show a toast to show if data added successfully
                    Toast.makeText(getContext(), "Vehicle data added successfully", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new Profile()).commit();
                } else {
                    Toast.makeText(getContext(), "Please enter a vehicle registration number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
    private void getVehicleMotData(String reg) {
        // Creating a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with the user's input (registration number) and the API key and using the package MotHistoryAndTaxStatusData
                    URL url = new URL("https://uk1.ukvehicledata.co.uk/api/datapackage/MotHistoryAndTaxStatusData?v=2&api_nullitems=1&auth_apikey=6837d0da-4635-4667-8034-e7a44aa9a610&key_VRM="+reg);
                    // Connecting to http and sending request using the URL
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");


                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Creating Buffer to obtain the data
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        // Parse JSON response to obtain the required data and define the variables.
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject dataItems = jsonObject.getJSONObject("Response").getJSONObject("DataItems");
                        motday = dataItems.getJSONObject("VehicleStatus").getString("DaysUntilNextMotIsDue");
                        Roadtaxday = dataItems.getJSONObject("VehicleStatus").getJSONObject("MotVed").getString("VedDaysRemaining");




                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Setting the TextViews from the data obtained using the API
                                mot.setText("Remaining Days of Mot: " + motday);
                                roadtax.setText("Remaing Days of RoadTax: " + Roadtaxday);

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