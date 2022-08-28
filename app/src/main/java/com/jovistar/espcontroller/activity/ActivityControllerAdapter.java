package com.jovistar.espcontroller.activity;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by jovika on 4/14/2018.
 */

public class ActivityControllerAdapter extends FragmentStatePagerAdapter {
    final String LOG_TAG = ActivityControllerAdapter.class.getSimpleName();
    SparseArray<FragmentController> registeredFragments = new SparseArray<FragmentController>();
    String deviceUID;

    public ActivityControllerAdapter(FragmentManager fm, String deviceUid) {
        super(fm);
        this.deviceUID = deviceUid;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new FragmentController();

        Bundle args = new Bundle();
        args.putString(FragmentController.ARG_DEVICE, deviceUID);
        args.putInt(FragmentController.ARG_INDEX, position);
        Log.v(LOG_TAG, "getItem ARG_DEVICE " + position + ", " + deviceUID);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return ActivityNetwork.getDeviceByUID(deviceUID).getControllerCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return new String(ActivityNetwork.getDeviceByUID(deviceUID).getControllerAt(position).getName());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FragmentController fragment = (FragmentController) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public FragmentController getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
