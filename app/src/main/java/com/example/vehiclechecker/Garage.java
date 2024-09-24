package com.example.vehiclechecker;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationRequest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Garage extends Fragment {
    //Declaring the Textviews, client, api, smf
    SupportMapFragment smf;
    FusedLocationProviderClient client;
    GoogleMap googleMap;
    TextView garage1;
    TextView garage2;
    TextView garage3;
    TextView garage4;
    TextView garage5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialising the layout
        View view = inflater.inflate(R.layout.fragment_garage, container, false);
        //Defininfing the declariations for TextViews, SMF, CLient
        smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googlemap);
        client = LocationServices.getFusedLocationProviderClient(requireActivity());
        garage1 = view.findViewById(R.id.garage1);
        garage2 = view.findViewById(R.id.garage2);
        garage3 = view.findViewById(R.id.garage3);
        garage4 = view.findViewById(R.id.garage4);

        // Request location permission
        requestLocationPermission();

        return view;
    }

    private void requestLocationPermission() {
        Dexter.withContext(requireContext().getApplicationContext())
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        // Calling method if permission granted
                        getMyLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        // Handle permission denied
                        Log.d("Permission Denied", "onPermissionDenied: ");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        // Canceling the Request
                        permissionToken.cancelPermissionRequest();
                    }
                }).check();
    }

    private void getMyLocation() {
        if (isLocationPermissionGranted()) {
            // Obtaining the User's Location
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        smf.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull GoogleMap googleMap) {
                                // Obtaining the longitude and latitude of users location
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                // Declaring a marker and portraying the location of the user to the layout
                                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Your Location");
                                googleMap.addMarker(markerOptions);
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                                //Calling the method to obtain nearest garages
                                findNearestGarages(latLng);
                            }
                        });
                    }
                }
            });
        }
    }

    private void findNearestGarages(LatLng latLng) {
        int radius = 5000; // 5 kilometers

        // Build the Places API request
        String type = "car_repair"; // Type of place to search
        String apiKey = getString(R.string.api_key); // getiing the api key
        //Setting up the URL using places API to get nearest garages
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + latLng.latitude + "," + latLng.longitude +
                "&radius=" + radius +
                "&type=" + type +
                "&key=" + apiKey;
        // Using Json to request the data using the url
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < Math.min(results.length(), 5); i++) {
                                // Obtaining the garage name
                                JSONObject garage = results.getJSONObject(i);
                                String name = garage.getString("name");

                                // Update the TextViews with garage names
                                switch (i) {
                                    case 0:
                                        garage1.setText(name);
                                        break;
                                    case 1:
                                        garage2.setText(name);
                                        break;
                                    case 2:
                                        garage3.setText(name);
                                        break;
                                    case 3:
                                        garage4.setText(name);
                                        break;
                                    
                                    default:
                                        break;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        // Add the request to the RequestQueue (Volley)
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }


    private boolean isLocationPermissionGranted() {
        //Checking if user grants location permission
        return ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}

