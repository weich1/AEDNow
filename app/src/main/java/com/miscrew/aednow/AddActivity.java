package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddActivity extends AppCompatActivity {
    FirebaseDatabase database;
    LatLng coords;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Bundle extras = getIntent().getExtras();
        if (extras == null || account == null) { // no account or lat or lng so why are we here?
            onBackPressed();
        }
        database = FirebaseDatabase.getInstance();
        configureToolbar();
        EditText edtLatitude = (EditText) findViewById(R.id.edtLatitude);
        EditText edtLongitude = (EditText) findViewById(R.id.edtLongitude);
        EditText edtNotes = (EditText) findViewById(R.id.edtNotes);
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        coords = new LatLng(extras.getDouble("lat"), extras.getDouble("lng"));
        edtLatitude.setText(Double.toString(coords.latitude));
        edtLongitude.setText(Double.toString(coords.longitude));
        btnSubmit.setOnClickListener(view -> {
            if(account!=null) {
                btnSubmit.setEnabled(false);
                View current = getCurrentFocus();
                if (current != null) current.clearFocus();
                HashMap<String, Object> AEDData = new HashMap<>();
                AEDData.put("email", account.getEmail());
                AEDData.put("username", account.getDisplayName());
                AEDData.put("pos", coords);
                AEDData.put("notes", edtNotes.getText().toString());
                String refx = account.getDisplayName();
                //Double.toString(coords.latitude) + ":" + Double.toString(coords.latitude);
                AEDData.put("ref", refx);
                DatabaseReference dbRef = database.getReference();
                dbRef.child("AEDLocRequest")
                        .child(refx)
                        .setValue(AEDData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Snackbar.make(findViewById(R.id.AddCoordinator), "Data write successful", Snackbar.LENGTH_SHORT)
                                        .addCallback(new Snackbar.Callback() {
                                            @Override
                                            public void onDismissed(Snackbar snackbar, int event) {
                                                super.onDismissed(snackbar, event);
                                                onBackPressed();
                                            }
                                        }).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(findViewById(R.id.AddCoordinator), "Data write failed", Snackbar.LENGTH_SHORT)
                                    .addCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar snackbar, int event) {
                                            super.onDismissed(snackbar, event);
                                            onBackPressed();
                                        }
                                }).show();
                            }
                        });
                //myRef.setValue("Test database read");
            }
        });

    }

    // toolbar creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    private Toolbar configureToolbar() {
        // create toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add AED location");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        return toolbar;
    }
}