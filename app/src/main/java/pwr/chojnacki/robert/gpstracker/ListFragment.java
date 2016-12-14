package pwr.chojnacki.robert.gpstracker;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ListFragment extends Fragment {
    private View my_view;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private Button buttonRefresh;
    private TextView textDistance;
    private TextView textSpeed;
    private TextView textSteps;

    public ListFragment() {
        super();
    }

    // Round to first decimal place
    double RoundTo1Decimal(double val) {
        DecimalFormat df1 = new DecimalFormat("###.#");
        return Double.valueOf(df1.format(val));
    }

    // Calculate distance between two locations
    private double getDistance(LatLng a, LatLng b) {
        Location loc1 = new Location("");
        loc1.setLatitude(a.latitude);
        loc1.setLongitude(a.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(b.latitude);
        loc2.setLongitude(b.longitude);

        return Math.abs(loc1.distanceTo(loc2));
    }

    // Ceil double from string to make int
    private String strip_int(String s) {
        try {
            Double d = Double.valueOf(s);
            return String.valueOf(Math.ceil(d));
        } catch (Exception e) {
            Log.e("ListFragment", "Integer parsing error");
            Log.e("ListFragment", e.getMessage());
            return null;
        }
    }

    // Converting decimal coordinates to degrees
    private String convert(double latitude, double longitude) {
        final String DEGREE = "\u00b0";
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        String strLatitude[] = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS).split(":");
        String strLongitude[] = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS).split(":");
        String lat_symbol, lng_symbol, result;

        if (latitude >= 0)
            lat_symbol = "N";
        else
            lat_symbol = "S";
        if (longitude >= 0)
            lng_symbol = "E";
        else
            lng_symbol = "W";

        result = "" + strLatitude[0] + DEGREE + " " + strip_int(strLatitude[1]) + "' " + strip_int(strLatitude[2]) + "'' " + lat_symbol;
        result += ",  " + strLongitude[0] + DEGREE + " " + strip_int(strLongitude[1]) + "' " + strip_int(strLongitude[2]) + "'' " + lng_symbol;

        return result;
    }

    private void refresh() {
        try {
            ArrayList<TrackingDatabase.TrackingRecordClass> result = TrackingDatabase.select();
            ArrayList<String> list_items = new ArrayList<>();

            LatLng last_coords = null;
            double distance = 0;
            String first_time = null, last_time = null;

            if (result != null) {
                int i = 0;
                for (TrackingDatabase.TrackingRecordClass r : result) {
                    list_items.add(r.time + ",   " + this.convert(r.latitude, r.longitude));
                    LatLng coords = new LatLng(r.latitude, r.longitude);
                    if (i > 0) {
                        distance += getDistance(last_coords, coords);
                    } else {
                        first_time = r.time;
                    }
                    last_coords = coords;
                    last_time = r.time;
                    i++;
                }
                textDistance.setText("Distance: " + String.valueOf(Math.ceil(distance) / 1000) + " km");

                // Time format HH:MM:SS
                String first_time_array[] = first_time.split(":");
                double end_time = Double.valueOf(first_time_array[0]) * 3600 +
                        Double.valueOf(first_time_array[1]) * 60 +
                        Double.valueOf(first_time_array[2]);
                String last_time_array[] = last_time.split(":");
                double start_time = Double.valueOf(last_time_array[0]) * 3600 +
                        Double.valueOf(last_time_array[1]) * 60 +
                        Double.valueOf(last_time_array[2]);
                double time = end_time - start_time; // in seconds
                Log.d("ListFragment", "Distance: " + (Math.ceil(distance) / 1000));
                Log.d("ListFragment", "Time: " + (time / 3600));
                textSpeed.setText(String.valueOf(RoundTo1Decimal((Math.ceil(distance) / 1000) / (time / 3600))) + " km/h");

                adapter = new ArrayAdapter<>(getActivity(), R.layout.fragment_list_item, list_items);
                list.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e("ListFragment", "Cannot create list");
            Log.e("ListFragment", e.getMessage());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.setToolbarTitle("List");
        my_view = inflater.inflate(R.layout.fragment_list, container, false);
        buttonRefresh = (Button) my_view.findViewById(R.id.buttonRefresh);
        textDistance = (TextView) my_view.findViewById(R.id.textDistance);
        textSpeed = (TextView) my_view.findViewById(R.id.textSpeed);
        textSteps = (TextView) my_view.findViewById(R.id.textSteps);
        list = (ListView) my_view.findViewById(R.id.list);
        //mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        //mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        TrackingDatabase.init(getContext());

        // On Aplly click
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        refresh();

        return my_view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.setToolbarTitle("List");
    }
}