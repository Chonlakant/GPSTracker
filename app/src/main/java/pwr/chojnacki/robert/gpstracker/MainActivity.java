package pwr.chojnacki.robert.gpstracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TrackingService ts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1001) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ts = new TrackingService(MainActivity.this);
                if (ts.isWorking()) {
                    //Toast.makeText(this, "It's working :)", Toast.LENGTH_LONG).show();
                    double latitude = ts.getLatitude();
                    double longitude = ts.getLongitude();

                    Toast.makeText(getApplicationContext(), "Your location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "It's not working!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "No permissions!", Toast.LENGTH_LONG).show();
                return;
            }
            //ts.stopService();
        }
    }
}
