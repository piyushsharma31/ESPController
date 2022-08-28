package com.jovistar.espcontroller.dto;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static com.jovistar.espcontroller.activity.Constants.PADDING;
import static com.jovistar.espcontroller.dto.UDPPacket.bytesToShort;
import static com.jovistar.espcontroller.dto.UDPPacket.shortToBytes;

/**
 * Created by jovika on 4/8/2018.
 */

public class DeviceController {
    final String LOG_TAG = DeviceController.class.getSimpleName();

    // pin which controls these capabilities, also called controller id
    byte pin;
    byte[] name = new byte[16];
    byte numberOfCapabilities;

    // capabilities of the device which can be controlled by this class
    HashMap<String, DeviceCapability> capabilities = new HashMap<>();

    public DeviceController() {
        Arrays.fill(name, PADDING);
    }

    public byte[] getName() {
        return (name);
    }

    public void setName(String nm) {
        Arrays.fill( name, PADDING );
        nm.getBytes(0, nm.length(), name, 0);
    }

    public byte getPin() {
        return pin;
    }

    public void setPin(byte pin) {
        this.pin = pin;
    }

    public void addCapability(DeviceCapability dc) {
        capabilities.put(new String(dc.getName()), dc);
    }

    public DeviceCapability getCapabilityAt(int i) {
        Iterator it = capabilities.values().iterator();
        int index = 0;
        while (it.hasNext()) {
            DeviceCapability dc = (DeviceCapability) it.next();
            if (index++ == i) {
                return dc;
            }
        }
        return null;
    }

    public byte getCapabilitiesCount() {
        return this.numberOfCapabilities;
    }

    public void setNumberOfCapabilities(byte nos) {
        this.numberOfCapabilities = nos;
    }

    public boolean setCapability(String capname, int value) {
        capabilities.get(capname).setValue(value);
        return true;
    }

    // initialize the capabilities with provided data (capabilities) received from network device
    public int fromByteArray(byte[] array) {
        int offset = 0;

        capabilities.clear();

        pin = array[offset++];
        numberOfCapabilities = array[offset++];

        System.arraycopy(array, offset, name, 0, name.length);
        offset += name.length;

        int countOfCaps = 0;

        while (countOfCaps < numberOfCapabilities) {

            DeviceCapability dc = new DeviceCapability();

            System.arraycopy(array, offset, dc.getName(), 0, dc.getName().length);
            offset += dc.getName().length;

            byte[] bytes = new byte[2];
            System.arraycopy(array, offset, bytes, 0, 2);
            dc.setValueMin(bytesToShort(bytes));
            offset += 2;

            System.arraycopy(array, offset, bytes, 0, 2);
            dc.setValueMax(bytesToShort(bytes));
            offset += 2;

            System.arraycopy(array, offset, bytes, 0, 2);
            dc.setValue(bytesToShort(bytes));
            offset += 2;

            try {
                capabilities.put(new String(dc.getName()), dc);
                countOfCaps++;
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.v(LOG_TAG, "add capability " + dc);
        }

        Log.v(LOG_TAG, "parse " + toString());
        return offset;
    }

    public int fromByteArrayFavorite(byte[] array) {
        int offset = 0;

        if (pin != array[0]) {
            Log.v(LOG_TAG, "parseValues, wrong pin! " + pin + ", array pin " + array[0]);
            return offset;
        }

        pin = array[offset++];
        numberOfCapabilities = array[offset++];

        int countOfCaps = 0;

        while (countOfCaps < numberOfCapabilities) {

            byte[] name = new byte[getCapabilityAt(0).getName().length];
            System.arraycopy(array, offset, name, 0, name.length);
            offset += name.length;

            byte[] value = new byte[2];
            System.arraycopy(array, offset, value, 0, value.length);
            offset += value.length;

            DeviceCapability dc = getCapabilityByName(new String(name));
            dc.setValue(bytesToShort(value));

            countOfCaps++;
        }

        Log.v(LOG_TAG, "parseValues " + toString());
        return offset;
    }

    public byte[] toByteArray(String capabilityName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1];

        try {
            // write pin
            bytes[0] = pin;
            baos.write(bytes);

            // write number of capabilities (just 1 (capabilityName) in this case)
            bytes[0] = capabilityName == null ? numberOfCapabilities : 1;
            baos.write(bytes);

            // write controller name
            /* send only [PIN][NO. OF CAPS][CAP NAME][CAP VALUE] */

            Iterator it = capabilities.values().iterator();
            // write all capabilities
            while (it.hasNext()) {
                DeviceCapability dc = (DeviceCapability) it.next();

                if (capabilityName == null/*all capabilities*/ || capabilityName.equals(new String(dc.getName()))/*one capability*/) {

                    // write capability name MAX_SIZEOF_STRING (24 bytes)
                    bytes = new byte[dc.getName().length];
                    Arrays.fill(bytes, PADDING);
                    System.arraycopy(dc.getName(), 0, bytes, 0, dc.getName().length);

                    baos.write(bytes, 0, bytes.length);

                    bytes = shortToBytes((short)dc.getValue());
                    baos.write(bytes);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "toByteArray length " + baos.toByteArray().length);

        return baos.toByteArray();
    }

    /*
        public byte[] toByteArray() {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bytes = new byte[1];

            try {
                // write pin
                bytes[0] = pin;
                baos.write(bytes);

                // write number of capabilities
                bytes[0] = numberOfCapabilities;
                baos.write(bytes);

                // write name MAX_SIZEOF_STRING (24 bytes)
                bytes = new byte[MAX_SIZEOF_STRING];
                Arrays.fill(bytes, PADDING);
                System.arraycopy(getName().getBytes(), 0, bytes, 0, getName().getBytes().length);

                baos.write(bytes, 0, bytes.length);

                // write all capabilities
                for (int i = 0; i < capabilities.size(); i++) {

                    // write capability name MAX_SIZEOF_STRING (24 bytes)
                    bytes = new byte[MAX_SIZEOF_STRING];
                    Arrays.fill(bytes, PADDING);
                    System.arraycopy(capabilities.get(i).getName().getBytes(), 0, bytes, 0, capabilities.get(i).getName().getBytes().length);

                    baos.write(bytes, 0, bytes.length);
                    Log.d(LOG_TAG, "toByteArray array " + capabilities.get(i).getName());

                    // write capability minimum value
                    bytes = shortToBytes(capabilities.get(i).getMinValue());
                    baos.write(bytes);
                    Log.d(LOG_TAG, "toByteArray array.length " + baos.toByteArray().length);

                    bytes = shortToBytes(capabilities.get(i).getMaxValue());
                    baos.write(bytes);
                    Log.d(LOG_TAG, "toByteArray array.length " + baos.toByteArray().length);

                    bytes = shortToBytes(capabilities.get(i).getValue());
                    baos.write(bytes);
                    Log.d(LOG_TAG, "toByteArray array.length " + baos.toByteArray().length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "toByteArray array.length " + baos.toByteArray().length);

            return baos.toByteArray();
        }
    */
    public DeviceCapability getCapabilityByName(String name) {
        return capabilities.get(name);
    }

    public String toString() {
        return ("pin: " + pin
                + ", name: " + new String(getName()).trim()
                + ", capabilities size: " + capabilities.size()
                + ", capabilities: " + capabilities.values());
    }
}
