package com.doubleacoding.wanderfulinternchallenge;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A fragment representing a single Location detail screen.
 * This fragment is either contained in a {@link LocationListActivity}
 * in two-pane mode (on tablets) or a {@link LocationDetailActivity}
 * on handsets.
 */
public class LocationDetailFragment extends SupportMapFragment {
    public static final String TAG = "detail";


    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private String reference;

    private GoogleMap map;
    private Marker location;
    private TargetGeoFence mGeofence;
    // Store a list of geofences to add
    private List<Geofence> mCurrentGeofences;
    // location item
    private HashMap<String, String> locationItem;
    // Persistent storage for geofences
    private SimpleGeofenceStore mPrefs;
    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;
    private GeofenceRemover mGeofenceRemover;

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
        // Instantiate a new geofence storage area
        mPrefs = new SimpleGeofenceStore(getActivity());

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(getActivity());
        mGeofenceRemover = new GeofenceRemover(getActivity());


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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        map=getMap();
        map.clear();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_menu, menu);
        MenuItem setgeo = menu.findItem(R.id.set_geofence);
        MenuItem removegeo = menu.findItem(R.id.remove_geofence);
        List<String> fences = mPrefs.getGeofenceList();
        if(fences != null && fences.contains(reference)){
            setgeo.setVisible(false);
            removegeo.setVisible(true);
        }
        else{
            setgeo.setVisible(true);
            removegeo.setVisible(false);
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==  R.id.set_geofence) {

            mCurrentGeofences.add(mGeofence.toGeofence());
            try {
                // Try to add geofences
                mGeofenceRequester.addGeofences(mCurrentGeofences, locationItem.get("url"));
            } catch (UnsupportedOperationException e) {
                // Notify user that previous request hasn't finished.
                Toast.makeText(getActivity(), R.string.add_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();
            }
            mPrefs.setGeofence(locationItem.get("reference"), mGeofence);
            getActivity().onBackPressed();
            return true;
        }else if (item.getItemId() == R.id.remove_geofence){
            List<String> mGeofenceIdsToRemove = new ArrayList<String>();

            mGeofenceIdsToRemove.add(reference);

            // Try to remove the geofence
            try {
                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

                // Catch errors with the provided geofence IDs
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                // Notify user that previous request hasn't finished.
                Toast.makeText(getActivity(), R.string.remove_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();
            }
            mPrefs.clearGeofence(locationItem.get("reference"));
            getActivity().onBackPressed();
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
        @Override
        protected String doInBackground(String... urls) {
            try{
                locationItem = downloadUrl(urls[0]);
                if (locationItem == null) {
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
            if (locationItem == null || locationItem.isEmpty()) {
                Toast.makeText(getActivity(), getResources().getString(R.string.query_empty), Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
            } else{
                //setup the camera to zoom on location selected
                Double lat = Double.parseDouble(locationItem.get("lat"));
                Double lng = Double.parseDouble(locationItem.get("lng"));
                LatLng latlng = new LatLng(lat, lng);
                location = map.addMarker(new MarkerOptions()
                        .position(latlng)
                        .title(locationItem.get("name"))
                        .draggable(false)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .snippet(locationItem.get("vicinity")));
                location.showInfoWindow();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latlng)      // Sets the center of the map to Mountain View
                        .zoom(10)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                setupGeofence(locationItem);
            }
        }
    }
    private void setupGeofence(HashMap<String, String> item){
        mGeofence = new TargetGeoFence(
                item.get("reference")
                , item.get("name")
                , Double.parseDouble(item.get("lat"))
                , Double.parseDouble(item.get("lng"))
                , 8046 //radius set to 5 miles
                , item.get("url")
                , item.get("vicinity")
                , Geofence.NEVER_EXPIRE
                , Geofence.GEOFENCE_TRANSITION_ENTER);

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(Double.parseDouble(item.get("lat")), Double.parseDouble(item.get("lng"))))
                //.radius(8046)
                .radius(50)
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);
        map.addCircle(circleOptions);
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

}
