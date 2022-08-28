package com.jovistar.espcontroller.activity;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditTextPreferenceWithButton extends EditTextPreference {
    final String LOG_TAG = EditTextPreferenceWithButton.class.getSimpleName();

    //Layout Fields
    private final AutoCompleteTextView spinner = new AutoCompleteTextView(this.getContext());
    private final LinearLayout layout = new LinearLayout(this.getContext());
    String newValue;
//    private final EditText editText = new EditText(this.getContext());
//    private final Button button = new Button(this.getContext());


    //Called when addPreferencesFromResource() is called. Initializes basic paramaters
    public EditTextPreferenceWithButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setPersistent(false);
//        layout.setOrientation(LinearLayout.HORIZONTAL);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        //this.setSummary(this.getText());
        Log.v(LOG_TAG, "onCreateView " + newValue);
//        this.setSummary(this.getText());
//        this.setText(this.getText());
        return super.onCreateView(parent);
    }

    //Create the Dialog view
    @Override
    protected View onCreateDialogView() {
        layout.addView(spinner);
        layout.setPadding(40, 2, 2, 2);

        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(FragmentControllerSettings.getWiFiAPList()));
        list.add("list 1");
        list.add("list 2");
        list.add("list 3");
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setWidth(460);
        spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.showDropDown();
            }
        });

        spinner.setAdapter(dataAdapter);
        return layout;
    }

    //Attach persisted values to Dialog
    @Override
    protected void onBindDialogView(View view) {
        Log.v(LOG_TAG, "onBindDialogView");
        super.onBindDialogView(view);
        //spinner.setText(getPersistedString("ABC"), TextView.BufferType.NORMAL);
    }

    //persist values and disassemble views
    @Override
    protected void onDialogClosed(boolean positiveresult) {
        super.onDialogClosed(positiveresult);
        if (positiveresult /*&& shouldPersist()*/) {
            if (spinner.getText() != null) {
                //persistString(spinner.getText().toString());
//                newValue = spinner.getText().toString();
                this.setSummary(spinner.getText());
//                setText(spinner.getText().toString());
                Log.v(LOG_TAG, "onDialogClosed " + spinner.getText());
            }
        }

        ((ViewGroup) spinner.getParent()).removeView(spinner);
//        ((ViewGroup) editText.getParent()).removeView(editText);
//        ((ViewGroup) button.getParent()).removeView(button);
        ((ViewGroup) layout.getParent()).removeView(layout);

        notifyChanged();
    }
}
