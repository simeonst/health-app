package com.healthapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class GPStracker implements LocationListener {
    private Context context;

    public GPStracker(Context c) {
        this.context = c;
    }

    public Location getLocation() {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, R.string.gps_permission_denied, Toast.LENGTH_SHORT).show();
            return null;
        }
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            // TODO change minTime or minDistance to better suit use
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 100, this);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return location;
        } else {
            Toast.makeText(context,R.string.gps_permission_prompt, Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
