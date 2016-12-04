package com.rahul.merchant.merchantdemo;

import android.location.Address;

/**
 * Created by root on 11/22/16.
 */


public class GeoLocationModel {

    public double latitude;
    public double longitude;
    public String address_line1;
    public String address_line2;
    public String city;
    public String state;
    public String postal_code;
    public String known_name;
    public String phone;
    public String url;

    public GeoLocationModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public GeoLocationModel(double latitude, double longitude, String address_line1, String address_line2, String city, String state, String postal_code, String known_name, String phone, String url) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address_line1 = address_line1;
        this.address_line2 = address_line2;
        this.city = city;
        this.state = state;
        this.postal_code = postal_code;
        this.known_name = known_name;
        this.phone = phone;
        this.url = url;
    }
}
