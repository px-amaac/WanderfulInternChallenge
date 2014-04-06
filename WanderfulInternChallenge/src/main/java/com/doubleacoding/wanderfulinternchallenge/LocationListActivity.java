package com.doubleacoding.wanderfulinternchallenge;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;


/**
 * An activity representing a list of Locations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a Map. On tablets, the activity presents the list of items and
 * map side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link LocationListFragment} and the map
 * (if present) is a {@link LocationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link LocationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class LocationListActivity extends FragmentActivity
        implements LocationListFragment.Callbacks, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    protected static final String TAG_ERROR_DIALOG_FRAGMENT = "errorDialog";
    private LocationClient locClient;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Location mloc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        locClient =new LocationClient(this, this, this);


        ActionBar aB = getActionBar();
        aB.setDisplayHomeAsUpEnabled(true);
        aB.setDisplayShowCustomEnabled(true);
        LayoutInflater inf = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(readyToGo()) {
            if (findViewById(R.id.location_detail_container) != null) {
                // The detail container view will be present only in the
                // large-screen layouts (res/values-large and
                // res/values-sw600dp). If this view is present, then the
                // activity should be in two-pane mode.
                mTwoPane = true;

                // In two-pane mode, list items should be given the
                // 'activated' state when touched.
                ((LocationListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.location_list))
                        .setActivateOnItemClick(true);
            }
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }


    // sets location client up if needed
    private void setUpLocClientIfNeeded() {
        if (locClient == null) {
            locClient = new LocationClient(getApplicationContext(), this,
                    this);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }


    /*Check if GooglePlayServices are available. Using Code written by Mark L. Murphy. Commonsware Warescription.*/
    protected boolean readyToGo() {
        int status =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            return (true);
        } else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
            ErrorDialogFragment.newInstance(status)
                    .show(getFragmentManager(),
                            TAG_ERROR_DIALOG_FRAGMENT);
        }else {
            Toast.makeText(this, R.string.no_service, Toast.LENGTH_LONG).show();
            finish();
        }
        return(false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mloc=locClient.getLastLocation();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), getString(R.string.loc_failed),
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
                connectionResult.startResolutionForResult(this,
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
     * Show a dialog returned by Google Play services for the connection error
     * code
     *
     * @param errorCode
     *            An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(),
                    LocationUtils.APPTAG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        SearchManager mSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        mSearchView.setSearchableInfo(mSearchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Callback method from {@link LocationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(LocationDetailFragment.ARG_ITEM_ID, id);
            LocationDetailFragment fragment = new LocationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.location_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, LocationDetailActivity.class);
            detailIntent.putExtra(LocationDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
