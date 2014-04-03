package com.doubleacoding.wanderfulinternchallenge;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;


/**
 * An activity representing a list of Locations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link LocationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link LocationListFragment} and the item details
 * (if present) is a {@link LocationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link LocationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class LocationListActivity extends FragmentActivity
        implements LocationListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        */
        ActionBar aB = getActionBar();
        aB.setDisplayHomeAsUpEnabled(true);
        aB.setDisplayShowCustomEnabled(true);
        LayoutInflater inf = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inf.inflate(R.layout.autocomplete_view, null);
        getActionBar().setCustomView(v);
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

        // TODO: If exposing deep links into your app, handle intents here.
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
