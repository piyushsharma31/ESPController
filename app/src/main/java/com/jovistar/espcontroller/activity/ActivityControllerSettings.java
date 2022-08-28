package com.jovistar.espcontroller.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.jovistar.espcontroller.dto.Device;

import static com.jovistar.espcontroller.activity.FragmentController.ARG_DEVICE;

/**
 * Created by jovika on 7/6/2018.
 */

public class ActivityControllerSettings extends AppCompatActivity {
    final String LOG_TAG = ActivityControllerSettings.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String deviceUID = getIntent().getStringExtra(ARG_DEVICE);// device uuid
        Device device = ActivityNetwork.getDeviceByUID(getIntent().getStringExtra(ARG_DEVICE));
        setTitle(device.getDeviceName());

        Log.v(LOG_TAG, "device uid " + device.getUID());

        // Display the fragment as the main content.
        FragmentControllerSettings fcs = new FragmentControllerSettings();
        fcs.setDeviceId(deviceUID);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fcs)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
