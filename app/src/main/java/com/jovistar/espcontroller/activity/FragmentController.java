package com.jovistar.espcontroller.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.dto.DeviceCapability;
import com.jovistar.espcontroller.dto.DeviceController;
import com.jovistar.espcontroller.task.TaskSetController;

/**
 * Created by jovika on 4/15/2018.
 */

public class FragmentController extends Fragment {
    public static final String ARG_DEVICE = "deviceUID";
    public static final String ARG_INDEX = "fragmentIndex";
    final String LOG_TAG = FragmentController.class.getSimpleName();
    String deviceUID = null;
    int fragmentIndex = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        fragmentIndex = args.getInt(ARG_INDEX);// position of controller in device
        deviceUID = args.getString(ARG_DEVICE);// device uuid

        DeviceController dc = ActivityNetwork.getDeviceByUID(deviceUID).getControllerAt(fragmentIndex);
        Log.d(LOG_TAG, "getDeviceByUID  " + dc);

        LinearLayout ll = linearLayout();

        for (int i = 0; i < dc.getCapabilitiesCount(); i++) {
            DeviceCapability dcap = dc.getCapabilityAt(i);
            Log.d(LOG_TAG, "getCapabilityAt  " + i + ", " + dcap);

            // Add child view to parent RelativeLayout
            if (dcap.getMaxValue() - dcap.getMinValue() == 1) {

                //ll.addView(addButtonWidget(dcap.getMaxValue(), R.drawable.circle_grey, dcap.getName() ));
                ll.addView(addToggleButton(dcap.getValue(), R.drawable.circle_grey, new String(dcap.getName())));

            } else if (dcap.getMaxValue() - dcap.getMinValue() > 1) {

                ll.addView(addSeekbarWidget(dcap.getValue(), dcap.getMaxValue(),
                        R.drawable.ic_brightness_low_white,
                        new String(dcap.getName())));
            }
        }

        return ll;
    }

    RelativeLayout relativeLayout() {

        RelativeLayout relativeLayout = new RelativeLayout(this.getContext());

        //*** SET THE SIZE
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        //*** SET BACKGROUND COLOR JUST TO MAKE LAYOUT VISIBLE
        relativeLayout.setBackgroundColor(Color.GREEN);
        return relativeLayout;
    }

    LinearLayout linearLayout() {

        LinearLayout linearLayout = new LinearLayout(this.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        return linearLayout;
    }

    View addSeekbarWidget(int val, final int max, int drawableRes, final String name) {
        SeekBar seekBar = new SeekBar(getContext());
        seekBar.setMax(max);
        Drawable thumb = getResources().getDrawable(drawableRes);
        seekBar.setThumb(thumb);
        seekBar.setProgress(val);

        TextView nameTV = new TextView(getContext());
        nameTV.setText(name);
        nameTV.setId(10 + 40);

        final TextView valueTV = new TextView(this.getContext());
        valueTV.setText(String.format("%s",val));
        valueTV.setId(10 + 50);

        RelativeLayout rl = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rl.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams params_name = new RelativeLayout.LayoutParams(180, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params_seekbar = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params_value = new RelativeLayout.LayoutParams(80, ViewGroup.LayoutParams.WRAP_CONTENT);

        params_name.setMargins(30, 5, 30, 5);
        params_value.setMargins(30, 5, 30, 5);
        params_seekbar.setMargins(5, 30, 5, 30);

        params_name.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params_name.addRule(RelativeLayout.CENTER_IN_PARENT);
        params_seekbar.addRule(RelativeLayout.RIGHT_OF, nameTV.getId());
        params_seekbar.addRule(RelativeLayout.LEFT_OF, valueTV.getId());
        params_value.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params_value.addRule(RelativeLayout.CENTER_VERTICAL);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == false) {
                    return;
                }
                DeviceController dc = ActivityNetwork.getDeviceByUID(deviceUID).getControllerAt(fragmentIndex);
                changeControllerCapability(new String(dc.getName()), name, progress);
                valueTV.setText(String.format("%s",progress));
            }
        });

        rl.addView(nameTV, params_name);
        rl.addView(seekBar, params_seekbar);
        rl.addView(valueTV, params_value);

        return rl;
    }

    View addToggleButton(int val, int drawableRes, final String name) {
        ToggleButton button = new ToggleButton(getContext());
        button.setChecked(val > 0);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                DeviceController dc = ActivityNetwork.getDeviceByUID(deviceUID).getControllerAt(fragmentIndex);
                changeControllerCapability(new String(dc.getName()), name, b ? 1 : 0);
                Log.d(LOG_TAG, "onCheckedChanged  " + b);
            }
        });

        TextView nameTV = new TextView(getContext());
        nameTV.setText(name);
        nameTV.setId(10 + 60);

        final TextView valueTV = new TextView(this.getContext());
        valueTV.setText(Integer.toString(0));
        valueTV.setId(10 + 50);

        RelativeLayout rl = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rl.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams params_name = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params_button = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params_value = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params_name.setMargins(30, 5, 30, 5);
        params_value.setMargins(30, 5, 30, 5);
        params_button.setMargins(5, 30, 5, 30);

        params_name.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params_name.addRule(RelativeLayout.CENTER_IN_PARENT);
        params_button.addRule(RelativeLayout.RIGHT_OF, nameTV.getId());
        params_button.addRule(RelativeLayout.LEFT_OF, valueTV.getId());
        params_value.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params_value.addRule(RelativeLayout.CENTER_VERTICAL);

        rl.addView(nameTV, params_name);
        rl.addView(button, params_button);
        rl.addView(valueTV, params_value);

        return rl;
    }

    public void changeControllerCapability(String controllerName, String capabilityName, int value) {

//        DeviceController dc = ActivityNetwork.getDeviceByUID(deviceUID).getControllerAt(fragmentIndex);

        new TaskSetController(getContext()).execute(deviceUID,
                controllerName, capabilityName, Integer.toString(value));
    }
}
