package pwr.chojnacki.robert.gpstracker;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListFragment extends Fragment {
    private static TrackingDatabase db;
    public View myView;
    private ListView list;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_list, container, false);
        list = (ListView) myView.findViewById(R.id.tracking_list);
        TrackingDatabase.init(getContext());
        ArrayList<TrackingDatabase.TrackingRecordClass> result = TrackingDatabase.select();
        //Log.d("ListFragment", "Record " + result.get(0).id);
        ArrayList<String> list_items = new ArrayList<String>();
        for (TrackingDatabase.TrackingRecordClass r : result) {
            String lat_symbol, long_symbol;
            if (r.latitude >= 0)
                lat_symbol = "N";
            else
                lat_symbol = "S";
            if (r.longitude >= 0)
                long_symbol = "W";
            else
                long_symbol = "E";
            list_items.add(r.date + " " + r.time + ", " + r.latitude + " " + lat_symbol + ", " + r.longitude + " " + long_symbol);
        }

        adapter = new ArrayAdapter<String>(getActivity(), R.layout.fragment_list_item, list_items);
        list.setAdapter(adapter);
        return myView;
        //return super.onCreateView(inflater, container, savedInstanceState);

    }

}