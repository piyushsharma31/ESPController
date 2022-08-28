package com.jovistar.espcontroller.activity;

import com.jovistar.espcontroller.dto.UDPPacket;

/**
 * Created by jake on 2/7/16.
 */
public class Constants {

    public static final byte PADDING = 0x00;
    public static final String exception_max_length_device_location = "length cannot be more than "+ UDPPacket.MAX_SIZE_DEVICE_LOCATION+" letters";
    public static final String exception_max_length_uid = "length cannot be more than "+ UDPPacket.MAX_SIZE_DEVICE_UID+" letters";
    public static final String exception_max_length_ssidkey = "length cannot be more than "+ UDPPacket.MAX_SIZE_SSID_KEY+" letters";
    public static final String exception_max_length_device_name = "length cannot be more than "+ UDPPacket.MAX_SIZE_DEVICE_NAME+" letters";

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static final String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return "0x00";

        String hexs = "";
        char[] hexChars = new char[2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            hexChars[0] = hexArray[v >>> 4];
            hexChars[1] = hexArray[v & 0x0F];
            hexs += new String(hexChars);
        }
        //return new String(hexChars);
        return hexs;
    }

    public static byte[] hexToBytes(String hexString) {
        byte[] val = new byte[hexString.length() / 2];
        for (int i = 0; i < val.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hexString.substring(index, index + 2), 16);
            val[i] = (byte) j;
        }
        return val;
    }
}
