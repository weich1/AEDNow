package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.view.PreviewView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.icu.text.IDNA;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class InfoExpandActivity extends AppCompatActivity {
    FirebaseDatabase database;

    EditText edtLatitude;
    EditText edtLongitude;
    EditText edtNotes;
    ViewGroup layoutImages;
    Button btnSubmit;

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
        edtLatitude =  findViewById(R.id.edtLatitude);
        edtLongitude = findViewById(R.id.edtLongitude);
        edtNotes = findViewById(R.id.edtNotes);
        //btnSubmit = findViewById(R.id.btnSubmit);
        Gson gson = new Gson();
        MapData ob = gson.fromJson(getIntent().getStringExtra("mapdata"), MapData.class);
        database = FirebaseDatabase.getInstance();

        ViewGroup layoutImages = findViewById(R.id.layoutImages);
        for(String imgUrl: ob.getImages()) {
            ImageView imgView = new ImageView(getApplicationContext());
            imgView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 400));
            imgView.setEnabled(true);
            loadImage(imgView, imgUrl);
            imgView.setOnLongClickListener(view -> {
                imageExpand(imgView);
                return true;
            });
            layoutImages.addView(imgView);
        }
        edtLatitude.setText(Double.toString(ob.getLat()));
        edtLongitude.setText(Double.toString(ob.getLng()));


        /*btnSubmit.setOnClickListener(view -> {
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
        });*/

    }

    @SuppressLint("ResourceType")
    private void imageExpand(ImageView src) {
        System.out.println("IMAGEVIEW");
        LayoutInflater inflater = (LayoutInflater) InfoExpandActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vs  = inflater.inflate(R.layout.layout_fullscreen_image, (ViewGroup)findViewById(R.id.layoutImages), false);
        // create the popup window
        int width = FrameLayout.LayoutParams.WRAP_CONTENT;
        int height = FrameLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(vs, width, height, true);

        ImageView ss = vs.findViewById(R.id.imgFs);
        ss.setImageDrawable(src.getDrawable());
        popupWindow.showAtLocation(src, Gravity.TOP, 0, 0);
        // dismiss the popup window when touched
        vs.setOnClickListener((view) -> {
            popupWindow.dismiss();
            //return true;
        });

    }

    private void loadImage(ImageView img, String url) {
        if(url.length() == 0) return;
        Picasso.get()
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .into(img);
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("AED Info");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        return toolbar;
    }


}