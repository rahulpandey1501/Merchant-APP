package com.rahul.merchant.merchantdemo;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
    private DatabaseReference mDatabase;
    private EditText coachingName, address, phone, city, faculty, noOfStudents, fee, courses, subjects;
    private Button submit, clear;
    private LinearLayout rootLayout;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private String provider;
    final int MANDATORY = 1;
    private double latitude, longitude;
    private ProgressBar progressBar;
    private String cityString, stateString, postalCodeString, knownNameString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setMandatoryTag();
        createDatabase();
        setListeners();
        checkLocationPermission();
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
        provider = locationManager.getBestProvider(criteria, false);
        if (provider == null)
            return;
        locationListener = new MyLocationListener() {
            @Override
            public void onLocationFound(double latitude, double longitude) {
                MainActivity.this.latitude = latitude;
                MainActivity.this.longitude = longitude;
                log(TAG, "lat: "+latitude+"  long:"+longitude);
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

//        Location location = locationManager.getLastKnownLocation(provider);
//        locationManager.requestLocationUpdates(provider, 400, 1, this);
//        if (location != null) {
//            log(TAG, "Provider " + provider + " has been selected.");
//            onLocationChanged(location);
//        } else {
//            log(TAG, "Location not available");
//        }
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
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                DataModel value = dataSnapshot.getValue(DataModel.class);
                log(TAG, "Value is: ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValidation(rootLayout)) return;
                if (latitude > 0 && longitude > 0)
                    sendData();
                else
                    showProgress();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData(rootLayout);
            }
        });
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
                getTextFromView(faculty), getTextFromView(noOfStudents), getTextFromView(fee), getTextFromView(courses), getTextFromView(subjects), latitude, longitude, getLocationByGeocoder());
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

//    @Override
//    public void onLocationChanged(Location location) {
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//        log(TAG, "lat: "+latitude+"  long:"+longitude);
//        if (progressBar.getVisibility() == View.VISIBLE) {
//            hideProgress();
//            sendData();
//        }
//    }
//
//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//
//    }
//
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                        checkLocationPermission();
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
            checkLocationPermission();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
