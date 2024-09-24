package com.example.vehiclechecker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Fuel extends Fragment {

    // Declaring the APIS, CLIENT AND TEXTVIEWS
    SupportMapFragment smf;
    FusedLocationProviderClient client;
    GoogleMap googleMap;
    TextView fuel1;
    TextView fuel2;
    TextView fuel3;
    TextView fuel4;
    TextView garage5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflating the layout
        View view = inflater.inflate(R.layout.fragment_fuel, container, false);
        //Finding the smf
        smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googlemap);
        // initalizing the fusedlocationproiderclient
        client = LocationServices.getFusedLocationProviderClient(requireActivity());
        //finding the textViews
        fuel1 = view.findViewById(R.id.fuel1);
        fuel2 = view.findViewById(R.id.fuel2);
        fuel3 = view.findViewById(R.id.fuel3);
        fuel4 = view.findViewById(R.id.fuel4);

        // Request location permission
        requestLocationPermission();

        return view;
    }

    private void requestLocationPermission() {
        Dexter.withContext(requireContext().getApplicationContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //Checking if Permission granted and calling the getMyLocation Method.
                        getMyLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        //Handling if permission denied
                        Log.d("Permission Denied", "onPermissionDenied: ");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        //Canceling permission request
                        permissionToken.cancelPermissionRequest();
                    }
                }).check();
    }

    private void getMyLocation() {
        if (isLocationPermissionGranted()) {
            // Getting the location of the user
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        smf.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull GoogleMap googleMap) {
                                // Obtaining the Longitude and Latitude
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                // Portratying the location to the user using a Marker
                                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Your Location");
                                googleMap.addMarker(markerOptions);
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                                // Calling the method and passing the latlng to the method
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
        String type = "gas_station"; // Type of place to search
        String apiKey = getString(R.string.api_key); // ApiKey
        //creating the url to use the Places API to find nearby garages
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + latLng.latitude + "," + latLng.longitude +
                "&radius=" + radius +
                "&type=" + type +
                "&key=" + apiKey;

        // Sending a request via JSON using the URL to retrive nearby garages
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < Math.min(results.length(), 5); i++) {
                                // Obtaining the name of nearby garage
                                JSONObject garage = results.getJSONObject(i);
                                String name = garage.getString("name");

                                // Update the TextViews with garage names
                                switch (i) {
                                    case 0:
                                        fuel1.setText(name);
                                        break;
                                    case 1:
                                        fuel2.setText(name);
                                        break;
                                    case 2:
                                        fuel3.setText(name);
                                        break;
                                    case 3:
                                        fuel4.setText(name);
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

        // Adding the request to the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }


    private boolean isLocationPermissionGranted() {
        //Checking if user grants location permission
        return ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}