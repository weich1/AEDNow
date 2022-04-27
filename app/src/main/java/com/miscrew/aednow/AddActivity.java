package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.UUID;

public class AddActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int CAMERA_REQUEST_CODE = 10;
    Vector<String> filePaths = new Vector<>();
    Vector<String> uploadPaths = new Vector<>();
    GoogleSignInAccount account;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseUser user;
    FirebaseAuth auth;
    StorageReference storageReference;
    PreviewView preView;
    ImageCapture imgCapture;
    EditText edtLatitude, edtLongitude, edtNotes;
    ImageView img1, img2, img3;
    Button btnSubmit;
    ImageView imgCap;
    Bitmap capturedImg;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    LatLng coords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        account = GoogleSignIn.getLastSignedInAccount(this);
        Bundle extras = getIntent().getExtras();
        if (extras == null || account == null) { // no account or lat or lng so why are we here?
            onBackPressed();
        }
        // get all firebase references
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storageReference = storage.getReference();
        if(user == null) {
            onBackPressed();
        }
        configureToolbar();
        edtLatitude = findViewById(R.id.edtLatitude);
        edtLongitude = findViewById(R.id.edtLongitude);
        edtNotes = findViewById(R.id.edtNotes);
        btnSubmit = findViewById(R.id.btnSubmit);
        img1 = findViewById(R.id.imageCap1);
        img2 = findViewById(R.id.imageCap2);
        img3 = findViewById(R.id.imageCap3);
        preView = findViewById(R.id.cameraView);
        initCamera();
        setClickListener(img1);
        setClickListener(img2);
        setClickListener(img3);
        Gson gson = new Gson();
        coords = gson.fromJson(getIntent().getStringExtra("coords"), LatLng.class);
        edtLatitude.setText(Double.toString(coords.latitude));
        edtLongitude.setText(Double.toString(coords.longitude));
        btnSubmit.setOnClickListener(view -> {
            if(account != null) {
                if(filePaths.size() != 0) {
                    doUpload();
                } else Snackbar.make(findViewById(R.id.AddCoordinator), "Please capture at least one image first", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void doUpload() {
        btnSubmit.setEnabled(false);
        View current = getCurrentFocus();
        if (current != null) current.clearFocus();
        uploadImage();
    }

    private void doSubmit () {
        HashMap<String, Object> AEDData = new HashMap<>();
        AEDData.put("email", account.getEmail());
        AEDData.put("username", account.getDisplayName());
        AEDData.put("pos", coords);
        System.out.println(uploadPaths.size());
        int counter=1;
        for (String url: uploadPaths ) {
            System.out.println(url);
            AEDData.put("imageUrl" + counter, url);
            counter++;
        }
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
                        Snackbar.make(findViewById(R.id.AddCoordinator), "Data write successful", Snackbar.LENGTH_SHORT)
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        super.onDismissed(snackbar, event);
                                        //onBackPressed();
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


    private void setClickListener (ImageView img) {
        img.setOnLongClickListener(view -> {
            savePhoto();
            takePhoto();
            imgCap = img;
            //unsetClickListener(img);
            return true;
        });
    }

    private void unsetClickListener (ImageView img) {
        img.setOnLongClickListener(null);
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
        toolbar.setTitle("Add AED location");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        return toolbar;
    }


    private void initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                if (hasPermission(Manifest.permission.CAMERA) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    startCameraX(cameraProvider);
                } else {
                    requestPermission(CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(preView.getSurfaceProvider());

        imgCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imgCapture);
    }


    public void takePhoto() {
        imgCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                capturedImg = getBitmap(imageProxy);
                imgCap.setImageBitmap(capturedImg);
                imageProxy.close();
                System.out.println("Image successfully captured.");
            }

            @Override
            public void onError(ImageCaptureException e) {
                capturedImg = null;
                System.out.println("Image capture failed.");
            }
        });
    }

    private void savePhoto() {
        long timestamp = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        imgCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Snackbar.make(findViewById(R.id.AddCoordinator), "Photo saved successfully at  " + outputFileResults.getSavedUri(), Snackbar.LENGTH_SHORT).show();
                        System.out.println("Photo saved successfully at" + outputFileResults.getSavedUri());
                        filePaths.add(outputFileResults.getSavedUri().toString());
                        // content://media/external/images/media/87
                        // load image into imgCap here
                    }


                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Snackbar.make(findViewById(R.id.AddCoordinator), "Error saving photo" + exception.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String[] permission, int permissionCode) {
        ActivityCompat.requestPermissions(
                this,
                permission,
                permissionCode
        );
    }

    private Bitmap getBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }


    // UploadImage method
    private void uploadImage()
    {
        if (!filePaths.isEmpty()) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();


            // adding listeners on upload
            // or failure of image
            for(String f: filePaths) {
                // Defining the child of storageReference
                StorageReference ref
                        = storageReference
                        .child(
                                "AEDLocRequests/images/"
                                        + UUID.randomUUID().toString());
                // Progress Listener for loading
// percentage on the dialog box
                ref.putFile(Uri.parse(f))
                        .addOnSuccessListener(ContextCompat.getMainExecutor(this),
                                taskSnapshot -> {
                                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                                            String generatedFilePath = task.getResult().toString();
                                            System.out.println("## Stored path is "+generatedFilePath);
                                            uploadPaths.add(generatedFilePath);
                                            filePaths.remove(f);
                                            if(filePaths.size() == 0) doSubmit();
                                    });
                                    progressDialog.dismiss();
                                    Toast.makeText(AddActivity.this,"Image Uploaded!!",Toast.LENGTH_SHORT).show();
                                })
                        .addOnFailureListener(ContextCompat.getMainExecutor(this),
                                e -> {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(AddActivity.this,"Failed " + e.getMessage(),Toast.LENGTH_SHORT).show();
                        })
                        .addOnProgressListener(ContextCompat.getMainExecutor(this),
                                taskSnapshot -> {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                });
            }
        }
    }


}