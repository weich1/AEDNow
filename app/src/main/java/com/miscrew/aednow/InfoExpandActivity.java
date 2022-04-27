package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.view.PreviewView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.HashMap;

public class InfoExpandActivity extends AppCompatActivity {
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_expand);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Bundle extras = getIntent().getExtras();
        if (extras == null || account == null) { // no account or lat or lng so why are we here?
            onBackPressed();
        }
        configureToolbar();
        Gson gson = new Gson();
        MapData ob = gson.fromJson(getIntent().getStringExtra("mapdata"), MapData.class);

        database = FirebaseDatabase.getInstance();
        EditText edtLatitude = (EditText) findViewById(R.id.edtLatitude);
        EditText edtLongitude = (EditText) findViewById(R.id.edtLongitude);
        EditText edtNotes = (EditText) findViewById(R.id.edtNotes);
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        edtLatitude.setText(Double.toString(ob.getLat()));
        edtLongitude.setText(Double.toString(ob.getLng()));
        btnSubmit.setOnClickListener(view -> {
            if(account!=null) {
                btnSubmit.setEnabled(false);
                View current = getCurrentFocus();
                if (current != null) current.clearFocus();
                HashMap<String, Object> AEDData = new HashMap<>();
                AEDData.put("email", account.getEmail());
                AEDData.put("username", account.getDisplayName());
                AEDData.put("md", ob);
                AEDData.put("notes", edtNotes.getText().toString());
                String refx = account.getDisplayName();
                //Double.toString(coords.latitude) + ":" + Double.toString(coords.latitude);
                DatabaseReference dbRef = database.getReference();
                dbRef.child("AEDLocRequest")
                        .child(refx)
                        .setValue(AEDData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Snackbar.make(findViewById(R.id.InfoExpandCoordinator), "Data write successful", Snackbar.LENGTH_SHORT)
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
                                Snackbar.make(findViewById(R.id.InfoExpandCoordinator), "Data write failed", Snackbar.LENGTH_SHORT)
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
        toolbar.setTitle("AED Info");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        return toolbar;
    }


}