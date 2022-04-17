package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.*;
import com.miscrew.aednow.databinding.ActivityMapsBinding;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class MapsActivity extends AppCompatActivity implements /*GoogleMap.OnMarkerClickListener,*/ OnMapReadyCallback {

    private GoogleMap mMap;
    GoogleSignInClient gsc;
    //SignInButton btnSignIn;
    Button btnSignOut;
    GoogleSignInAccount account;
    public Mapper md;
    public Marker marker;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configureToolbar();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // begin google sign-in process
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        // btnSignIn = (SignInButton) findViewById(R.id.google_sign_in);
        btnSignOut = (Button) findViewById(R.id.google_sign_out);
        account = GoogleSignIn.getLastSignedInAccount(this);
        changeButtonText();
        btnSignOut.setOnClickListener(view -> {
            if(account==null) {
                SignIn();
            } else {
                SignOut();
            }
        });

    }


    // change button text to reflect sign-in status
    private void changeButtonText() {
        //account = GoogleSignIn.getLastSignedInAccount(this);
        if(!isLoggedIn()) {
            btnSignOut.setText("Sign in via Google");
        } else {
            Toast.makeText(this, "Successfully signed in as user " + account.getDisplayName() + ".", Toast.LENGTH_SHORT).show();
            btnSignOut.setText("Sign Out");
        }
    }

    private Toolbar configureToolbar() {
        // create toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("AEDNow");
        //getSupportActionBar().setTitle("ADENow")
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        return toolbar;
    }

    private Boolean isLoggedIn() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        return (account != null);
    }

    // sign out code
    private void SignOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                changeButtonText();
            }
        });
    }
    // end sign out code

    // sign in code
    private void SignIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100) {
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                changeButtonText();
            } catch (ApiException e) {
                Toast.makeText(this, "Error signing in", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // end sign in code

    // toolbar creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_preferences:
                //Start Activity here
                Intent mIntent = new Intent(this, HomeActivity.class);
                startActivity(mIntent);
                break;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // enable zoom and location controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // read in JSON map markers
        readJSONMap();

        LatLng marker = new LatLng(41.62017922107947, -93.60208950567639);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        // zoom in
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));

        //googleMap.setOnMarkerClickListener(this);
        CustomMarkerWindow infoWin = new CustomMarkerWindow(this, md);
        mMap.setInfoWindowAdapter(infoWin);
        mMap.setOnInfoWindowClickListener(infoWin);
    }



    /*
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }

        // Return false for default behavior
        return false;
    }*/

    private void readJSONMap() {
        // create Gson instance for JSON read
        Gson gson = new Gson();
        try {
            // create a reader to read our JSON file
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("map.json")));
            // read JSON map location data from file into our mapper class
            md = gson.fromJson(reader, Mapper.class);

            for(MapData x: md.mapData) {
            //for(int y=0; y < md.mapData.size(); y++) {
                //MapData x = md.mapData.get(y);
                // create a new location marker for each entry
                LatLng marker = new LatLng(x.getLat(), x.getLng());
                // set lat+lng+title+description+icon
                MarkerOptions moMarker = new MarkerOptions();
                // load marker info
                moMarker.position(marker).title(x.getTitle()).snippet(x.getDescription());
                // load custom icon if specified
                if (x.getIcon() != 0) // null check
                    moMarker.icon(BitmapFromVector(getApplicationContext(), x.getIcon()));
                //System.out.println(R.drawable.ic_baseline_not_listed_location_24);
                // add the marker to mMap and set it to x
                x.setMarker(mMap.addMarker(moMarker));
            }
            //}
            // close file
            reader.close();
        } catch (Exception ex) {
            System.out.println("Error reading JSON file");
            Toast.makeText(this, "Error reading map markers from JSON file", Toast.LENGTH_SHORT).show();
            // print exception to logcat
            ex.printStackTrace();
        }
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

   /*private String LoadIcon(Context context, String path) {
        ImageView imgv = new ImageView(context);
        Picasso.get().load("").into(imgv);
        imgv.to
    }*/
}