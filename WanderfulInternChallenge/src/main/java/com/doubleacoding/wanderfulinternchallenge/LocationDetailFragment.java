package com.doubleacoding.wanderfulinternchallenge;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


/**
 * A fragment representing a single Location detail screen.
 * This fragment is either contained in a {@link LocationListActivity}
 * in two-pane mode (on tablets) or a {@link LocationDetailActivity}
 * on handsets.
 */
public class LocationDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private String reference;

    MapView mapView;
    GoogleMap map;


     /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            reference = getArguments().getString(ARG_ITEM_ID);

            String URL = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
            StringBuilder sBuilder = new StringBuilder(URL);
            try {
                sBuilder.append(URLEncoder.encode(reference, "utf8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String DEFAULT_RADIUS_SENSOR_ADD_KEY = "&sensor=true&key=";
            sBuilder.append(DEFAULT_RADIUS_SENSOR_ADD_KEY);
            sBuilder.append(getResources().getString(R.string.places_api_key));
            if (this.isAdded())
                new GetTargetTask().execute(sBuilder.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();//needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        map = mapView.getMap();

        map.setMyLocationEnabled(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==  R.id.set_geofence) {
            //TODO: Request the geofence that is being displayed.
            return true;
        }else
        return super.onOptionsItemSelected(item);

    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // query string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the JSON as
    // an InputStream. Finally, the InputStream is converted into a JSON, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class GetTargetTask extends AsyncTask<String, Void, String> {
        HashMap<String, String> item = null;

        @Override
        protected String doInBackground(String... urls) {
            try{
                item = downloadUrl(urls[0]);
                if (item == null) {
                    return getResources().getString(R.string.data_not_there);
                } else
                    return getResources().getString(R.string.data_loaded);
            }catch (IOException e) {
                return "Invalid URL: " + urls[0];
            }

        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
            if (item == null || item.isEmpty()) {
                Toast.makeText(getActivity(), getResources().getString(R.string.query_empty), Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
            } else{
                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(item.get("lat")), Double.parseDouble(item.get("lng"))));
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
                    map.moveCamera(center);
                    map.animateCamera(zoom);
            }
        }
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private HashMap<String, String> downloadUrl(String urlString) throws IOException {
        HashMap<String, String> results;
        URL url = new URL(urlString);
        TargetParser targetParse = new TargetParser();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(100000 /* milliseconds */);
        connection.setConnectTimeout(150000 /* milliseconds */);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        // Starts the query
        connection.connect();
        InputStream JSONStream = connection.getInputStream();
        results = targetParse.readStream(JSONStream);
        connection.disconnect();
        return results;
    }
    //in order to display map view in fragment we need to override all of the map view lifecycle changes
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
