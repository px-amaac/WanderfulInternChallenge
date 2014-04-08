package com.doubleacoding.wanderfulinternchallenge;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    private String mUrl;
    /**
     * Sets an identifier for this class' background thread
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }



    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        mUrl = intent.getExtras().getString(LocationDetailFragment.ARG_ITEM_ID);
        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();

        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
            // Get the type of transition (entry or exit)
        int transition = LocationClient.getGeofenceTransition(intent);

        // Test that a valid transition was reported
        if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL)) {

            // Post a notification
            List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
            String[] geofenceIds = new String[geofences.size()];
            for (int index = 0; index < geofences.size() ; index++) {
                geofenceIds[index] = geofences.get(index).getRequestId();
            }
            String ids = TextUtils.join(GeofenceUtils.GEOFENCE_ID_DELIMITER,geofenceIds);
            String transitionType = getTransitionString(transition);

            sendNotification(transitionType, ids);

            // Log the transition type and a message
            Log.d(GeofenceUtils.APPTAG,
                    getString(
                            R.string.geofence_transition_notification_title,
                            transitionType,
                            ids));
            Log.d(GeofenceUtils.APPTAG,
                    getString(R.string.geofence_transition_notification_text));

        // An invalid transition was reported
        } else {
            // Always log as an error
            Log.e(GeofenceUtils.APPTAG,
                    getString(R.string.geofence_transition_invalid_type, transition));
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * @param transitionType The type of transition that occurred.
     *
     */
    private void sendNotification(String transitionType, String ids) {

        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.ic_launcher)
               .setContentTitle(
                       getString(R.string.geofence_transition_notification_title,
                               transitionType, ids))
               .setContentText(getString(R.string.geofence_transition_notification_text))
               .setContentIntent(pendingIntent);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
}
