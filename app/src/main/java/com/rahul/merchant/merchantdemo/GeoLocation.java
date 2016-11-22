package com.rahul.merchant.merchantdemo;

/**
 * Created by root on 11/22/16.
 */


public class GeoLocation {

    public double latitude;
    public double longitude;
    public String address;
    public String city;
    public String state;
    public String postal_code;
    public String known_name;

    public GeoLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public GeoLocation(double latitude, double longitude, String address, String city, String state, String postal_code, String known_name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postal_code = postal_code;
        this.known_name = known_name;
    }
}
