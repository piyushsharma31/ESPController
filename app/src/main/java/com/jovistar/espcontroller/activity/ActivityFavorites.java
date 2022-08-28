package com.jovistar.espcontroller.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jovistar.espcontroller.R;

/**
 * Created by jovika on 7/28/2018.
 */

public class ActivityFavorites extends AppCompatActivity {
    final String LOG_TAG = ActivityFavorites.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_view);

        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));

        setTitle(getResources().getString(R.string.preference_favorites));
    }
}
