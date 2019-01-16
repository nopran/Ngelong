package com.gmitmedia.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by melvin on 08/07/2017.
 */

public class GpsLocation {

    FragmentActivity activity;
    Location lastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    OnLocationListener onLocationListener;

    final int REQUEST_CODE = 511, GPS_ENABLE_REQUEST = 512;

    //translation
    String explanationTitle = "Current Location";
    String explanationDesc = "We would like to have your permission to show your current location";
    String explanationOK = "OK";
    String explanationCancel = "CANCEL";

    //translation
    String GPSDisabled_explanationTitle = "GPS Disabled";
    String GPSDisabled_explanationDesc = "Gps is disabled, in order to use the App properly you need to enable GPS of your device";
    String GPSDisabled_explanationOK = "Enable GPS";
    String GPSDisabled_explanationCancel = "CANCEL";

    //GETTERS and SETTERS for translation
    public void setExplanationTitle(String explanationTitle) {
        this.explanationTitle = explanationTitle;
    }

    public void setExplanationDesc(String explanationDesc) {
        this.explanationDesc = explanationDesc;
    }

    public void setExplanationOK(String explanationOK) {
        this.explanationOK = explanationOK;
    }

    public void setExplanationCancel(String explanationCancel) {
        this.explanationCancel = explanationCancel;
    }

    public GpsLocation(FragmentActivity activity) {
        this.activity = activity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public interface OnPermissionGrantedListener {
        void permissionGranted();

        void permissionDenied();
    }


    OnPermissionGrantedListener onPermissionGrantedListener;

    public interface OnLocationListener {
        void location(Location location, boolean shouldRefresh);
    }

    public void performIfPermissionGranted(OnPermissionGrantedListener onPermissionGrantedListener) {
        this.onPermissionGrantedListener = onPermissionGrantedListener;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
                Alert alert = new Alert();
                alert.DisplayText(explanationTitle, explanationDesc, explanationOK, explanationCancel, activity);
                alert.show(activity.getSupportFragmentManager(), explanationTitle);
                alert.setPositiveButtonListener(new Alert.PositiveButtonListener() {
                    @Override
                    public void onPositiveButton(String input) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                    }
                });
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
            onPermissionGrantedListener.permissionDenied();
        } else {
            onPermissionGrantedListener.permissionGranted();
        }
    }

    /**
     * Ask for user to turn on GPS
     * @param activity
     */
    public void showGPSDiabledDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(GPSDisabled_explanationTitle);
        builder.setMessage(GPSDisabled_explanationDesc);
        builder.setPositiveButton(GPSDisabled_explanationOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE_REQUEST);
            }
        }).setNegativeButton(GPSDisabled_explanationCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onLocationListener.location(null, (lastLocation == null));
            }
        });
        builder.show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GPS_ENABLE_REQUEST) {
            final LocationManager manager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (onLocationListener != null)
                    getCurrentLocation(onLocationListener);
            }else{
                onLocationListener.location(null, (lastLocation == null));
            }
        }
    }

    /**
     * Call this to share image when permission is granted.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Share.
                    if (onPermissionGrantedListener != null)
                        onPermissionGrantedListener.permissionGranted();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    if (onPermissionGrantedListener != null)
                        onPermissionGrantedListener.permissionDenied();
                }
            }

        }
    }


    public boolean hasPermission() {
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    public void getCurrentLocation(final OnLocationListener onLocationListener) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.onLocationListener = onLocationListener;
            final LocationManager manager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDiabledDialog(activity);
                return;
            }
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location == null) {
                                // Toast.makeText(activity, "Check your location provider", Toast.LENGTH_SHORT).show();
                                onLocationListener.location(location, (lastLocation == null));
                            } else {
                                onLocationListener.location(location, (lastLocation == null));
                                lastLocation = location;

                            }
                        }
                    });
        }
    }

//    public Location getCurrentLocation() {
//        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
//            String provider = locationManager.getBestProvider(new Criteria(), false);
//            Location location = locationManager.getLastKnownLocation(provider);
//            if (location == null) {
//                // Toast.makeText(activity, "Check your location provider", Toast.LENGTH_SHORT).show();
//            } else {
//                lastLocation = location;
//                return location;
//            }
//        }
//        return null;
//    }

    public float getDistanceToLocation(double gpsLat, double gpsLon) {
        float[] results = new float[1];
        if (lastLocation == null)
            return 0;
        Location.distanceBetween(gpsLat, gpsLon, lastLocation.getLatitude(), lastLocation.getLongitude(), results);

        return results[0];
    }
}
