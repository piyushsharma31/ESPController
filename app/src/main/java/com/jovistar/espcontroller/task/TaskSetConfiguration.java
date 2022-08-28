package com.jovistar.espcontroller.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.activity.ActivityNetwork;
import com.jovistar.espcontroller.dto.Device;
import com.jovistar.espcontroller.dto.UDPPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

import static com.jovistar.espcontroller.activity.Constants.bytesToHex;

public class TaskSetConfiguration extends AsyncTask<String, Void, Integer> {

    public AsyncResponse delegate = null;
    String exceptions = "";

    final static int SUCCESS = 0;
    final static int FAILURE = 1;

    final String LOG_TAG = TaskSetConfiguration.class.getSimpleName();

    private Context mContext;
    byte command = 0;
    byte[] param1 = new byte[16];
    byte[] param2 = new byte[16];
    String deviceUID = null;
    byte[] payload;

    public TaskSetConfiguration(Context context) {
        mContext = context;
    }

    public void onPostExecute(Integer result) {

        if (result == SUCCESS) {
            Log.v(LOG_TAG, "saved success deviceUID " + deviceUID + ", param1 " + param1 + ", param2 " + param2);
        } else {
            delegate.processFinish(exceptions);
            Log.e(LOG_TAG, "error " +exceptions);
        }

    }

    public Integer doInBackground(String... params) {
        deviceUID = params[0];
        payload = params[1].getBytes();

        Device device = ActivityNetwork.getDeviceByUID(deviceUID);

        InetAddress address;
        try {
            address = InetAddress.getByName(device.getDeviceIPAddress());
        } catch (Exception e) {
            Log.w(LOG_TAG, "No valid IP");
            exceptions = e.getMessage()==null?"No valid IP":e.getMessage();
            e.printStackTrace();
            return FAILURE;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            // write configuration parameter to be updated
            baos.write(payload);

            Log.d(LOG_TAG, "doInBackground command " + command + ", payload " + payload);//+", param2 "+param2+", deviceUID "+deviceUID);
        } catch (IOException e) {
            e.printStackTrace();
            exceptions = e.getMessage()==null?"IO Error":e.getMessage();
            return FAILURE;
        }

        UDPPacket udpPacket = new UDPPacket();
        udpPacket.setCommand(UDPPacket.DEVICE_COMMAND_SET_CONFIGURATION);
        //udpPacket.setAddressable(true);
        udpPacket.setPayload(baos.toByteArray());
        byte[] bytes = udpPacket.toByteArray();
        DatagramSocket socket = null;

        try {
            Log.v(LOG_TAG, "sending packet length " + bytes.length + ", to " + address.toString() + bytesToHex(bytes));

            socket = new DatagramSocket(mContext.getResources().getInteger(R.integer.udp_port_response));
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, mContext.getResources().getInteger(R.integer.udp_port));
            socket.send(packet);

            // listen for a response
            byte[] response = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.setSoTimeout(mContext.getResources().getInteger(R.integer.configuration_timeout));

            UDPPacket response_header = new UDPPacket();
            Log.v(LOG_TAG, "Listening for a response");
            socket.receive(responsePacket);

            response_header.parse(responsePacket.getData());

            if (response_header.getCommand() == UDPPacket.DEVICE_COMMAND_SET_CONFIGURATION) {
                if(response_header.getPayload().length > 0) {
                    exceptions = new String(response_header.getPayload());

                } else {
                    return SUCCESS;
                }
            }

        } catch (SocketTimeoutException e) {
            exceptions = "Socket Timeout Exception";
            e.printStackTrace();
        } catch (PortUnreachableException e) {
            exceptions = "Port Unreachable Exception";
            e.printStackTrace();
        } catch (IllegalBlockingModeException e) {
            exceptions = "Illegal Blocking Mode Exception";
            e.printStackTrace();
        } catch (IOException e) {
            exceptions = e.getMessage()==null?"Exception "+e.getLocalizedMessage():e.getMessage();
            e.printStackTrace();

        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return FAILURE;
    }
}
