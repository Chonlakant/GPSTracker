package pwr.chojnacki.robert.gpstracker;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private TrackingService tracking_service;

    public MapFragment() {
        super();
        tracking_service = MainActivity.getTS();
    }

    // Calculate distance between two locations
    private double getDistance(LatLng a, LatLng b) {
        Location loc1 = new Location("");
        loc1.setLatitude(a.latitude);
        loc1.setLongitude(a.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(b.latitude);
        loc2.setLongitude(b.longitude);

        return loc1.distanceTo(loc2);
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

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        MainActivity.setToolbarTitle("Map");

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.e("MapFragment", "Cannot initialize the map");
            Log.e("MapFragment", e.getMessage());
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // Check permissions
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                ArrayList<TrackingDatabase.TrackingRecordClass> result = TrackingDatabase.select();

                int i = 0;
                LatLng last_coords = null;

                if (result != null) {
                    for (TrackingDatabase.TrackingRecordClass r : result) {
                        LatLng coords = new LatLng(r.latitude, r.longitude);
                        googleMap.addMarker(
                                new MarkerOptions().position(coords)
                                        .title("#" + r.id + " " + r.time)
                                        .snippet(convert(r.latitude, r.longitude))
                        );
                        if (i > 0) {
                            Polyline line = googleMap.addPolyline(new PolylineOptions()
                                    .add(last_coords, coords)
                                    .width(5)
                                    .color(Color.RED));
                        }
                        last_coords = coords;
                        i++;
                    }
                }
                // For zooming automatically to the location of the marker
                if (result.size() > 0) {
                    LatLng last_pos = new LatLng(result.get(0).latitude, result.get(0).longitude);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(last_pos)
                            .zoom(14)
                            .build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(51.11, 17.022222))
                            .zoom(14)
                            .build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        MainActivity.setToolbarTitle("Map");
    }
}
