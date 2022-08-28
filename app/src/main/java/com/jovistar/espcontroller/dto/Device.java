package com.jovistar.espcontroller.dto;

import com.jovistar.espcontroller.activity.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static com.jovistar.espcontroller.activity.Constants.bytesToHex;

/**
 * Created by jovika on 4/12/2018.
 */

public class Device {
    final String LOG_TAG = Device.class.getSimpleName();

    // example UID:2C3AE80622F3, the length is actually 12 chars when represented as string
    byte[] UID = new byte[6];// unique id (like mac address)
    byte[] SSID = new byte[UDPPacket.MAX_SIZE_SSID];
    byte[] passwd = new byte[UDPPacket.MAX_SIZE_SSID_KEY];
    byte[] deviceName = new byte[UDPPacket.MAX_SIZE_DEVICE_NAME];
    byte[] deviceLocation = new byte[UDPPacket.MAX_SIZE_DEVICE_LOCATION];
    byte[] deviceFirmwareVersion = new byte[UDPPacket.MAX_SIZE_DEVICE_NAME];

    String deviceIPAddress = "0.0.0.0";
    byte status;// on or off
    HashMap<String, DeviceController> controllers = new HashMap<>();

    public Device() throws Exception {

        setUID("00AACC");
        setDeviceLocation("unknown");
        setDeviceFirmwareVersion("unknown");
        setDeviceName("no name");
        setSSID("no ssid");
        setPasswd("no passwd");
        setDeviceIPAddress("0.0.0.0");
    }

    public Device(String id, String nam, String ss, String pas, String loc, String firmversion) throws Exception {
        // example UID:2C3AE80622F3, the length is actually 12 chars
        if (/*id.length() > UID.length ||*/ ss.length() > SSID.length
                || nam.length() > deviceName.length || loc.length() > deviceLocation.length
                || pas.length() > passwd.length || firmversion.length() > deviceFirmwareVersion.length) {
            throw new Exception("ss.length() "+ss+", SSID.length "+SSID.length+", nam.length() "+nam
            +", deviceName.length "+deviceName.length+", loc.length() "+loc+", deviceLocation.length "+deviceLocation.length
            +", pas.length() "+pas+", passwd.length "+passwd.length
                    +", firmversion.length() "+firmversion+", deviceFirmwareVersion.length "+deviceFirmwareVersion.length);
        }

        setUID(id);
        setDeviceLocation(loc);
        setDeviceName(nam);
        setSSID(ss);
        setPasswd(pas);
        setDeviceIPAddress(deviceIPAddress);
        setDeviceFirmwareVersion(firmversion);
    }

    public String getUID() {
        return bytesToHex(UID);
    }

    public void setUID(String val) throws Exception {

        if (val == null || val.length() <= 0 /*|| val.length() > UID.length*/) {
            throw new Exception(Constants.exception_max_length_uid);
        }

        byte[] bytes = Constants.hexToBytes(val);
        Arrays.fill(UID, (byte) 0);
        System.arraycopy(bytes, 0, UID, 0, bytes.length);
    }

    public byte[] getUIDRaw() {
        return (UID);
    }

    public String getSSID() {
        return new String(SSID);
    }

    public void setSSID(String val) throws Exception {
        if (val == null || val.length() <= 0 || val.length() > SSID.length) {
            throw new Exception();
        }

        Arrays.fill(SSID, (byte) 0);
        System.arraycopy(val.getBytes(), 0, SSID, 0, val.length());
    }

    public String getPasswd() {
        return new String(passwd);
    }

    public void setPasswd(String val) throws Exception {

        if (val == null || /*val.length() <= 0 || */val.length() > passwd.length) {
            throw new Exception(Constants.exception_max_length_ssidkey);
        }

        Arrays.fill(passwd, (byte) 0);
        System.arraycopy(val.getBytes(), 0, passwd, 0, val.length());
    }

