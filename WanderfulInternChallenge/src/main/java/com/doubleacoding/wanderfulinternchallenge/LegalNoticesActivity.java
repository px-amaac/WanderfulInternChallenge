package com.doubleacoding.wanderfulinternchallenge;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class LegalNoticesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legal);

        TextView legal=(TextView)findViewById(R.id.legal);

        legal.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
    }
}