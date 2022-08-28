package com.jovistar.espcontroller.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.dto.Device;
import com.jovistar.espcontroller.dto.UDPPacket;
import com.jovistar.espcontroller.task.AsyncResponse;
import com.jovistar.espcontroller.task.TaskSetConfiguration;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static com.jovistar.espcontroller.activity.Constants.bytesToHex;

/**
 * Created by jovika on 7/6/2018.
 */

public class FragmentControllerSettings extends PreferenceFragment implements AsyncResponse {
    final static String LOG_TAG = FragmentControllerSettings.class.getSimpleName();

    String deviceId;
    AlertDialog.Builder firmwareUpdateBuilder;
    ArrayAdapter<String> versionArrayAdapter;
    String baseUrl = "http://web.caltxt.com:8080/esp01firmware";
    String deviceCode = "rgbc";
//    String firmwareFilename = "rgbc.191230.bin";

    public static String[] getWiFiAPList() {
        String[] sarray = new String[ActivityNetwork.getWifiSSID().size()];

        if (ActivityNetwork.getWifiSSID().size() > 0) {
            for (int i = 0; i < ActivityNetwork.getWifiSSID().size(); i++) {
//                if(ActivityNetwork.getWifiSSID().get(i).startsWith(device.getDeviceName())) {
//                    continue;
//                }
                sarray[i] = ActivityNetwork.getWifiSSID().get(i);
            }
        }

        Log.v(LOG_TAG, "getWiFiAPList " + sarray.length);
        return sarray;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        versionArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_controller);

        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Log.v(LOG_TAG, "getPreferenceScreen().getPreference " + i + "=" + getPreferenceScreen().getPreference(i));
            pickPreferenceObject(getPreferenceScreen().getPreference(i));

