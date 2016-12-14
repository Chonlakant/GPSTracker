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

public class TrackingService extends Service implements LocationListener {
    protected static int MIN_DISTANCE_DIFFERENCE = 10; // 10 meters
    protected static int INTERVAL = 1000 * 10; // 10 seconds
    private static TrackingDatabase db;
    protected final Context context;
    protected LocationManager location_manager = null;
    protected Location location = null;
    protected boolean is_working = false;
    protected boolean is_gps_enabled = false;
    protected boolean is_network_enabled = false;

    public TrackingService(Context context) {
        super();
        this.context = context;
        TrackingDatabase.init(context);
    }

    public TrackingService() {
        super();
        this.context = null;
        TrackingDatabase.init(context);
    }

    public void setInternal(int interval) {
        if (!this.is_working && interval > 0)
            INTERVAL = interval;
    }

    public int getInterval() {
        return INTERVAL;
    }

    public int getMinDistanceDifference() {
        return MIN_DISTANCE_DIFFERENCE;
    }

    public void setMinDistanceDifference(int min_distance) {
        if (!this.is_working && min_distance > 0)
            MIN_DISTANCE_DIFFERENCE = min_distance;
    }

    // Insert new location into database
    protected void insertLocationToDatabase() {
        try {
            if (this.location != null) {
                long result = TrackingDatabase.insert(this.location.getLatitude(), this.location.getLongitude());
                if (result > 0)
                    Log.i("TrackingService", "Location saved to database");
            }
        } catch (Exception e) {
            Log.e("TrackingService", "Cannot save location to database");
            Log.e("TrackingService", e.getMessage());
    }
    }

    // Get new or replace location manager
    protected void setLocationManager() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            // Get location manager
            this.location_manager = (LocationManager) this.context.getSystemService(LOCATION_SERVICE);
            Log.i("TrackingService", "Available providers: " + this.location_manager.getAllProviders().toString());

            // Get GPS provider status
            this.is_gps_enabled = this.location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // Get network provider status
            this.is_network_enabled = this.location_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (this.is_gps_enabled) {
                // Request location updates from GPS provider
                this.location_manager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        INTERVAL,
                        MIN_DISTANCE_DIFFERENCE,
                        this);
                Log.i("TrackingService", "Using GPS provider");
                this.location = this.location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else if (this.is_network_enabled) {
                // Request location updates from network provider
                this.location_manager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        INTERVAL,
                        MIN_DISTANCE_DIFFERENCE,
                        this);
                Log.i("TrackingService", "Using network provider");
                this.location = this.location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
    }

    // Start service
    public void start() {
        try {
            this.setLocationManager();
            insertLocationToDatabase();
            this.is_working = true;
            Log.i("TrackingService", "Background service started");
        } catch (Exception e) {
            this.is_working = false;
            this.location = null;
            Log.e("TrackingService", "Cannot start the background service");
            Log.e("TrackingService", e.getMessage());
        }
    }

    // Stop service
    public void stop() {
        try {
            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            }
            if (this.is_working) {
                this.location_manager.removeUpdates(TrackingService.this);
            }
            this.is_working = false;
            this.location = null;
            Log.i("TrackingService", "Background service stopped");
        } catch (Exception e) {
            Log.e("TrackingService", "Cannot stop the background service");
            Log.e("TrackingService", e.getMessage());
        }
    }

    // Show location settings alert
    public void showSettingsAlert() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.context);
            alertDialog.setTitle("Location settings");
            alertDialog.setMessage("Location service is not enabled. Do you want to go to settings menu?");
            // On pressing settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Start settings activity
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                }
            });
            // On pressing canel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Close alert
                    dialog.cancel();
                }
            });
            alertDialog.show();
        } catch (Exception e) {
            Log.e("TrackingService", "Cannot show location settings alert");
            Log.e("TrackingService", e.getMessage());
        }
    }

    // Get latest location
    public Location getLocation() {
        return this.location;
    }

    // Get latest latitude
    public double getLatitude() {
        if (this.location != null) {
            return this.location.getLatitude();
        } else {
            return 0.0;
        }
    }

    // Get latest longitude
    public double getLongitude() {
        if (this.location != null) {
            return this.location.getLongitude();
        } else {
            return 0.0;
        }
    }

    // Get service status
    public boolean isWorking() {
        return this.is_working;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        this.insertLocationToDatabase();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String status_str = "UNKNOWN";
        switch(status) {
            case 0:
                status_str = "OUT_OF_SERVICE";
                break;
            case 1:
                status_str = "TEMPORARILY_UNAVAILABLE";
                break;
            case 2:
                status_str = "AVAILABLE";
                break;
        }
        Log.i("TrackingService", "Provider " + provider + " has changed status to: " + status_str);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("TrackingService", "Provider " + provider + " is now enabled");
        try {
            if (is_working) {
                this.setLocationManager();
            }
        } catch (Exception e) {
            Log.e("TrackingService", "Cannot update location manager");
            Log.e("TrackingService", e.getMessage());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("TrackingService", "Provider " + provider + " is now disabled");
        try {
            if (is_working) {
                this.setLocationManager();
            }
        } catch (Exception e) {
            Log.e("TrackingService", "Cannot update location manager");
            Log.e("TrackingService", e.getMessage());
        }
    }
}
