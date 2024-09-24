package com.example.vehiclechecker;

import static androidx.fragment.app.FragmentManager.TAG;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Profile extends Fragment {
    // Declaring TextView, FireBase Realtime Database, Buttons, ListView, Notification Variables
    TextView email;
    Button loginbtn;
    Button regbtn;
    Button logoutbtn;
    DatabaseReference databaseReference;
    ListView listView;
    private static final String CHANNEL_ID = "vehicle_monitoring_channel";
    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // When fragment is initialized call this method
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Check if Android version is API 24 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define name, Description and importance level for notification
            CharSequence name = "Vehicle Monitoring";
            String description = "Notifications for vehicle monitoring";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // Creating a notification Channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Get the system service for managing notification API
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            //Register the notification chanel with the system
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflating the layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Defining the TextViews and List View and Buttons
        email = view.findViewById(R.id.textView2);
        loginbtn = view.findViewById(R.id.login);
        logoutbtn = view.findViewById(R.id.logout);
        regbtn = view.findViewById(R.id.register);
        listView = view.findViewById(R.id.listview);


        // Set OnClickListener for logout button
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user
                FirebaseAuth.getInstance().signOut();
                // Finish the hosting Activity
                getActivity().finish();
            }
        });

        // Set OnClickListener for login button
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), login.class);
                startActivity(intent);
                // Finish the hosting Activity (if Profile is hosted by an Activity)
                getActivity().finish();
            }
        });

        // Set OnClickListener for register button
        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Register.class);
                startActivity(intent);
                // Finish the hosting Activity (if Profile is hosted by an Activity)
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check authentication status when Fragment starts
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is logged in display email to user and make visible the logout button and hide the other buttons.
            email.setText("Email: " + currentUser.getEmail());
            loginbtn.setVisibility(View.GONE);
            regbtn.setVisibility(View.GONE);
            logoutbtn.setVisibility(View.VISIBLE);
            databaseReference = FirebaseDatabase.getInstance().getReference("users");
            displayUserData();
        } else {
            // User is not logged in display message in textview and hide logout button and make the other buttons visible
            email.setText("You are not logged in, please login or register");
            loginbtn.setVisibility(View.VISIBLE);
            regbtn.setVisibility(View.VISIBLE);
            logoutbtn.setVisibility(View.GONE);
        }
    }

    private void displayUserData() {
        // Getting the userid from firebase Authentication
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Creating array lists
        List<String> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        // Getting the vehicle data that the user is monitoring and displaying to user and setting a listener to each item
        databaseReference.child(userID).child("Vehicle").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Looping through all the vehicles the user is monitoring
                for (DataSnapshot x : snapshot.getChildren()) {
                    String regnum = x.getKey();
                    String motData = x.child("motData").getValue(String.class);
                    String roadtaxData = x.child("roadtaxData").getValue(String.class);
                    String serviceDue = String.valueOf(x.child("serviceDue").getValue(Long.class));
                    // Converting to integer
                    int motDays = Integer.parseInt(motData);
                    int roadTaxDays = Integer.parseInt(roadtaxData);
                    // Checking if mot or roadtax is less than 14 days remaining then call the notification method.
                    if (motDays <= 14 || roadTaxDays <= 14) {
                        showNotification("Vehicle Expiry Alert", "Your vehicle's MOT or road tax is expiring soon!");
                    }
                    // Concatenate the strings above into 1 string, then add to the lists
                    String vehicleData = "Registration Number: " + regnum + "\n" +
                            "  - Days Remaining MOT: " + motData + "\n" +
                            "  - Days Remaining RoadTax: " + roadtaxData + "\n" +
                            "  - Service Due When Vehicle Mileage is: " + serviceDue;
                    list.add(vehicleData);
                    list2.add(regnum);
                    // Check if vehicle data is added to list
                    Log.d("ProfileCheck", "onDataChange: Added vehicle data to list: " + vehicleData);


                }
                // Setting up the ListView using Array Adapter passing the list of vehicles created above
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list);
                // Setting the adapter so displays to user
                listView.setAdapter(adapter);
                // Creating an listener on when the user clicks an element from the listView
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = (String) parent.getItemAtPosition(position); // Get selected item (vehicle data)

                        String[] lines = selectedItem.split("\n");
                        if (lines.length > 0) {
                            String regNum = lines[0].substring(lines[0].indexOf(":") + 1).trim();  // Extract registration number from the first line
                            showRemoveConfirmationDialog(regNum);
                        }


                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Checking if error what the error is
                Log.e("ProfileCheck", "onCancelled: Database error: " + error.getMessage());
            }
        });
    }
    // Showing the notificcation using the NotificationCompactAPI.
    private void showNotification(String title, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            // Request the permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, show the notification even when the app is not running
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                    // Setting the notification logo/icon
                    .setSmallIcon(R.drawable.noun_trouble_5870353)
                    //Setting the title of the notification
                    .setContentTitle(title)
                    //Seting the message to portray of the notification
                    .setContentText(content)
                    // Setting the piority level of the notification
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            notificationManager.notify(1, builder.build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                // Permission denied by the user
                Toast.makeText(getContext(), "Permission denied to show notifications", Toast.LENGTH_SHORT).show();
            } else {
                // Permission granted, show the notification
                showNotification("Notification Title", "Notification Content");
            }
        }
    }

    private void showRemoveConfirmationDialog(String regNum) {
        //Inflating the pop up layout giving the user options of 2 buttons to remove the vehicle or return back to the screen
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.remove_vehicle, null);
        builder.setView(dialogView);
        // Defining Text Views and Buttons
        TextView Title = dialogView.findViewById(R.id.title);
        Button btnCancel = dialogView.findViewById(R.id.cancel);
        Button btnRemove = dialogView.findViewById(R.id.remove);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set click listeners for buttons
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Dismiss the dialog
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the vehicle from the database

                Log.d("ProfileCheck", "Removing vehicle with regNum: " + regNum);
                // Call the method passing the Regestration Number
                removeVehicle(regNum);
                dialog.dismiss(); // Dismiss the dialog
            }
        });
    }

    private void removeVehicle(String regNum) {
        // Geting userid from firebase and the database data
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Getting the vehicle the user wants to remove
        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userID).child("Vehicle").child(regNum);
        Log.d("ProfileCheck", "Removing vehicle from database with regNum: " + regNum);

        // Remove the vehicle data from the database
        vehicleRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "Vehicle removed successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to remove vehicle", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
