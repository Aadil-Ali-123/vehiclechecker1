package com.example.vehiclechecker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class Home extends Fragment {
    //Declaring TextViews and Buttons and EditText
    EditText editText;
    Button spec;
    Button runningCost;
    Button history;
    Button mot;
    Button peformance;
    Button monitor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Defining the TextViews, Buttons, EditText
        editText = view.findViewById(R.id.et_search);
        spec = view.findViewById(R.id.button);
        runningCost = view.findViewById(R.id.button2);
        history = view.findViewById(R.id.button3);
        mot = view.findViewById(R.id.button4);
        peformance = view.findViewById(R.id.button5);
        monitor = view.findViewById(R.id.button6);

        //Setting up on click listeners for the spec Button
        spec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Using Bundle to pass the data and open the Fragment
                Bundle bundle = new Bundle();
                bundle.putString("key",editText.getText().toString());
                Specification specification = new Specification();
                specification.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame, specification).commit();

            }
        });
        //Setting up on click listeners for the runningCost Button
        runningCost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Using Bundle to pass the data and open the Fragment
                Bundle bundle = new Bundle();
                bundle.putString("key",editText.getText().toString());
                RunningCost runningCost1 = new RunningCost();
                runningCost1.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame, runningCost1).commit();

            }
        });
        //Setting up on click listeners for the history Button
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Using Bundle to pass the data and open the Fragment
                Bundle bundle = new Bundle();
                bundle.putString("key",editText.getText().toString());
                History history1 = new History();
                history1.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame, history1).commit();

            }
        });
        //Setting up on click listeners for the mot Button
        mot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Using Bundle to pass the data and open the Fragment
                Bundle bundle = new Bundle();
                bundle.putString("key",editText.getText().toString());
                Mot mot1 = new Mot();
                mot1.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame, mot1).commit();

            }
        });
        //Setting up on click listeners for the performance Button
        peformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Using Bundle to pass the data and open the Fragment
                Bundle bundle = new Bundle();
                bundle.putString("key",editText.getText().toString());
                Performance perform = new Performance();
                perform.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame, perform).commit();

            }
        });
        //Setting up on click listeners for the monitor Button
        monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Using Bundle to pass the data and open the Fragment
                Bundle bundle = new Bundle();
                bundle.putString("key",editText.getText().toString());
                MonitorVehicle monitorVehicle = new MonitorVehicle();
                monitorVehicle.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame, monitorVehicle).commit();

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }
}