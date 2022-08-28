package com.jovistar.espcontroller.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.activity.ActivityNetwork;
import com.jovistar.espcontroller.dto.Device;
import com.jovistar.espcontroller.dto.DeviceController;
import com.jovistar.espcontroller.dto.UDPPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import static com.jovistar.espcontroller.activity.Constants.bytesToHex;

/**
 * Created by jake on 2/7/16.
 */
public class TaskDiscoverControllers extends AsyncTask<Void, Void, Device> {

    final String LOG_TAG = TaskDiscoverControllers.class.getSimpleName();

    ActivityNetwork mContext = null;
    DiscoverControllerCallback mCallback;
    String uuid;

    ProgressDialog progressDialog;

    public TaskDiscoverControllers(ActivityNetwork context, DiscoverControllerCallback callback, String uuid) {
        this.mContext = context;
        this.mCallback = callback;
        this.uuid = uuid;
    }

    protected void onPreExecute() {
        // progress bar
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Searching for device details");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);//temporarily set true 31-JAN-18
        progressDialog.show();

        if (((WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled() == false) {
            Log.v(LOG_TAG, "onPreExecute isWifiEnabled=false");
            new TaskDiscoverWifiHotspots(mContext, mContext).execute(null, null);
        }
    }

    protected void onPostExecute(Device device) {
        progressDialog.dismiss();

        if (device == null) {
            // error
            // TODO
        } else {
            try {
                mCallback.onFoundController(device);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected Device doInBackground(Void... params) {
        DatagramSocket socket = null;
        Device device = null;

        try {

            device = ActivityNetwork.getDeviceByUID(uuid);

            InetAddress address = InetAddress.getByName(device.getDeviceIPAddress());

            UDPPacket request_header = new UDPPacket();
            request_header.setCommand(UDPPacket.DEVICE_COMMAND_GETALL_CONTROLLER);

            byte[] bytes = request_header.toByteArray();

            // send a packet with above contents to all on local network to specified port
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, mContext.getResources().getInteger(R.integer.udp_port));

            // open a socket
            Log.v(LOG_TAG, "sending packet " + bytesToHex(bytes));
            socket = new DatagramSocket(mContext.getResources().getInteger(R.integer.udp_port_response));
            socket.send(packet);

            // listen for a response
            byte[] response = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.setSoTimeout(mContext.getResources().getInteger(R.integer.request_timeout));

            UDPPacket responseHeader = new UDPPacket();

            try {
                Log.v(LOG_TAG, "Listening for a response");
                socket.receive(responsePacket);

                responseHeader.parse(responsePacket.getData());
            } catch (SocketTimeoutException e) {
                Log.w(LOG_TAG, "Socket timed out");
                socket.send(packet);

            }

            int index = 0;

            byte[] payload = responseHeader.getPayload();

            while (responseHeader.getPayload().length > index) {

                byte[] pload = new byte[payload.length - index];
                Arrays.fill(pload, (byte) 0);
                System.arraycopy(payload, index, pload, 0, payload.length - index);

                DeviceController dc = new DeviceController();
                int len = dc.fromByteArray(pload);
                Log.v(LOG_TAG, "DeviceController " + dc);
                device.addController(dc);
                Log.v(LOG_TAG, "Device controller count " + device.getControllerCount());
                index += len;
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in TaskDiscoverControllers doInBackground()");
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return device;
    }

    public interface DiscoverControllerCallback {
        void onFoundController(Device device);
    }
}
