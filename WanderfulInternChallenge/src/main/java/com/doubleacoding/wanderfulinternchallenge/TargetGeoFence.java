package com.doubleacoding.wanderfulinternchallenge;

import com.google.android.gms.location.Geofence;

import java.util.HashMap;

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

/**
 * Created by Aaron McIntyre on 4/7/2014.
 */
public class TargetGeoFence {

        // Instance variables
        private final String mId;
        private final String mName;
        private final double mLatitude;
        private final double mLongitude;
        private final float mRadius;
        private final String mUrl;
        private final String mNotificationText;
        private long mExpirationDuration;
        private int mTransitionType;

        /**
         * @param geofenceId The Geofence's request ID
         * @param latitude Latitude of the Geofence's center. The value is not checked for validity.
         * @param longitude Longitude of the Geofence's center. The value is not checked for validity.
         * @param radius Radius of the geofence circle. The value is not checked for validity
         * @param url is the url to the location being displayed on the mapdity
         * @param notificationText Text to be displayed in the notification.
         * @param expiration Geofence expiration duration in milliseconds The value is not checked for
         * validity.
         * @param transition Type of Geofence transition. The value is not checked for validity.
         */
        public TargetGeoFence(
                String geofenceId,
                String name,
                double latitude,
                double longitude,
                float radius,
                String url,
                String notificationText,
                long expiration,
                int transition) {
            // Set the instance fields from the constructor

            // An identifier for the geofence
            this.mId = geofenceId;
            this.mName = name;

            // Center of the geofence
            this.mLatitude = latitude;
            this.mLongitude = longitude;

            // Radius of the geofence, in meters
            this.mRadius = radius;
            this.mUrl = url;
            this.mNotificationText = notificationText;

            // Expiration time in milliseconds
            this.mExpirationDuration = expiration;

            // Transition type
            this.mTransitionType = transition;
        }
        // Instance field getters

        /**
         * Get the geofence ID
         * @return A SimpleGeofence ID
         */
        public String getId() {
            return mId;
        }
    
        public String getName() {return mName; }

        /**
         * Get the geofence latitude
         * @return A latitude value
         */
        public double getLatitude() {
            return mLatitude;
        }

        /**
         * Get the url from the geofence
         * @return a String url*/
        public String getUrl() {
            return mUrl;
        }
        /**
         * Get the notification text from the geofence
         * @return a String notification text*/
        public String getNotificationText() {
            return mNotificationText;
        }

        /**
         * Get the geofence longitude
         * @return A longitude value
         */
        public double getLongitude() {
            return mLongitude;
        }

        /**
         * Get the geofence radius
         * @return A radius value
         */
        public float getRadius() {
            return mRadius;
        }

        /**
         * Get the geofence expiration duration
         * @return Expiration duration in milliseconds
         */
        public long getExpirationDuration() {
            return mExpirationDuration;
        }

        /**
         * Get the geofence transition type
         * @return Transition type (see Geofence)
         */
        public int getTransitionType() {
            return mTransitionType;
        }

        /**
         * Creates a Location Services Geofence object from a
         * SimpleGeofence.
         *
         * @return A Geofence object
         */
        public Geofence toGeofence() {
            // Build a new Geofence object
            return new Geofence.Builder()
                    .setRequestId(getId().substring(0,42))
                    .setTransitionTypes(mTransitionType)
                    .setCircularRegion(
                            getLatitude(),
                            getLongitude(),
                            getRadius())
                    .setLoiteringDelay(5)
                    .setExpirationDuration(mExpirationDuration)
                    .build();
        }

        public HashMap<String, String> toHashMap(){
            HashMap<String,String> result = new HashMap<String, String>();
            result.put(GeofenceUtils.KEY_NAME, getName());
            result.put(GeofenceUtils.KEY_LATITUDE, Double.toString(getLatitude()));
            result.put(GeofenceUtils.KEY_LONGITUDE, Double.toString(getLongitude()));
            result.put(GeofenceUtils.KEY_NOTIFICATION_TEXT, getNotificationText());
            result.put(GeofenceUtils.KEY_URL, getUrl());
            return result;



        }
    }
