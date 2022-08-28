package com.jovistar.espcontroller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.dto.Device;
import com.jovistar.espcontroller.task.TaskDiscoverControllers;
import com.jovistar.espcontroller.task.TaskDiscoverDevices;
import com.jovistar.espcontroller.task.TaskDiscoverWifiHotspots;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jovika on 4/13/2018.
 */

public class ActivityNetwork extends AppCompatActivity implements TaskDiscoverDevices.DiscoverDeviceCallback, TaskDiscoverControllers.DiscoverControllerCallback, TaskDiscoverWifiHotspots.DiscoverWiFiCallback {

    public static HashMap<String, Device> deviceList = new HashMap<String, Device>();
    static ArrayList<String> wifiSSID = new ArrayList<>();
    final String LOG_TAG = ActivityNetwork.class.getSimpleName();
    protected ActivityNetworkAdapter adapter;
    RecyclerView recyclerView = null;
    TextView noDeviceView = null;

    public static Device getDeviceByUID(String uid) {
        return deviceList.get(uid);
    }

    public static Device getDeviceAt(int i) {
        return (Device) deviceList.values().toArray()[i];
    }

    public static int getDeviceCount() {
        return deviceList.size();
    }

    public static ArrayList<String> getWifiSSID() {
        return wifiSSID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netwrok_grid);

        recyclerView = findViewById(R.id.network_recyclerview);
        noDeviceView = findViewById(R.id.activity_network_nodevice);

        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));

        if (deviceList.size() > 0) {
            noDeviceView.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new GridLayoutManager(this, (int) Math.round(Math.sqrt(deviceList.size()))));
            adapter = new ActivityNetworkAdapter(this, new ArrayList<Device>(deviceList.values()));
            recyclerView.setAdapter(adapter);
        } else {
            noDeviceView.setText(getResources().getString(R.string.info_devicenotfound));
            noDeviceView.setVisibility(View.VISIBLE);
        }

        searchForDevices();
    }

    /**
     * Start TaskDiscoverDevices to search for devices on local network.
     */
    public void searchForDevices() {
        Log.v(LOG_TAG, "starting TaskDiscoverDevices");
        new TaskDiscoverDevices(ActivityNetwork.this, this).execute(null, null);
    }

    @Override
    public void onFoundDevice(ArrayList<Device> devices) {
        // we found a LED strip! what do we do now?
        if ((devices == null || devices.size() == 0)) {

            if (adapter == null || (adapter != null && adapter.getItemCount() == 0)) {
                noDeviceView.setText(getResources().getString(R.string.info_devicenotfound));
                noDeviceView.setVisibility(View.VISIBLE);
            }

            return;
        }

        for (int i = 0; i < devices.size(); i++) {
            Log.v(LOG_TAG, "onDeviceFound() device: " + devices.get(i));
            devices.get(i).setStatus((byte) 1);// device is on
            deviceList.put(devices.get(i).getUID(), devices.get(i));// put in memory list
        }

        if (devices.size() > 0) {
            adapter = null;
            //rebuild the device list with new Device(s)
            noDeviceView.setVisibility(View.GONE);
            adapter = new ActivityNetworkAdapter(this, new ArrayList<Device>(deviceList.values()));
            recyclerView.setAdapter(adapter);
        } else {
            // deviceList.put should have upadated the adapter. just call notifyDataSetChanged
            //adapter.add(device);
        }

        recyclerView.setLayoutManager(new GridLayoutManager(this, (int) Math.round(Math.sqrt(deviceList.size()))));
        adapter.notifyDataSetChanged();

        Log.v(LOG_TAG, "onDeviceFound() devices: " + adapter.getItemCount());
        Log.v(LOG_TAG, "onDeviceFound() devices: " + deviceList);

    }

    @Override
    public void onFoundController(Device device) {
        if (device == null) {
            return;
        }

        adapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new GridLayoutManager(this, (int) Math.round(Math.sqrt(deviceList.size()))));

        Intent it = new Intent(this, ActivityController.class);
        it.putExtra(FragmentController.ARG_DEVICE, device.getUID());
        startActivity(it);

        //Log.v(LOG_TAG, "onFoundCapability() devices: " + device);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_network, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_refresh:

                if (adapter != null) {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }
                adapter = null;

                searchForDevices();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFoundWiFi(ArrayList<String> list) {
        Log.d("onFoundWiFi ", "list " + list);
        wifiSSID.clear();
        wifiSSID.addAll(list);
    }
}
