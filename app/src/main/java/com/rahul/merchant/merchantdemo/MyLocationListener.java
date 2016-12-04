package com.rahul.merchant.merchantdemo;

import android.location.Location;
import android.os.Bundle;

/**
 * Created by root on 11/22/16.
 */

public abstract class MyLocationListener implements android.location.LocationListener {

    public abstract void onLocationFound(double latitude, double longitude);

    @Override
    public void onLocationChanged(Location location) {
        onLocationFound(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
