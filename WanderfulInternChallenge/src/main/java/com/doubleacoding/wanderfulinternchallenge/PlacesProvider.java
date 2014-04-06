package com.doubleacoding.wanderfulinternchallenge;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aaron McIntyre on 4/4/2014.
 */
public class PlacesProvider extends ContentProvider {
    private static final String LOG = "WanderfulChallengerApp";

    //define the athority for this content provider.
    private static final String AUTHORITY = "com.doubleacoding.wanderfulinternchallenge.places_provider";
    private static final String API_KEY = "AIzaSyDWUuQj_GS_stpWB0oqf7FzaGiuL6-7UbE";
    private static final String PLACES_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?sensor=false&key=";
    private static final String AFTER_KEY = "&components=country:us&input=";

    //arbitrary constant identifying search suggestion in the uri matcher
    private static final int SUGGESTION = 42;


    private static final UriMatcher uMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    private static final String[] SUGGESTION_COLUMNS = {
            BaseColumns._ID,                                //id to match clicks
            SearchManager.SUGGEST_COLUMN_TEXT_1,            //column for the suggestiojn
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA

    };

    //build up URI matcher
    static {
        uMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTION);
        uMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SUGGESTION);
    }
    @Override
    public boolean onCreate() {


        return true;
    }





    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(LOG, "uri= " + uri); //sanity check
        switch(uMatcher.match(uri)){
            case SUGGESTION:
                MatrixCursor mCursor = new MatrixCursor(SUGGESTION_COLUMNS, 1);
                if(uri.getLastPathSegment().toLowerCase() != null && !uri.getLastPathSegment().toLowerCase().isEmpty()) {
                    for (List<String> suggestion : getSuggestions(uri.getLastPathSegment().toLowerCase())) {
                        mCursor.addRow(suggestion);
                    }
                    return mCursor;
                }
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private List<List<String>> getSuggestions(String query){
        ArrayList<List<String>> result = null;
        List<String> resultItem = null;
        HttpURLConnection connection = null;
        StringBuilder results = new StringBuilder();
        //try to connect to places autocomplete api.
        try {
            StringBuilder sBuilder = new StringBuilder(PLACES_URL + API_KEY + AFTER_KEY);
            sBuilder.append(URLEncoder.encode(query, "utf8"));

            URL url = new URL(sBuilder.toString());
            connection = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader((connection.getInputStream()));

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                results.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG, "Error processing Places API URL", e);
            return result;
        } catch (IOException e) {
            Log.e(LOG, "Error connecting to Places API", e);
            return result;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        result = new ArrayList<List<String>>();

        //try to parse the json resaults from places autocomplete api.
        try {
            JSONObject obj = new JSONObject(results.toString());
            JSONArray predictionsArray = obj.getJSONArray("predictions");

            for(int i = 0; i < predictionsArray.length(); i++){
                resultItem = new ArrayList<String>();
                JSONArray terms = predictionsArray.getJSONObject(i).getJSONArray("terms");

                resultItem.add(Integer.toString(i)); //give cursor item an id
                resultItem.add(terms.getJSONObject(0).getString("value")); //suggestion string
                resultItem.add(terms.getJSONObject(0).getString("value"));
                result.add(resultItem); //add the item to the list to populate the cursor.
            }
        } catch (JSONException e) {
            Log.e(LOG, "Cannot process JSON results", e);
        }
        return result;
    }

    @Override
    public String getType(Uri uri) {
        switch (uMatcher.match(uri)){
            case SUGGESTION:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
