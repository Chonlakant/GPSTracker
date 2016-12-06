package pwr.chojnacki.robert.gpstracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;

import pwr.chojnacki.robert.gpstracker.TrackingDatabase.*;

public class TrackingService extends Service implements LocationListener {
    private static final int MIN_DISTANCE_DIFFERENCE = 10; // 10 meters
    private static final int INTERVAL = 1000 * 15 * 1; // 1 minute
    private static TrackingDatabase db;
    private final Context context;
    private Location location;
    private double latitude;
    private double longitude;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean isWorking = false;

    protected LocationManager locationManager;

    public TrackingService(Context c) {
        super();
        this.context = c;
        db.init(c);
        startService();
    }

    public void startService() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                // Get location manager
                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                Log.d("TrackingService", "Available providers: " + locationManager.getAllProviders().toString());
                // Get GPS provider status
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // Get network provider status
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (isGPSEnabled) {
                    this.isWorking = true;
                    locationManager.requestLocationUpdates(
                            locationManager.GPS_PROVIDER,
                            INTERVAL,
                            MIN_DISTANCE_DIFFERENCE,
                            this);
                    Log.d("TrackingService", "Using GPS provider");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
                else if (isNetworkEnabled) {
                    this.isWorking = true;
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            INTERVAL,
                            MIN_DISTANCE_DIFFERENCE, this);
                    Log.d("TrackingService", "Using network provider");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        } catch(Exception e) {
            isWorking = false;
            location = null;
            Log.d("TrackingService", "Cannot start the service");
        }
    }

    public void stopService() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager != null) {
                    isWorking = false;
                    locationManager.removeUpdates(TrackingService.this);
                }
            }
        } catch(Exception e) {
            isWorking = true;
            Log.d("Tracking service", "Cannot stop the service");
        }

    }

    public Location getLocation() {
        return location;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // on pressing settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public boolean isWorking() {
        return isWorking;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        db.insert(location.getLatitude(), location.getLongitude());
        Log.d("TrackingService", "Received location update");
        //ArrayList<TrackingRecordClass> result = db.select();
        //Log.d("TrackingService", "Record " + result.get(0).id);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String s = "UNKNOWN";
        switch(status) {
            case 0:
                s = "OUT_OF_SERVICE";
                break;
            case 1:
                s = "TEMPORARILY_UNAVAILABLE";
                break;
            case 2:
                s = "AVAILABLE";
                break;
        }
        Log.d("TrackingService", "Provider " + provider + " has changed status to: " + s);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("TrackingService", "Provider " + provider + " is now enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("TrackingService", "Provider " + provider + " is now disabled");
    }
}
