package com.jovistar.espcontroller.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.dto.Device;
import com.jovistar.espcontroller.dto.DeviceController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.jovistar.espcontroller.activity.FragmentController.ARG_DEVICE;

/**
 * Created by jovika on 4/14/2018.
 */

public class ActivityController extends AppCompatActivity {
    final String LOG_TAG = ActivityController.class.getSimpleName();

    ActivityControllerAdapter mControllerAdapter;
    ViewPager mViewPager;
    Device device;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_view);

        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));

        device = ActivityNetwork.getDeviceByUID(getIntent().getStringExtra(ARG_DEVICE));

        Log.v(LOG_TAG, "device uid " + device.getUID());

        mControllerAdapter = new ActivityControllerAdapter(getSupportFragmentManager(), device.getUID());
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mControllerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the corresponding tab.
                        //getActionBar().setSelectedNavigationItem(position);
                    }
                });

        setTitle(device.getDeviceName());
    }

    private void showSaveAsFavoriteDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Save as favorite")
                .setMessage("Favorite name")
                .setView(taskEditText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String favName = String.valueOf(taskEditText.getText());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            for (int i = 0; i < device.getControllerCount(); i++) {
                                baos.write(device.getControllerAt(i).toByteArray(null));
                            }

                            storeFavorite(favName, device.getUID(), baos.toByteArray());
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    ArrayList<String> getAllFavorites(String deviceUID) {
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.preference_favorites), Context.MODE_PRIVATE);
        Map<String, ?> map = sharedPref.getAll();
        ArrayList<String> favMap = new ArrayList<>();
        for (String entry : map.keySet()) {
            if (entry.endsWith(deviceUID)) {
                favMap.add(entry.substring(0, entry.indexOf(deviceUID) - 1));
                Log.v(LOG_TAG, "getAllFavorites " + entry.substring(0, entry.indexOf(deviceUID) - 1));
            }
        }

        return favMap;
    }

    byte[] getFavorite(String name, String deviceUID) {
        int sz = 0;
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.preference_favorites), Context.MODE_PRIVATE);
        String stringFromSharedPrefs = sharedPref.getString(name + "^" + deviceUID, "");
        if (stringFromSharedPrefs.length() > 0) {
            byte[] ba = Base64.decode(stringFromSharedPrefs, Base64.DEFAULT);
            //System.arraycopy(ba, 0, value, 0, ba.length);
            Log.v(LOG_TAG, "getFavorite " + name + "^" + deviceUID + ", " + ba.length);
            sz = ba.length;
            return ba;
        }
        Log.v(LOG_TAG, "getFavorite NULL" + name + "^" + deviceUID);
        return null;
    }

    void storeFavorite(String name, String deviceName, byte[] value) {
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.preference_favorites), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name + "^" + device.getUID(), Base64.encodeToString(value, Base64.DEFAULT));
        editor.apply();
        Log.v(LOG_TAG, "storeFavorite " + name + "^" + deviceName);
    }

    private void showSavedFavoritesDialog(Context c) {
        ArrayList<String> al = getAllFavorites(device.getUID());
        final CharSequence[] cs = al.toArray(new CharSequence[al.size()]);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Pick a favorite")
                .setItems(cs/*R.array.preference_favorites*/, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        byte[] value = getFavorite(cs[which].toString(), device.getUID());

                        int index = 0;
                        //device.getControllerAt(0).parse(value);
                        for (int i = 0; i < device.getControllerCount(); i++) {

                            byte[] pload = new byte[value.length - index];
                            Arrays.fill(pload, (byte) 0);
                            System.arraycopy(value, index, pload, 0, pload.length);

                            for (int j = 0; j < device.getControllerCount(); j++) {
                                if (device.getControllerAt(j).getPin() == pload[0]) {
                                    int len = device.getControllerAt(j).fromByteArrayFavorite(pload);
                                    index += len;
                                    break;
                                }
                            }
                        }

                        refreshVisibleFragment();
                    }
                }).create();
        dialog.show();

/*    AlertDialog.Builder dialog = new AlertDialog.Builder(c);
        dialog.setTitle("Pick a favorite")
                .setItems(R.array.preference_favorites, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                }).create();
        dialog.show();*/
    }

    public void refreshVisibleFragment() {

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FragmentController fc = null;
        boolean setdone = false;

        for (int i = 0; i < mControllerAdapter.getCount(); i++) {
            fc = mControllerAdapter.getRegisteredFragment(i);
            if (fc != null /*&& fc.isVisible()*/) {
                ft.detach(fc);
                ft.attach(fc);
                DeviceController dc = device.getControllerAt(i);
                if (!setdone) {
                    fc.changeControllerCapability(null, null, -1);
                    setdone = true;
                }
            }
        }
        ft.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_controller, menu);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View view = findViewById(R.id.action_favorite);

                if (view != null) {
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            // Do something...
                            showSaveAsFavoriteDialog(ActivityController.this);

                            return true;
                        }
                    });
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showSavedFavoritesDialog(ActivityController.this);
                        }
                    });
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent it = new Intent(this, ActivityControllerSettings.class);
                it.putExtra(ARG_DEVICE, device.getUID());
                startActivity(it);
                return true;

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
