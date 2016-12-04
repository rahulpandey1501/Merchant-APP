package com.rahul.merchant.merchantdemo;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by finomena on 11/6/16.
 */
@IgnoreExtraProperties
class DataModel {

    public String phone;
    public String coaching_name;
    public String city;
    public String faculty;
    public String no_of_students;
    public String fee;
    public String course;
    public String subjects;
    public String address;
    public String image;
    public double latitude;
    public double longitude;
    public GeoLocationModel geo_location;

    public DataModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public DataModel(String phone, String coaching_name, String address, String city, String faculty, String no_of_students, String fee, String course, String subjects, String image, double latitude, double longitude, GeoLocationModel geo_location) {
        this.phone = phone;
        this.coaching_name = coaching_name;
        this.address = address;
        this.city = city;
        this.faculty = faculty;
        this.no_of_students = no_of_students;
        this.fee = fee;
        this.course = course;
        this.subjects = subjects;
        this.latitude = latitude;
        this.image = image;
        this.longitude = longitude;
        this.geo_location = geo_location;
    }
}
