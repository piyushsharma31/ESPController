/*package com.jovistar.espcontroller.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.jovistar.espcontroller.dto.Device;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Persistence extends SQLiteOpenHelper implements BaseColumns {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "controller.db";
    public static final String TABLE_DEVICES = "devices";
    public static final String COLUMN_DEVICE_UID = "device_uid";
    public static final String COLUMN_DEVICE_SSID_PASS = "device_ssid_pass";
    public static final String COLUMN_DEVICE_SSID = "device_ssid";
    public static final String COLUMN_DEVICE_NAME = "device_name";
    public static final String COLUMN_DEVICE_LOCATION = "device_location";
    public static final String COLUMN_DEVICE_FIRMWAREVERSION = "device_firmware_version";
    public static final String COLUMN_DEVICE_ATTR1 = "attribute1";
    public static final String COLUMN_DEVICE_ATTR2 = "attribute2";
    public static final String COLUMN_DEVICE_ATTR3 = "attribute3";
    private static final String TAG = "Persistence";
    // sql lite data types
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES_DEVICES =

            "CREATE TABLE if not exists " + TABLE_DEVICES + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_DEVICE_UID + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_SSID + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_SSID_PASS + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_NAME + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_LOCATION + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_FIRMWAREVERSION + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_ATTR1 + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_ATTR2 + TEXT_TYPE + COMMA_SEP
                    + COLUMN_DEVICE_ATTR3 + TEXT_TYPE + ")";
    private static final String SQL_DELETE_ENTRIES_DEVICES = "DROP TABLE IF EXISTS "
            + TABLE_DEVICES;
    private static final String SQL_MAX_RECORD_ID_DEVICES = "SELECT * FROM "
            + TABLE_DEVICES + " WHERE " + _ID + " = (SELECT MAX(" + _ID
            + ") FROM " + TABLE_DEVICES + ")";
    private static Persistence instance = null;//single instance for Application
    Context context;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;

    private Persistence(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        SQLiteDatabase db = openDatabase();
        db.execSQL(SQL_CREATE_ENTRIES_DEVICES);
    }

    public static synchronized Persistence getInstance(Context context) {
        if (instance == null)
            instance = new Persistence(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_DEVICES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_DEVICES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = getWritableDatabase();
        }
        return mDatabase;
    }

    private synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
        }
    }

    public synchronized void clearDevices() {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_DEVICES, null, null);
        closeDatabase();
    }

    public synchronized long insertDevice(Device device) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long newRowId = -1;
        Log.v(TAG, "insertDevice() " + device.getUID() + ", " + device.getSSID() + ", " + device.getPasswd() + ", " + device.getDeviceName() + ", " + device.getDeviceLocation());

        // put the column values object
        values.put(COLUMN_DEVICE_UID, device.getUID());
        values.put(COLUMN_DEVICE_SSID, device.getSSID());
        values.put(COLUMN_DEVICE_SSID_PASS, device.getPasswd());
        values.put(COLUMN_DEVICE_NAME, device.getDeviceName());
        values.put(COLUMN_DEVICE_LOCATION, device.getDeviceLocation());
        values.put(COLUMN_DEVICE_FIRMWAREVERSION, device.getDeviceFirmwareVersion());

        // insert the values into the tables, returns the ID for the row
        newRowId = db.insert(TABLE_DEVICES, null, values);
        if (newRowId == -1) {
            Log.e(TAG, TABLE_DEVICES + " newRowId:" + newRowId
                    + ", failed for qresponse:" + device);
        }
        closeDatabase(); // close the db then deal with the result of the query
        return newRowId;
    }

    public synchronized long addDevice(Device device) throws Exception {
        Log.v(TAG, "addDevice() : " + device);
        Device device1 = getDeviceByUID(device.getUID());
        long rid = 0;

        if (device1 == null) {
            rid = insertDevice(device);
        } else {
            updateDevice(device);
        }

        return rid;
    }

    public ArrayList<Device> getAllDevices() throws Exception {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();
        ArrayList<Device> devices = new ArrayList<>();

        // columns to return
        String[] columns = {_ID, COLUMN_DEVICE_UID, COLUMN_DEVICE_SSID, COLUMN_DEVICE_SSID_PASS, COLUMN_DEVICE_NAME, COLUMN_DEVICE_LOCATION, COLUMN_DEVICE_FIRMWAREVERSION};

        Cursor c = db.query(TABLE_DEVICES, columns, null, null, null, null,
                sort + " DESC");

        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
                break;
            }

            Device dev = new Device(c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_UID)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_SSID)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_SSID_PASS)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_LOCATION)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_FIRMWAREVERSION)));

            devices.add(0, dev);
            Log.d(TAG, " device : " + devices.get(i));
        }

        c.close();
        closeDatabase();
        return devices;
    }

    public synchronized void deleteDevice(String id) {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_DEVICES, COLUMN_DEVICE_UID + "=?",
                new String[]{id});
        closeDatabase();
    }

    public synchronized long getCountDevices() {
        SQLiteDatabase db = openDatabase();

        long count = DatabaseUtils.queryNumEntries(db, TABLE_DEVICES);
        closeDatabase();
        return count;
    }

    public synchronized void updateDevice(Device device) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
        values.put(COLUMN_DEVICE_NAME, String.valueOf(device.getDeviceName()));
        values.put(COLUMN_DEVICE_LOCATION, String.valueOf(device.getDeviceLocation()));
        values.put(COLUMN_DEVICE_FIRMWAREVERSION, String.valueOf(device.getDeviceFirmwareVersion()));

        // update the values into the tables, returns the number of rows updated (must be always 1)
        noOfRowsUpdated = db.update(TABLE_DEVICES, values, COLUMN_DEVICE_UID + "=?",
                new String[]{(device.getUID())});
        if (noOfRowsUpdated != 1) {
            Log.e(TAG, "updateDevice, noOfRowsUpdated:" + noOfRowsUpdated + ", object:" + device);
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    public Device getDeviceByUID(String id) throws Exception {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();
        Log.v(TAG, "getDeviceByUID(), id: " + id);

        // columns to return
        String[] columns = {_ID, COLUMN_DEVICE_LOCATION, COLUMN_DEVICE_FIRMWAREVERSION,
                COLUMN_DEVICE_NAME, COLUMN_DEVICE_UID, COLUMN_DEVICE_SSID_PASS, COLUMN_DEVICE_SSID};
        String selection = COLUMN_DEVICE_UID + "=\"" + id + "\"";

        Cursor c = db.query(TABLE_DEVICES, columns, selection, null, null, null,
                sort + " DESC");
        Device obj = null;
        if (c.getCount() == 1) {

            c.moveToNext();
            // get data from cursor
            obj = new Device(c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_UID)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_SSID)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_SSID_PASS)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_LOCATION)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_DEVICE_FIRMWAREVERSION)));
            Log.d(TAG, "getDeviceByUID, id:" + obj);
        }

        c.close();
        closeDatabase();

        return obj;
    }
}
*/