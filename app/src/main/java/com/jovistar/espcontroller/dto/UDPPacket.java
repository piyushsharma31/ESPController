package com.jovistar.espcontroller.dto;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.jovistar.espcontroller.activity.Constants.bytesToHex;

/**
 * Created by jovika on 4/1/2018.
 * Class implements UDP datagram packet format, parsing and construction routines
 * between any Thing (ESP-01 based device) and Android controller client app
 */

public class UDPPacket implements Serializable {
    public static final int MAX_SIZE_DEVICE_UID = 16;
    public static final int MAX_SIZE_DEVICE_NAME = 16;
    public static final int MAX_SIZE_DEVICE_LOCATION = 16;
    public static final int MAX_SIZE_CAPABILITY_NAME = 16;
    public static final int MAX_SIZE_SSID = 24;
    public static final int MAX_SIZE_SSID_KEY = 24;
    // commands; 2-4, 10-14 is reserved
    public static byte DEVICE_COMMAND_DISCOVER = 0x01;
    public static byte DEVICE_COMMAND_SET_CONFIGURATION = 0x05;
    public static byte DEVICE_COMMAND_SET_CONFIGURATION_NAME = 0x6;
    public static byte DEVICE_COMMAND_SET_CONFIGURATION_SSID = 0x7;
    public static byte DEVICE_COMMAND_SET_CONFIGURATION_AP = 0x8;
    public static byte DEVICE_COMMAND_SET_CONFIGURATION_LOCATION = 0x9;
    public static byte DEVICE_COMMAND_GET_CONTROLLER = 0x0f;
    public static byte DEVICE_COMMAND_SET_CONTROLLER = 0x10;
    public static byte DEVICE_COMMAND_GETALL_CONTROLLER = 0x11;
    public static byte DEVICE_COMMAND_SETALL_CONTROLLER = 0x12;
    public static byte DEVICE_COMMAND_FIRMWARE_UPDATE = 0x13;
    final String LOG_TAG = UDPPacket.class.getSimpleName();
    // payload (Device object or Controller object)
    byte[] _payload;
    // size of this UDP packet
    private short _size;
    // request or command
    private byte _command = -1;

    public static short bytesToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static byte[] shortToBytes(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    public byte[] getPayload() {
        return _payload;
    }

    public void setPayload(byte[] pl) {
        _payload = new byte[pl.length];
        Arrays.fill(_payload, (byte) 0);
        System.arraycopy(pl, 0, _payload, 0, pl.length);
        setSize((short) (3 + pl.length));
    }

    public byte getCommand() {
        return _command;
    }

    public void setCommand(byte cc) {
        _command = cc;
        setSize((short) 3);
    }

    private void setSize(short sz) {
        _size = sz;
    }

    public byte[] toByteArray() {
        byte[] bytes = null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bytes = shortToBytes(_size);
            baos.write(bytes);

            bytes = ByteBuffer.allocate(1).put(_command).array();
            baos.write(bytes);

            if (_payload != null) {

                baos.write(_payload);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    public void parse(byte[] array) {
        if (array == null || array.length == 0)
            return;

        Log.v(LOG_TAG, "parse bytesToHex: " + bytesToHex(array));

        int index = 0;

        byte[] bytes = new byte[2];
        Arrays.fill(bytes, (byte) 0);
        System.arraycopy(array, index, bytes, 0, 2);
        _size = bytesToShort(bytes);
        index += 2;

        _command = array[index];
        index += 1;

        // 3 bytes less (size (2 byte) + command (1 byte)
        int payloadSize = _size - index;

        _payload = new byte[payloadSize];
        Arrays.fill(_payload, (byte) 0);
        System.arraycopy(array, index, _payload, 0, payloadSize);
        Log.v(LOG_TAG, "parse payloadSize " + payloadSize+ ", _payload: "+ new String(_payload));
    }

    public String toString() {
        return ("_size: " + _size + ", _command: " + _command + ", _payload len: " + _payload == null ? "0" : _payload.length + ", _payload: " + bytesToHex(_payload));
    }
}
