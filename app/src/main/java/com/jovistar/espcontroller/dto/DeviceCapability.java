package com.jovistar.espcontroller.dto;

import java.util.Arrays;

import static com.jovistar.espcontroller.activity.Constants.PADDING;

/**
 * Created by jovika on 4/6/2018.
 */

public class DeviceCapability {
    private byte[] _name = new byte[UDPPacket.MAX_SIZE_CAPABILITY_NAME];
    private int _value_min = 0;//actual value sent from ESP8266 is short; using int to accomodate values upto 65,535
    private int _value_max = 0;//actual value sent from ESP8266 is short; using int to accomodate values upto 65,535
    private int _value = 0;//actual value sent from ESP8266 is short; using int to accomodate values upto 65,535

    public DeviceCapability() {
        Arrays.fill(_name, PADDING);
    }
/*
    public DeviceCapability(byte nm[], short min, short max, short val) {

        Arrays.fill(_name, PADDING);
        System.arraycopy(nm, 0, _name, 0, _name.length);
        _value_min = min;
        _value_max = max;
        _value = val;
    }
*/
    public int size() {
        return _name.length + 2 + 2 + 2;
    }

    public byte[] getName() {
        return (_name);
    }
/*
    public void setName(String name) {
        Arrays.fill(_name, PADDING);
        name.getBytes(0, name.length(), _name, 0);
    }
*/
    public int getMinValue() {
        return _value_min;
    }

    public int getMaxValue() {
        return _value_max;
    }

    public int getValue() {
        return _value;
    }

    public void setValue(int val) {
        _value = val & 0xFFFF;//convert to int
    }

    public void setValueMin(int val) {
        _value_min = val & 0xFFFF;//convert to int
    }

    public void setValueMax(int val) {
        _value_max = val & 0xFFFF;//convert to int
    }

    public String toString() {
        return ("_name: " + new String(_name).trim() + ", _value_min: " + _value_min + ", _value_max : " + _value_max + ", _value : " + _value);
    }
}
