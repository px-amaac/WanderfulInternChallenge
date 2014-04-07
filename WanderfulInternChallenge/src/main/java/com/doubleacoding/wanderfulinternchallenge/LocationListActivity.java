package com.doubleacoding.wanderfulinternchallenge;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


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
        implements LocationListFragment.Callbacks {

    protected static final String TAG_ERROR_DIALOG_FRAGMENT = "errorDialog";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private SearchView mSearchView;
    private boolean mTwoPane;

    private String query = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        ActionBar aB = getActionBar();
        aB.setDisplayHomeAsUpEnabled(true);
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

        handleIntent(getIntent());
    }



    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_LONG).show();
        }else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
            Toast.makeText(this, query, Toast.LENGTH_LONG).show();
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
            LocationListFragment.ErrorDialogFragment.newInstance(status)
                    .show(getFragmentManager(),
                            TAG_ERROR_DIALOG_FRAGMENT);
        }else {
            Toast.makeText(this, R.string.no_service, Toast.LENGTH_LONG).show();
            finish();
        }
        return(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        SearchManager mSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
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

    @Override
    public String getQuery() {
        return query;
    }



}
