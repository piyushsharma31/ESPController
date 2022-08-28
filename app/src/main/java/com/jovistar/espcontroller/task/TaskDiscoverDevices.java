package com.jovistar.espcontroller.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.activity.ActivityNetwork;
import com.jovistar.espcontroller.dto.Device;
import com.jovistar.espcontroller.dto.UDPPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;

import static com.jovistar.espcontroller.activity.Constants.bytesToHex;

/**
 * Created by jake on 2/7/16.
 */
public class TaskDiscoverDevices extends AsyncTask<Void, Void, ArrayList<Device>> {

    final String LOG_TAG = TaskDiscoverDevices.class.getSimpleName();

    private ActivityNetwork mContext = null;
    DiscoverDeviceCallback mCallback;
    //HashMap<String, Device> deviceList = new HashMap<>();

    ProgressDialog progressDialog;

     // A task for discovering LED strips on the local network
     // callback callback implementation so we can say if we found a device
    public TaskDiscoverDevices(ActivityNetwork context, DiscoverDeviceCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public static InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    protected void onPreExecute() {
        ActivityNetwork.deviceList.clear();

        // progress bar
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Searching for devices on local network");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);//temporarily set true 31-JAN-18
        progressDialog.show();

        if (((WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled() == false) {
            Log.v(LOG_TAG, "onPreExecute isWifiEnabled=false");
            new TaskDiscoverWifiHotspots(mContext, mContext).execute(null, null);
        }

    }

    protected void onPostExecute(/*byte[] result*/ArrayList<Device> devices) {
        progressDialog.dismiss();

        if (devices == null) {
            // error
            // TODO
        } else {
            try {
                mCallback.onFoundDevice(devices);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected ArrayList<Device> doInBackground(Void... params) {
        DatagramSocket socket = null;
        Device device = null;

        try {

            // get broadcast address to send packet to all devices on network
            InetAddress address = getBroadcastAddress(mContext);

            // packet contents
            UDPPacket request_header = new UDPPacket();
            request_header.setCommand(UDPPacket.DEVICE_COMMAND_DISCOVER);

            byte[] bytes = request_header.toByteArray();

            // send a packet with above contents to all on local network to specified port
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, mContext.getResources().getInteger(R.integer.udp_port));

            Log.v(LOG_TAG, "sending packet " + bytesToHex(packet.getData()));

            // open a socket
            socket = new DatagramSocket(mContext.getResources().getInteger(R.integer.udp_port_response));
            socket.send(packet);

            // listen for a response
            byte[] response = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.setSoTimeout(mContext.getResources().getInteger(R.integer.request_timeout));

            UDPPacket response_header = new UDPPacket();
            long startTimeDiscovery = Calendar.getInstance().getTimeInMillis();

            // keep listening and sending packets until the LED strip responds
            Log.v(LOG_TAG, "Listening for a response");

            while (true) {

                // 3 seconds over! exit discovery
                // commented, discover in one shot. Android client has Refresh provision on UI
                /*if(Calendar.getInstance().getTimeInMillis() - startTimeDiscovery
                        >= mContext.getResources().getInteger(R.integer.discovery_timeout)) {

                    Log.w(LOG_TAG, "Discovery complete!");
                    break;
                }*/

                try {
                    socket.receive(responsePacket);

                } catch (SocketTimeoutException e) {
                    Log.w(LOG_TAG, "Socket timed out");

                    // no need to send broadcast again
                    //Log.v(LOG_TAG, "sending packet " + bytesToHex(packet.getData()));
                    //socket.send(packet);

                    //device = addDummy();
                    //continue;
                    break;
                }

                response_header.parse(responsePacket.getData());

                if (response_header.getCommand() == UDPPacket.DEVICE_COMMAND_DISCOVER) {
                    // found device, get the ip address of it and return it
                    InetAddress ipAddr = responsePacket.getAddress();

                    device = new Device();
                    device.fromByteArray(response_header.getPayload());
                    device.setDeviceIPAddress(ipAddr.getHostAddress());

                    ActivityNetwork.deviceList.put(device.getUID(), device);
//                    Log.d(LOG_TAG, "deviceList.added "+device);
                }

            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception in TaskDiscoverDevices doInBackground()");
            e.printStackTrace();

            /*try {
                device = addDummy();
            } catch (Exception e1) {
                e1.printStackTrace();
            }*/
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return new ArrayList<>(ActivityNetwork.deviceList.values());
    }

    private Device addDummy() throws Exception {

        if (ActivityNetwork.getDeviceCount() > 0) {
            return ActivityNetwork.getDeviceAt(0);
        }

        Device device = null;

        if (ActivityNetwork.deviceList.size() == 0) {
            device = new Device();
            device.setDeviceIPAddress("10.1.1.10");
            device.setStatus((byte) 0);
            device.setDeviceName("Heraki");
            device.setSSID("yureka");
            device.setUID("123456");
            device.setDeviceLocation("Hawaii");

        } else if (ActivityNetwork.deviceList.size() == 1) {
            device = new Device();
            device.setDeviceIPAddress("10.1.1.11");
            device.setStatus((byte) 0);
            device.setDeviceName("Drone");
            device.setSSID("Citilink");
            device.setUID("234567");
            device.setDeviceLocation("San Francisco");

        } else if (ActivityNetwork.deviceList.size() == 2) {
            device = new Device();
            device.setDeviceIPAddress("10.1.1.12");
            device.setStatus((byte) 0);
            device.setDeviceName("Rabri");
            device.setSSID("Tokoyo");
            device.setUID("3456778");
            device.setDeviceLocation("Toronto");

        }

        Log.e(LOG_TAG, "addDummy " + device);
        return device;
    }

    private String getIpAddress() {
        WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public interface DiscoverDeviceCallback {
        void onFoundDevice(ArrayList<Device> device);
    }

}
