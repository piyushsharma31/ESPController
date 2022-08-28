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
import java.net.SocketTimeoutException;

import static com.jovistar.espcontroller.activity.Constants.bytesToHex;

/**
 * Created by jake on 2/7/16.
 */
public class TaskSetController extends AsyncTask<String, Void, Integer> {

    final static int SUCCESS = 0;
    final static int FAILURE = 1;

    final String LOG_TAG = TaskSetController.class.getSimpleName();

    private Context mContext;

    String deviceUID;
    String controllerName;
    String capabilityName;
    int newCapabilityValue = -1;
    int currentCapabilityValue = -1;

    public TaskSetController(Context context) {
        mContext = context;
    }

    public void onPostExecute(Integer result) {

        if (result == SUCCESS) {
            Device device = ActivityNetwork.getDeviceByUID(deviceUID);
            Log.v(LOG_TAG, "getDeviceByUID " + deviceUID + ", device " + device);

            // set controller value to new from device
            if (newCapabilityValue >= 0)
                device.getControllerByName(controllerName).setCapability(capabilityName, newCapabilityValue);

            Log.v(LOG_TAG, "saved success deviceUID " + deviceUID + ", controller " + device.getControllerByName(controllerName));
        } else {
            Log.e(LOG_TAG, "error");
        }
    }

    public Integer doInBackground(String... params) {
        deviceUID = params[0];
        controllerName = params[1];
        capabilityName = params[2];
        newCapabilityValue = Integer.parseInt(params[3]);
        Log.d(LOG_TAG, "doInBackground deviceUID " + deviceUID + ", controllerName " + controllerName + ", capabilityName " + capabilityName + ", newCapabilityValue " + newCapabilityValue);

        Device device = ActivityNetwork.getDeviceByUID(deviceUID);

        InetAddress address;
        try {
            address = InetAddress.getByName(device.getDeviceIPAddress());
        } catch (Exception e) {
            Log.w(LOG_TAG, "No valid IP");
            e.printStackTrace();
            return FAILURE;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UDPPacket udpPacket = new UDPPacket();

        try {
            if (controllerName == null) {
                // setAllControllers
                for (int i = 0; i < device.getControllerCount(); i++) {
                    baos.write(device.getControllerAt(i).toByteArray(null));
                }

                udpPacket.setCommand(UDPPacket.DEVICE_COMMAND_SETALL_CONTROLLER);
            } else if (capabilityName == null) {
                // setController (controllerName)
                baos.write(device.getControllerByName(controllerName).toByteArray(null));

                udpPacket.setCommand(UDPPacket.DEVICE_COMMAND_SET_CONTROLLER);
            } else {
                // store and set the new capability value
                currentCapabilityValue = device.getControllerByName(controllerName).getCapabilityByName(capabilityName).getValue();
                device.getControllerByName(controllerName).setCapability(capabilityName, newCapabilityValue);

                // setCapability (controllerName, capabilityName, newCapabilityValue)
                baos.write(device.getControllerByName(controllerName).toByteArray(capabilityName));
                udpPacket.setCommand(UDPPacket.DEVICE_COMMAND_SET_CONTROLLER);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return FAILURE;
        }

        //udpPacket.setAddressable(true);
        udpPacket.setPayload(baos.toByteArray());
        byte[] bytes = udpPacket.toByteArray();
        DatagramSocket socket = null;

        try {
            Log.v(LOG_TAG, "sending packet length " + bytes.length + ", to " + address.toString() + ", " + bytesToHex(bytes));

            socket = new DatagramSocket(mContext.getResources().getInteger(R.integer.udp_port_response));
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, mContext.getResources().getInteger(R.integer.udp_port));
            socket.send(packet);

            // listen for a response
            byte[] response = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.setSoTimeout(mContext.getResources().getInteger(R.integer.request_timeout));

            UDPPacket response_header = new UDPPacket();
            //String text = "";
            try {
                Log.v(LOG_TAG, "Listening for a response");
                socket.receive(responsePacket);

                response_header.parse(responsePacket.getData());

                //text = new String(response, 0, responsePacket.getLength());
//                Log.v(LOG_TAG, "Received packet.  contents: " + response_header);
            } catch (SocketTimeoutException e) {
                Log.w(LOG_TAG, "Socket timed out");

                return FAILURE;
            }

            //if (text.equals("acknowledged")) {
            if (response_header.getCommand() == UDPPacket.DEVICE_COMMAND_SET_CONTROLLER
                    || response_header.getCommand() == UDPPacket.DEVICE_COMMAND_SETALL_CONTROLLER) {
                return SUCCESS;
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in TaskSetController doInBackground()");
            e.printStackTrace();

            return FAILURE;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        // reinstate the capability value
        if (currentCapabilityValue >= 0)
            device.getControllerByName(controllerName).setCapability(capabilityName, currentCapabilityValue);

        return FAILURE;
    }
}
