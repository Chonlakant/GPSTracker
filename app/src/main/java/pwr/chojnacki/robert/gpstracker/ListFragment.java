package pwr.chojnacki.robert.gpstracker;

import android.app.Fragment;
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
    // Strip int after 2 digits
    private String strip_int(int i) {
        try {
            String s = String.valueOf(i);
            String r;
            if (s.length() > 1) {
                r = "" + s.charAt(0) + s.charAt(1);
            } else {
                r = "" + s.charAt(0);
            }
            return r;
        } catch (Exception e) {
            Log.e("ListFragment", "Integer parsing error");
            Log.e("ListFragment", e.getMessage());
            return null;
        }
    }

    // Converting decimal coordinates to degrees
    private String convert(double latitude, double longitude) {
        final String DEGREE = "\u00b0";
        int lat_d = (int) latitude;
        int lat_m = (int) ((latitude - lat_d) * 60);
        int lat_s = (int) (latitude - lat_d - lat_m) * 3600;
        int lng_d = (int) longitude;
        int lng_m = (int) ((longitude - lng_d) * 60);
        int lng_s = (int) (longitude - lng_d - lng_m) * 3600;
        String lat_symbol, lng_symbol, result;

        if (latitude >= 0)
            lat_symbol = "N";
        else
            lat_symbol = "S";
        if (longitude >= 0)
            lng_symbol = "E";
        else
            lng_symbol = "W";

        result = "" + Math.abs(lat_d) + DEGREE + " " + strip_int(Math.abs(lat_m)) + "' " + strip_int(Math.abs(lat_s)) + "'' " + lat_symbol;
        result += ",  " + Math.abs(lng_d) + DEGREE + " " + strip_int(Math.abs(lng_m)) + "' " + strip_int(Math.abs(lng_s)) + "'' " + lng_symbol;

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