    public String getDeviceName() {
        return new String(deviceName);
    }

    public void setDeviceName(String val) throws Exception {

        if (val == null || val.length() <= 0 || val.length() > deviceName.length) {
            throw new Exception(Constants.exception_max_length_device_name);
        }

        Arrays.fill(deviceName, (byte) 0);
        System.arraycopy(val.getBytes(), 0, deviceName, 0, val.length());
    }

    public String getDeviceLocation() {
        return new String(deviceLocation);
    }

    public String getDeviceFirmwareVersion() {
        return new String(deviceFirmwareVersion);
    }

    public void setDeviceLocation(String val) throws Exception {

        if (val == null || val.length() <= 0 || val.length() > deviceLocation.length) {
            throw new Exception(Constants.exception_max_length_device_location);
        }

        Arrays.fill(deviceLocation, (byte) 0);
        System.arraycopy(val.getBytes(), 0, deviceLocation, 0, val.length());
    }

    public void setDeviceFirmwareVersion(String val) throws Exception {

        if (val == null || val.length() <= 0 || val.length() > deviceFirmwareVersion.length) {
            throw new Exception(Constants.exception_max_length_device_name);
        }

        Arrays.fill(deviceFirmwareVersion, (byte) 0);
        System.arraycopy(val.getBytes(), 0, deviceFirmwareVersion, 0, val.length());
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte s) {
        status = s;
    }

    public String getDeviceIPAddress() {
        return deviceIPAddress;
    }

    public void setDeviceIPAddress(String deviceIPAddress) {
        deviceIPAddress = deviceIPAddress;

        this.deviceIPAddress = deviceIPAddress;
    }

    public int getControllerCount() {
        return controllers.size();
    }

    public DeviceController getControllerByName(String name) {
        return controllers.get(name);
    }

    public DeviceController getControllerAt(int i) {
        Iterator it = controllers.values().iterator();
        int index = 0;
        while (it.hasNext()) {
            DeviceController dc = (DeviceController) it.next();
            if (
                    i == index
//                    (dc.getPin()==0 && i==0)
//                    || (dc.getPin()==2 && i==1)
//                    || (dc.getPin()==3 && i==2)
            ) {
                return dc;
            }
            index++;
        }
        return null;
    }

    public void addController(DeviceController dc) {
        controllers.put(new String(dc.getName()), dc);
    }

    public boolean fromByteArray(byte[] array) {
        int offset = 0;

        byte isConfigured = array[offset++];

        Arrays.fill(UID, (byte) 0);
        System.arraycopy(array, offset, UID, 0, UID.length);
        offset += UID.length;

        Arrays.fill(SSID, (byte) 0);
        System.arraycopy(array, offset, SSID, 0, SSID.length);
        offset += SSID.length;

        Arrays.fill(passwd, (byte) 0);
        System.arraycopy(array, offset, passwd, 0, passwd.length);
        offset += passwd.length;

        Arrays.fill(deviceName, (byte) 0);
        System.arraycopy(array, offset, deviceName, 0, deviceName.length);
        offset += deviceName.length;

        Arrays.fill(deviceLocation, (byte) 0);
        System.arraycopy(array, offset, deviceLocation, 0, deviceLocation.length);
        offset += deviceLocation.length;

        Arrays.fill(deviceFirmwareVersion, (byte) 0);
        System.arraycopy(array, offset, deviceFirmwareVersion, 0, deviceFirmwareVersion.length);
        offset += deviceFirmwareVersion.length;

        return true;
    }

    public String toString() {

        return "UID:" + bytesToHex(getUIDRaw()) + ", SSID:" + getSSID() + ", PASS:" + getPasswd() + ", Device Name:" + getDeviceName()
                + ", Device Location:" + getDeviceLocation() + ", IP Address:" + getDeviceIPAddress() + ", Firmware ver:" + getDeviceFirmwareVersion()
                + ", Controllers:" + controllers.values();
    }
}
