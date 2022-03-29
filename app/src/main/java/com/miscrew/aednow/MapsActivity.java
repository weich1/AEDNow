package com.miscrew.aednow;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.*;
import com.miscrew.aednow.databinding.ActivityMapsBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Mapper md;
    private ActivityMapsBinding binding;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //String js = gson.fromJson()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // enable zoom and location controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // create Gson instance for JSON read
        Gson gson = new Gson();
        MarkerOptions moMarker = new MarkerOptions();

        try {
            // create a reader to read our JSON file
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("map.json")));
            // read JSON map location data from file into our mapper class
            Mapper md = gson.fromJson(reader, Mapper.class);

            for(MapData x: md.mapData) {
                // create a new location marker for each entry
                LatLng marker = new LatLng(x.getLat(), x.getLng());
                // set lat+lng+title+description
                moMarker.position(marker).title(x.getTitle()).snippet(x.getDescription());
                // add the marker to mMap
                mMap.addMarker(moMarker);
            }
            // close file
            reader.close();
        } catch (Exception ex) {
            System.out.println("Error reading JSON file");
            // print exception to logcat
            ex.printStackTrace();
        }
        // Grand View Marker
        LatLng marker = new LatLng(41.62017922107947, -93.60208950567639);
        moMarker.position(marker).title("Grandview University").snippet("Krumm Business Center");
        mMap.addMarker(moMarker);
        // move to grand view marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        // zoom in
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
    }


}