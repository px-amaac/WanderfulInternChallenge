package com.doubleacoding.wanderfulinternchallenge;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * A list fragment representing a list of Locations. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link LocationDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class LocationListFragment extends ListFragment implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    public static final String TAG = "listFratment";
    // Whether there is a wifi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    private LocationClient locClient;
    private Location mloc = null;

    //a single list task
    private ListViewLoaderTask listTask = null;

    //data in the list.
    private List<HashMap<String,String>> data = null;
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = null;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    @Override
    public void onConnected(Bundle bundle) {

        mloc=locClient.getLastLocation();
        if(mloc != null){
            Toast.makeText(getActivity(), "Location Found", Toast.LENGTH_LONG);
            try {
                queryPlaces();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.loc_failed),
                Toast.LENGTH_SHORT).show();
		/*
		 * Called by Location Services if the attempt to Location Services
		 * fails.
		 */
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(),
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with
            // the error.
            showErrorDialog(connectionResult.getErrorCode());
        }

    }
    /**
     * Show a dialog returned by Google Play services for the connection error
     * code
     *
     * @param errorCode
     *            An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                getActivity(), LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getActivity().getFragmentManager(),
                    LocationUtils.APPTAG);
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        static final String ARG_STATUS = "status";
        private Dialog	mDialog;

        static ErrorDialogFragment newInstance(int status) {
            Bundle args = new Bundle();
            args.putInt(ARG_STATUS, status);
            ErrorDialogFragment result = new ErrorDialogFragment();
            result.setArguments(args);
            return (result);
        }
        /**
         * Set the dialog to display
         *
         * @param dialog
         *            An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }



        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args=getArguments();
            return GooglePlayServicesUtil.getErrorDialog(args.getInt(ARG_STATUS),
                    getActivity(), 0);
        }
        @Override
        public void onDismiss(DialogInterface dlg) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
//    end Commonsware code.

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         * @param id
         */
        public void onItemSelected(String id);

        public String getQuery();

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationListFragment() {
    }

    public boolean checkAdapter() {
        SimpleAdapter adapter = (SimpleAdapter) getListAdapter();
        return adapter != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locClient =new LocationClient(getActivity(), this, this);
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onStart(){
        super.onStart();
        updateConnectedFlags();
        new SetupLocTask().execute();

    }
    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }


    private String getLatLng() throws UnsupportedEncodingException {
        if(mloc != null) {
            return Double.toString(mloc.getLatitude()) +"," + Double.toString(mloc.getLongitude());
        }
        else
            return null;
    }
    private void queryPlaces() throws UnsupportedEncodingException {
        String query = mCallbacks.getQuery();
        String latlng = getLatLng();
        if(query != null && latlng != null) {
            if (wifiConnected || mobileConnected) {

                String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?keyword=";
                StringBuilder sBuilder = new StringBuilder(URL);
                sBuilder.append(URLEncoder.encode(query, "utf8"));
                String ADD_LOCATION = "&location=";
                sBuilder.append(ADD_LOCATION);
                sBuilder.append(latlng);
                String DEFAULT_RADIUS_SENSOR_ADD_KEY = "&radius=1000&sensor=true&key=";
                sBuilder.append(DEFAULT_RADIUS_SENSOR_ADD_KEY);
                sBuilder.append(getResources().getString(R.string.places_api_key));
                if (this.isAdded())
                    new GetPlacesTask().execute(sBuilder.toString());
            } else {
                Toast.makeText(getActivity(), "No Network Connection Available", Toast.LENGTH_LONG).show();
            }

        }else
        Toast.makeText(getActivity(), "No query or Location unable to provide results.", Toast.LENGTH_LONG).show();
    }



    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(data.get(position).get("reference"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // query string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the JSON as
    // an InputStream. Finally, the InputStream is converted into a JSON, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class GetPlacesTask extends AsyncTask<String, Void, String> {
        List<HashMap<String, String>> items = null;

        @Override
        protected String doInBackground(String... urls) {
            try{
                items = downloadUrl(urls[0]);
                if (items == null) {
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
            if (items == null || items.isEmpty()) {
                Toast.makeText(getActivity(), getResources().getString(R.string.query_empty), Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
            } else{
                data = items;
                ListViewLoaderTask lvLoader = new ListViewLoaderTask();
                lvLoader.execute(data);
            }
        }
    }

    private class ListViewLoaderTask extends AsyncTask<List<HashMap<String, String>>, Void, SimpleAdapter> {
        SimpleAdapter mAdapter = null;
        @Override
        protected SimpleAdapter doInBackground(List<HashMap<String, String>>... list) {
            List<HashMap<String, String>> items = list[0];
            String[] from = {"name", "vicinity"};
            int[] to = {R.id.name, R.id.vicinity};
            mAdapter = new SimpleAdapter(getActivity()
                    .getBaseContext(), items, R.layout.row, from, to);

            return mAdapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter mAdapter) {
            setListAdapter(mAdapter);
            updateThisList();
        }
    }

    public void updateThisList() {
        if (data != null) {
            if (!checkAdapter()) {
                if (listTask == null) {
                    listTask = new ListViewLoaderTask();
                    listTask.execute(data);
                }
            } else {
                ((SimpleAdapter) getListAdapter())
                        .notifyDataSetChanged();
            }
        }
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private List<HashMap<String, String>> downloadUrl(String urlString) throws IOException {
        List<HashMap<String, String>> results;
        URL url = new URL(urlString);
        PlacesParser placesParse = new PlacesParser();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(100000 /* milliseconds */);
        connection.setConnectTimeout(150000 /* milliseconds */);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        // Starts the query
        connection.connect();
        InputStream JSONStream = connection.getInputStream();
        results = placesParse.readStream(JSONStream);
        connection.disconnect();
        return results;
    }
    // sets location client up if needed
    private void setUpLocClientIfNeeded() {
        if (locClient == null) {
            locClient = new LocationClient(getActivity().getApplicationContext(), this,
                    this);
        }

    }
    private class SetupLocTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            setUpLocClientIfNeeded();
            return null;

        }

        @Override
        protected void onPostExecute(Void unused) {
            locClient.connect();

        }
    }
}
