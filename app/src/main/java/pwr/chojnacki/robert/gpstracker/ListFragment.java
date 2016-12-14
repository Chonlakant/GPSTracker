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

import java.util.ArrayList;

public class ListFragment extends Fragment {
    private View my_view;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private Button buttonRefresh;

    public ListFragment() {
        super();
    }

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
        ArrayList<TrackingDatabase.TrackingRecordClass> result = TrackingDatabase.select();
        ArrayList<String> list_items = new ArrayList<>();
        if (result != null) {
            for (TrackingDatabase.TrackingRecordClass r : result) {
                list_items.add(r.time + ",   " + this.convert(r.latitude, r.longitude));
            }
        }

        adapter = new ArrayAdapter<>(getActivity(), R.layout.fragment_list_item, list_items);
        list.setAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.setToolbarTitle("List");
        my_view = inflater.inflate(R.layout.fragment_list, container, false);
        buttonRefresh = (Button) my_view.findViewById(R.id.buttonRefresh);
        list = (ListView) my_view.findViewById(R.id.list);

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