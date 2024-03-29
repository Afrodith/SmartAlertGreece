package com.kospeac.smartgreecealert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*
*  H  LocationService επιστρεφει τις συντεταγμενες της συσκευης του χρηστη με την χρηση του gps
*  */
public class LocationService implements LocationListener {
static double latitude;
static double longitude;


    @Override
    public void onLocationChanged(Location location) {

        if(location==null){

        }else {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        }

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




    /*
    * getDistance
    *  Υπολογιζει με βαση τις συντεταγμενες εισοδου αμα η αποσταση σε μετρα ειναι
    *   μικροτερη η ιση με 5000 μετρα (5χμλ)
    *  */
    public static boolean getDistance(Double lat1, Double lon1, Double lat2, Double lon2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        float distanceInMeters = loc1.distanceTo(loc2);

        System.out.println(distanceInMeters);
        if(distanceInMeters <= 5000){ // Αν η αποσταση ειναι μικροτερη απο 5χλ
            return true;
        }else {
            return false;
        }

    }
}
