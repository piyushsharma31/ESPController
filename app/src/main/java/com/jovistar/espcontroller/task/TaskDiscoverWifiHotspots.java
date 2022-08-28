package com.jovistar.espcontroller.task;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jovistar.espcontroller.activity.ActivityNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jovika on 7/21/2018.
 */

public class TaskDiscoverWifiHotspots extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "TaskDiscoverWifiHots";

    private static boolean scanComplete = true;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 125;
    List<ScanResult> wifiList;
    ArrayList<String> values = new ArrayList<String>();
    int netCount = 0;
    TaskDiscoverWifiHotspots.DiscoverWiFiCallback mCallback;
    private ActivityNetwork mContext;
    BroadcastReceiver breceiver = null;
    private WifiManager wifi;

    public TaskDiscoverWifiHotspots(ActivityNetwork context, TaskDiscoverWifiHotspots.DiscoverWiFiCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public static boolean isPermissionGranted(final Activity activity, final String permission) {
        int ret = ContextCompat.checkSelfPermission(activity,
                permission/*Manifest.permission.READ_CONTACTS*/);

        return ret == PackageManager.PERMISSION_GRANTED;
    }

    public static int checkPermission(final Activity activity, final String permission, final int callbackArg, String rationale) {
        int ret = ContextCompat.checkSelfPermission(activity, permission);

        if (ret != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

                // Show an explanation to the user *asynchronously* -- don't
                // block this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                showMessageOKCancel(activity, rationale,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.w(TAG, TAG + "::onClick which " + which);
                                if (which == -2) {
                                    activity.finish();
                                } else {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{permission/*Manifest.permission.READ_CONTACTS*/},
                                            callbackArg);
                                }
                            }
                        });
                Log.w(TAG, TAG + "::shouldShowRequestPermissionRationale");
                return ret;

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{permission}, callbackArg);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        return ret;
    }

    public static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, okListener)
                .create()
                .show();
    }

    protected void onPreExecute() {

        if (scanComplete == false) {
            return;
        }

        wifi = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //Check wifi enabled or not
        if (wifi.isWifiEnabled() == false) {

            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("WiFi is off")
                    .setMessage("Enable WiFi to scan the devices ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            wifi.setWifiEnabled(true);

                            startWiFiScan();
                        }
                    })
                    .setNegativeButton("No", null)
                    .create();
            dialog.show();

        } else {

            if (PackageManager.PERMISSION_GRANTED !=
                    checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS,
                            "Request permission to discover nearby WiFi hotspots")) {
                return;

            } else {

                startWiFiScan();

            }
        }
    }

    void startWiFiScan() {

        scanComplete = false;

        mContext.registerReceiver(breceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiList = wifi.getScanResults();
                netCount = wifiList.size();
                Log.d("Wifi", "Total Wifi Network" + netCount);

                netCount = netCount - 1;
                while (netCount >= 0) {
                    values.add(wifiList.get(netCount).SSID);
                    netCount = netCount - 1;
                }

                Log.d("Wifi", "Wifi Networks " + values);
                mCallback.onFoundWiFi(values);
                mContext.unregisterReceiver(breceiver);
                scanComplete = true;
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifi.startScan();
        Log.d("Wifi", "startScan");
        values.clear();

    }

    protected void onPostExecute(ArrayList<String> list) {

        /*if (list == null) {
            // error
            // TODO
        } else {
            try {
                mCallback.onFoundWiFi(list);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //register Broadcast receiver

        return null;
    }

    public interface DiscoverWiFiCallback {
        void onFoundWiFi(ArrayList<String> list);
    }

}
