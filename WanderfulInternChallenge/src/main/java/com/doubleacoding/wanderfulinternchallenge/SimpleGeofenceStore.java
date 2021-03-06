/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doubleacoding.wanderfulinternchallenge;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Storage for geofence values, implemented in SharedPreferences.
 * For a production app, use a content provider that's synced to the
 * web or loads geofence data based on current location.
 */
public class SimpleGeofenceStore {

    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;
    private String geofenceReference;
    public static final String GEOFENCE_IDS = "geofenceids";

    // The name of the resulting SharedPreferences
    private static final String SHARED_PREFERENCE_NAME =
                    LocationListActivity.class.getSimpleName();

    // Create the SharedPreferences storage with private access only
    public SimpleGeofenceStore(Context context) {
        mPrefs =
                context.getSharedPreferences(
                        SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE);

    }

    /**
     * Returns a stored geofence by its id, or returns {@code null}
     * if it's not found.
     *
     * @param id The ID of a stored geofence
     * @return A geofence defined by its center and radius. See
     * {@link TargetGeoFence}
     */
    public TargetGeoFence getGeofence(String id) {

        String name = mPrefs.getString(getGeofenceFieldKey(id, GeofenceUtils.KEY_NAME), null);

        /*
         * Get the latitude for the geofence identified by id, or GeofenceUtils.INVALID_VALUE
         * if it doesn't exist
         */


        double lat = mPrefs.getFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE),
                GeofenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the longitude for the geofence identified by id, or
         * -999 if it doesn't exist
         */
        double lng = mPrefs.getFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE),
                GeofenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the radius for the geofence identified by id, or GeofenceUtils.INVALID_VALUE
         * if it doesn't exist
         */
        float radius = mPrefs.getFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS),
                GeofenceUtils.INVALID_FLOAT_VALUE);
        /*
         * Get the URL for the geofence identified by id, or GeofenceUtils.INVALID_VALUE
         * if it doesn't exist
         */
        String url = mPrefs.getString(getGeofenceFieldKey(id, GeofenceUtils.KEY_URL), null);
        /*
         * Get the radius for the geofence identified by id, or GeofenceUtils.INVALID_VALUE
         * if it doesn't exist
         */
        String notificationText = mPrefs.getString(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_NOTIFICATION_TEXT),
                null);

        /*
         * Get the expiration duration for the geofence identified by
         * id, or GeofenceUtils.INVALID_VALUE if it doesn't exist
         */
        long expirationDuration = mPrefs.getLong(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
                GeofenceUtils.INVALID_LONG_VALUE);

        /*
         * Get the transition type for the geofence identified by
         * id, or GeofenceUtils.INVALID_VALUE if it doesn't exist
         */
        int transitionType = mPrefs.getInt(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE),
                GeofenceUtils.INVALID_INT_VALUE);

        // If none of the values is incorrect, return the object
        if (
            lat != GeofenceUtils.INVALID_FLOAT_VALUE &&
            lng != GeofenceUtils.INVALID_FLOAT_VALUE &&
            radius != GeofenceUtils.INVALID_FLOAT_VALUE &&
            expirationDuration != GeofenceUtils.INVALID_LONG_VALUE &&
            transitionType != GeofenceUtils.INVALID_INT_VALUE) {

            // Return a true Geofence object
            return new TargetGeoFence(id, name, lat, lng, radius, url,
                    notificationText, expirationDuration, transitionType);

        // Otherwise, return null.
        } else {
            return null;
        }
    }

    public List<String> getGeofenceList(){
        String tmp = mPrefs.getString(GEOFENCE_IDS, null);
        if(tmp != null) {
            return Arrays.asList(tmp.split(","));
        }
        else
            return null;
    }

    /*method gets all geofences stored in the preferences.*/
    public List<HashMap<String, String>> getGeofences(){
        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        List<String> refs = getGeofenceList();
        if(refs != null){
            for(String s : refs){
                HashMap<String, String> mapTmp = getGeofence(s).toHashMap();
                mapTmp.put("reference", s);
                result.add(mapTmp);
            }
            return result;
        }
        return null;
    }

    /**
     * Save a geofence.

     * @param geofence The {@link TargetGeoFence} containing the
     * values you want to save in SharedPreferences
     */
    public void setGeofence(String id, TargetGeoFence geofence) {

        /*
         * Get a SharedPreferences editor instance. Among other
         * things, SharedPreferences ensures that updates are atomic
         * and non-concurrent
         */
        Editor editor = mPrefs.edit();
        geofenceReference = mPrefs.getString(GEOFENCE_IDS, null);
        List<String> geofences = new ArrayList<String>();
        List<String> tmp = getGeofenceList();
        if(tmp != null){
            geofences.addAll(tmp);
        }
        //if no geofences then this is the first one. add it to the references and put references back in preferences.
        if(geofenceReference == null || geofences.isEmpty())
        {
            geofenceReference = (String) geofence.getId();
        }else
        {
            geofences.add(geofence.getId());
            //convert to csv to be put back into shared preferences.
            geofenceReference = geofences.toString().replace("[", "").replace("]", "")
                    .replace(", ", ",");
        }
        editor.putString(GEOFENCE_IDS, geofenceReference);

        // Write the Geofence values to SharedPreferences

        editor.putString(getGeofenceFieldKey(id, GeofenceUtils.KEY_NAME), (String) geofence.getName());

        editor.putFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE),
                (float) geofence.getLatitude());

        editor.putFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE),
                (float) geofence.getLongitude());

        editor.putFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS),
                geofence.getRadius());

        editor.putString(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_URL),
                geofence.getUrl());

        editor.putString(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_NOTIFICATION_TEXT),
                geofence.getNotificationText());

        editor.putLong(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
                geofence.getExpirationDuration());

        editor.putInt(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE),
                geofence.getTransitionType());

        // Commit the changes
        editor.commit();
    }

    public void clearGeofence(String id) {

        // Remove a flattened geofence object from storage by removing all of its keys
        Editor editor = mPrefs.edit();
        editor.remove(GEOFENCE_IDS);
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_NAME));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_URL));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_NOTIFICATION_TEXT));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE));
        editor.commit();
    }

    /**
     * Given a Geofence object's ID and the name of a field
     * (for example, GeofenceUtils.KEY_LATITUDE), return the key name of the
     * object's values in SharedPreferences.
     *
     * @param id The ID of a Geofence object
     * @param fieldName The field represented by the key
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeofenceFieldKey(String id, String fieldName) {

        return
                GeofenceUtils.KEY_PREFIX +
                id +
                "_" +
                fieldName;
    }
}
