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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    private View my_view;
    private Button buttonSettings;
    private EditText editInterval, editDifference;
    private TrackingService tracking_service;

    public SettingsFragment() {
        super();
        tracking_service = MainActivity.tracking_service;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        my_view = inflater.inflate(R.layout.fragment_settings, container, false);

        buttonSettings = (Button) my_view.findViewById(R.id.buttonSettings);
        editInterval = (EditText) my_view.findViewById(R.id.editInterval);
        editDifference = (EditText) my_view.findViewById(R.id.editDifference);
        editInterval.setText(String.valueOf(tracking_service.getInterval() / 1000));
        editDifference.setText(String.valueOf(tracking_service.getMinDistanceDifference()));

        // On Aplly click
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracking_service.is_working) {
                    Toast.makeText(container.getContext(), "You must stop tracking first.", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        int diff = Integer.valueOf(editDifference.getText().toString());
                        int interval = Integer.valueOf(editInterval.getText().toString());
                        tracking_service.setMinDistanceDifference(diff);
                        tracking_service.setInternal(interval * 1000);
                    } catch (Exception e) {
                        Log.e("SettingsFragment", "Integer parsing error");
                        Log.e("SettingsFragment", e.getMessage());
                    }
                }
            }
        });

        return my_view;
    }
}
