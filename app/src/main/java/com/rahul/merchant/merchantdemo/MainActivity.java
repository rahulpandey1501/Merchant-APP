package com.rahul.merchant.merchantdemo;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.rahul.merchant.merchantdemo.Utility.checkForGPSEnable;
import static com.rahul.merchant.merchantdemo.Utility.checkValidation;
import static com.rahul.merchant.merchantdemo.Utility.getTextFromView;
import static com.rahul.merchant.merchantdemo.Utility.log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION_SETTING = 2;
    private static final int CAMERA_REQUEST_CODE = 100;
    private DatabaseReference mDatabase;
    private EditText coachingName, address, phone, city, faculty, noOfStudents, fee, courses, subjects;
    private View takePhoto;
    private Button submit, clear;
    private LinearLayout rootLayout;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    final int MANDATORY = 1;
    private double latitude, longitude;
    private ProgressBar progressBar;
    private File imageFile;
    private boolean imageCaptured;
    private Uri firebaseImageStorageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setMandatoryTag();
        createDatabase();
        setListeners();
        checkPermissions();
    }

    private void bindViews() {
        rootLayout = (LinearLayout) findViewById(R.id.root_layout);
        coachingName = (EditText) findViewById(R.id.coaching_name_ET);
        address = (EditText) findViewById(R.id.address_ET);
        phone = (EditText) findViewById(R.id.phone_ET);
        city = (EditText) findViewById(R.id.city_ET);
        faculty = (EditText) findViewById(R.id.faculty_name_ET);
        noOfStudents = (EditText) findViewById(R.id.no_of_student_ET);
        fee = (EditText) findViewById(R.id.fee_ET);
        courses = (EditText) findViewById(R.id.course_ET);
        subjects = (EditText) findViewById(R.id.subject_ET);
        submit = (Button) findViewById(R.id.submit_BTN);
        clear = (Button) findViewById(R.id.clear_BTN);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        takePhoto = findViewById(R.id.take_photo);
    }

    private void setMandatoryTag() {
        coachingName.setTag(MANDATORY);
        address.setTag(MANDATORY);
        phone.setTag(MANDATORY);
        city.setTag(MANDATORY);
        faculty.setTag(MANDATORY);
        courses.setTag(MANDATORY);
        subjects.setTag(MANDATORY);
        fee.setTag(MANDATORY);
    }

    private void getLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, false);
        if (provider == null)
            return;
        locationListener = new MyLocationListener() {
            @Override
            public void onLocationFound(double latitude, double longitude) {
                MainActivity.this.latitude = latitude;
                MainActivity.this.longitude = longitude;
                if (progressBar.getVisibility() == View.VISIBLE) {
                    hideProgress();
                    sendData();
                }
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        Location oldLocation = locationManager.getLastKnownLocation(provider);
        if (oldLocation != null)  {
            locationListener.onLocationFound(latitude, longitude);
            Log.v(TAG, "Got Old location");
        }
    }

    private GeoLocationModel getLocationByGeocoder() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String addressLine2 = addresses.get(0).getMaxAddressLineIndex() > 0? addresses.get(0).getAddressLine(1): null;
            return new GeoLocationModel(latitude, longitude, addresses.get(0).getAddressLine(0), addressLine2, addresses.get(0).getLocality(), addresses.get(0).getAdminArea(),
                    addresses.get(0).getPostalCode(), addresses.get(0).getFeatureName(), addresses.get(0).getPhone(), addresses.get(0).getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setListeners() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                log(TAG, "Value is: ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage(coachingName.getText().toString()+".jpeg");
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValidation(rootLayout)) {
                    return;
                }
                if (!imageCaptured) {
                        Toast.makeText(getApplicationContext(), "Please take a picture", Toast.LENGTH_SHORT).show();
                }
                else {
                    startUpload(Uri.fromFile(ImageCompression.compressImage(imageFile.getAbsolutePath(), 95, 0)));
                }

            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData(rootLayout);
            }
        });
    }

    private void checkForLocationRetrieve() {
        if (latitude > 0 && longitude > 0) {
            sendData();
        }
        else {
            showProgress();
        }
    }

    private void captureImage(String fileName) {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/merchantApp/";
        File newDir = new File(dir);
        newDir.mkdirs();
        imageFile = new File(dir + fileName);
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri outputFileUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", imageFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void showProgress() {
        submit.setVisibility(View.GONE);
        clear.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        submit.setVisibility(View.VISIBLE);
        clear.setVisibility(View.VISIBLE);
    }

    private void sendData() {
        DataModel model = new DataModel(getTextFromView(phone), Utility.getTextFromView(coachingName), getTextFromView(address), getTextFromView(city),
                getTextFromView(faculty), getTextFromView(noOfStudents), getTextFromView(fee), getTextFromView(courses), getTextFromView(subjects), firebaseImageStorageUri.toString(), latitude, longitude, getLocationByGeocoder());
        String key = mDatabase.push().getKey();
        mDatabase.child(key).setValue(model);
        showSnackBar();
    }

    private void clearData(ViewGroup viewGroup) {
        coachingName.setText("");
        address.setText("");
        phone.setText("");
        city.setText("");
        faculty.setText("");
        courses.setText("");
        subjects.setText("");
        fee.setText("");
        noOfStudents.setText("");
    }

    private void showSnackBar() {
        Snackbar snackbar = Snackbar
                .make(rootLayout, "Updated Successfully", Snackbar.LENGTH_LONG);
        imageCaptured = false;
        snackbar.show();
        clearData(rootLayout);
        coachingName.requestFocus();
    }

    private void createDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkForGPSEnable(MainActivity.this)) {
            getLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            locationManager.removeUpdates(locationListener);
        }catch (Exception ignore) {}
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1);
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return;
            }
        }
        getLocation();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (permissions.length > 0) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!showRationale) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        Toast.makeText(getApplicationContext(), "Please enable location permission", Toast.LENGTH_SHORT).show();
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                    if (!(grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        checkPermissions();
                    } else {
                        getLocation();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            checkPermissions();
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    imageCaptured = true;
                    Toast.makeText(getApplicationContext(), "Image successfully captured", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Image not captured try again", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startUpload(Uri uri) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setIndeterminate(false);
        progressDialog.setTitle("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        new FileStorage(this, imageFile.getName()) {
            @Override
            protected void onProgress(int progress) {
                progressDialog.setProgress(progress);
                log(progress+"%");
            }

            @Override
            protected void onSuccess(Uri uri) {
                progressDialog.dismiss();
                firebaseImageStorageUri = uri;
                checkForLocationRetrieve();
            }

            @Override
            protected void onFailure() {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Upload fail please try again", Toast.LENGTH_SHORT).show();
            }
        }.startUpload(uri);
    }
}