            getPreferenceScreen().getPreference(i).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference instanceof ListPreference) {

                        if (preference.getKey().equals("controllerConnectAs")) {

                        }
                    } else if (preference instanceof Preference) {
                        Log.v(LOG_TAG, "onPreferenceClick " + preference.getKey());

                        if (preference.getKey().equals("deviceFirmwareVersion")) {

                            Device device = ActivityNetwork.getDeviceByUID(deviceId);
                            if (device.getDeviceFirmwareVersion().trim().length() == 0) {
                                //no firmware version information available
                                return false;
                            }

                            firmwareUpdateBuilder = new AlertDialog.Builder(getActivity());
                            firmwareUpdateBuilder.setTitle("Firmware Update");
                            Toast.makeText(getActivity(), "Checking available firmware...", Toast.LENGTH_SHORT).show();

                            firmwareUpdateBuilder.setIcon(R.drawable.ic_launcher_background);
                            final StringBuffer selectedVersion = new StringBuffer();
                            firmwareUpdateBuilder.setSingleChoiceItems(versionArrayAdapter, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedVersion.setLength(0);
                                    selectedVersion.append(versionArrayAdapter.getItem(which));
                                    Log.v(LOG_TAG, "Your Selected Item is "+selectedVersion.toString());
                                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                }
                            });

                            new DownloadVersionsFromURL().execute(baseUrl+"/"+deviceCode+"/"+"versions");

                            firmwareUpdateBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            firmwareUpdateBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            AlertDialog alert = new AlertDialog.Builder(getActivity())
                                                    .setTitle("Confirm firmware update").setMessage("Device MUST REMAIN powered on during update!!\n\r\n\rDo you want to update?")
                                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            String url = baseUrl+"/"+deviceCode+"/"+selectedVersion;
                                                            Log.v(LOG_TAG, "onPreferenceClick UPDATING........"+url);
                                                            byte[] boot_after_update = {1};// MUST BE 1 for successful firmware update
                                                            createTaskForSetConfiguration(UDPPacket.DEVICE_COMMAND_FIRMWARE_UPDATE,
                                                                    boot_after_update,
                                                                    UDPPacket.shortToBytes((short) url.length()),
                                                                    url.getBytes());
                                                        }
                                                    })
                                                    .setNegativeButton("NO", null)
                                                    .create();
                                            alert.show();
                                        }
                                    });
                            AlertDialog dialog = firmwareUpdateBuilder.create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                @Override
                                public void onShow(DialogInterface dialog) {
                                    ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                }
                            });
                            dialog.show();
                        }
                    }
                    return false;
                }
            });

            getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object nValue) {

                    String newValue = nValue.toString().trim();
                    Log.v(LOG_TAG, "onPreferenceChange " + newValue);

                    try {
                        if (preference instanceof Preference) {

                            Log.v(LOG_TAG, "onPreferenceChange newValue " + newValue + ", len " + newValue.length());
                            Device device = ActivityNetwork.getDeviceByUID(deviceId);

                            if (preference.getKey().equals("controllerName")) {
                                device.setDeviceName(newValue);

                                createTaskForSetConfiguration(UDPPacket.DEVICE_COMMAND_SET_CONFIGURATION_NAME, device.getDeviceName().getBytes(), null, null);

                            } else if (preference.getKey().equals("controllerLocation")) {
                                device.setDeviceLocation(newValue);

                                createTaskForSetConfiguration(UDPPacket.DEVICE_COMMAND_SET_CONFIGURATION_LOCATION, device.getDeviceLocation().getBytes(), null, null);

                            } else if (preference.getKey().equals("controllerConnectAs")) {
                                device.setSSID(newValue);

                                createTaskForSetConfiguration(UDPPacket.DEVICE_COMMAND_SET_CONFIGURATION_SSID, device.getSSID().getBytes(), device.getPasswd().getBytes(), null);

                            } else if (preference.getKey().equals("controllerAPPass")) {
                                Log.v(LOG_TAG, "onPreferenceChange controllerAPPass " + newValue.length());
                                device.setPasswd(newValue);

                                createTaskForSetConfiguration(UDPPacket.DEVICE_COMMAND_SET_CONFIGURATION_SSID, device.getSSID().getBytes(), device.getPasswd().getBytes(), null);
                            }

                            Log.v(LOG_TAG, "preference.setSummary " + newValue);
                            preference.setSummary(newValue);
                        } else if (preference instanceof ListPreference) {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    return true;
                }
            });
        }
    }

    @Override
    public void processFinish(String output) {
        if(getActivity()!=null) {
            Toast.makeText(getActivity(), output, Toast.LENGTH_LONG).show();
        }
    }

    class DownloadVersionsFromURL extends AsyncTask<String, String, ArrayList<String>> {

        String exceptions = "";
        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(LOG_TAG, "Starting version file download");
            versionArrayAdapter.clear();

        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected ArrayList<String> doInBackground(String... f_url) {
            int count;
            ArrayList<String> versions = new ArrayList<>();
            try {

                Log.v(LOG_TAG, "Downloading "+f_url[0]);
                URL url = new URL(f_url[0]);

                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                Log.v(LOG_TAG, "Download content length "+conection.getContentLength());

                // input stream to read file
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                String str;
                while ((str = in.readLine()) != null) {
                    Log.v(LOG_TAG,"readLine "+str);

                    String[] tokens = str.split(",");
                    for(int i=0;i<tokens.length;i++) {
                        versions.add(tokens[i].trim());
                    }
                }

                // closing streams
                in.close();

            } catch (SocketTimeoutException e) {
                exceptions = e.getMessage();
                Log.e("Error ", e.getMessage());
            } catch (IOException e) {
                exceptions = e.getMessage();
                Log.e("Error ", e.getMessage());
            }

            return versions;
        }

        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(ArrayList<String> versions) {
            Log.v(LOG_TAG,"Downloaded "+versions);

            if(exceptions.startsWith(baseUrl)) {
                exceptions = "No firmware found!";
                Toast.makeText(getActivity(), exceptions, Toast.LENGTH_LONG).show();
            }

            for(int i=0;i<versions.size();i++) {
                if(versions.get(i).startsWith(deviceCode)) {
                    versionArrayAdapter.add(versions.get(i));
                }
            }

        }
    }

    private void createTaskForSetConfiguration(byte parameter, byte[] param1, byte[] param2, byte[] param3) {

        if (parameter != 0) {
            int index = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(parameter);

            if (param1 != null) {
                try {
                    baos.write(param1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                index += param1.length;
                Log.v(LOG_TAG, "onPreferenceChange param1.length() " + param1.length);
            }

            if (param2 != null) {
                try {
                    baos.write(param2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                index += param2.length;
                Log.v(LOG_TAG, "onPreferenceChange param2.length() " + param2.length);
            }

            if (param3 != null) {
                try {
                    baos.write(param3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                index += param3.length;
                Log.v(LOG_TAG, "onPreferenceChange param3.length() " + param3.length);
            }

            Log.v(LOG_TAG, "onPreferenceChange payload " + bytesToHex(baos.toByteArray()));
            Device device = ActivityNetwork.getDeviceByUID(deviceId);

            if(UDPPacket.DEVICE_COMMAND_FIRMWARE_UPDATE==parameter) {
                Toast.makeText(getActivity(), "Downloading firmware", Toast.LENGTH_LONG).show();
            }
            TaskSetConfiguration asyncTask = new TaskSetConfiguration(getActivity());
            asyncTask.delegate = this;
            asyncTask.execute(device.getUID(), new String(baos.toByteArray()));
        }

    }

    private void pickPreferenceObject(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                pickPreferenceObject(cat.getPreference(i));
            }
        } else {
            initSummary(p);
        }
    }

    private void initSummary(Preference p) {

        Device device = ActivityNetwork.getDeviceByUID(deviceId);
        if(device==null) {
            return;
        }

        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (editTextPref.getKey().equals("controllerName")) {

                editTextPref.setSummary(device.getDeviceName().trim());
                editTextPref.setText(device.getDeviceName().trim());

            } else if (editTextPref.getKey().equals("controllerLocation")) {

                editTextPref.setSummary(device.getDeviceLocation().trim());
                editTextPref.setText(device.getDeviceLocation().trim());

            } else if (editTextPref.getKey().equals("controllerMAC")) {

                editTextPref.setSummary(bytesToHex(device.getUIDRaw()));
                editTextPref.setText(bytesToHex(device.getUIDRaw()));

            } else if (editTextPref.getKey().equals("controllerAP")) {

                editTextPref.setSummary(device.getSSID().trim());
                editTextPref.setText(device.getSSID().trim());

            } else if (editTextPref.getKey().equals("controllerConnectAs")) {

                editTextPref.setSummary(device.getSSID().trim());
                editTextPref.setText(device.getSSID().trim());

            } else if (editTextPref.getKey().equals("controllerAPPass")) {

                editTextPref.setSummary(device.getPasswd().trim());
                editTextPref.setText(device.getPasswd().trim());

            }
        } else if (p instanceof Preference) {
            if (p.getKey().equals("deviceFirmwareVersion")) {
                p.setSummary(device.getDeviceFirmwareVersion().trim());
                if(device.getDeviceFirmwareVersion().trim().length()>0) {
                    deviceCode = device.getDeviceFirmwareVersion().trim().substring(0, device.getDeviceFirmwareVersion().trim().indexOf("."));
                    Log.v(LOG_TAG, "deviceCode " + deviceCode);
                }
            }
        } else if (p instanceof ListPreference) {
            if (p.getKey().equals("controllerConnectAs")) {
                Log.v(LOG_TAG, "onPreferenceChange ListPreference device.getSSID().length " + device.getSSID().trim().length());
                if (device.getSSID().trim().length() == 0) {
                    p.setSummary(getResources().getString(R.string.preference_connect_ap));
//                    ((ListPreference) p).setValueIndex(0);
//                    return;
                } else {
                    p.setSummary(device.getSSID().trim());
                }
/*
                CharSequence values[] = ((ListPreference) p).getEntryValues();
                int index = 0;
                for(int i=0; i<values.length; i++) {
                    if(device.getSSID().startsWith((String) values[i])) {
                        index = i;
                        break;
                    }
                    Log.v(LOG_TAG, "onPreferenceChange ListPreference dvalues " + values[i]);
                }
                p.setSummary(device.getSSID());
                ((ListPreference) p).setValueIndex(index);*/
            }
        }

        // More logic for ListPreference, etc...
    }
}
