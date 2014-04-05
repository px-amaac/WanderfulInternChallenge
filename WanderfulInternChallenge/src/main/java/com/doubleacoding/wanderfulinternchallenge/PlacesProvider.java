package com.doubleacoding.wanderfulinternchallenge;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.Toast;

/**
 * Created by ShaDynastys on 4/4/2014.
 */
public class PlacesProvider extends ContentProvider {
    private static final String LOG = "WanderfulChallengerApp";

    //define the athority for this content provider.
    private static final String AUTHORITY = "com.doubleacoding.wanderfulinternchallenge.places_provider";
    private static final String API_KEY = "AIzaSyDWUuQj_GS_stpWB0oqf7FzaGiuL6-7UbE";

    //arbitrary constant identifying search suggestion in the uri matcher
    private static final int SUGGESTION = 42;


    private static final UriMatcher uMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    private static final String[] SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,                                //id to match clicks
            SearchManager.SUGGEST_COLUMN_TEXT_1,            //column for the suggestiojn
            SearchManager.SUGGEST_COLUMN_TEXT_2             //column for the description
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
        Toast.makeText(this.getContext(), "HELLO PROVIDER", Toast.LENGTH_LONG).show();
        return null;
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
