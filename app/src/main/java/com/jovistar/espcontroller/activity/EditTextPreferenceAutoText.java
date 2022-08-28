package com.jovistar.espcontroller.activity;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditTextPreferenceAutoText extends EditTextPreference {
    final String LOG_TAG = EditTextPreferenceAutoText.class.getSimpleName();
    private final AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this.getContext());
    EditText editText;
    private Context context;

    public EditTextPreferenceAutoText(Context context) {
        super(context);
        this.context = context;
    }

    public EditTextPreferenceAutoText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public EditTextPreferenceAutoText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    @Override
    protected View onCreateDialogView() {

        return super.onCreateDialogView();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            this.setSummary(getText());
        }

        ((ViewGroup) autoCompleteTextView.getParent()).removeView(autoCompleteTextView);
    }

    @Override
    protected void onBindDialogView(View view) {
        Log.v(LOG_TAG, "onBindDialogView " + getText());
        super.onBindDialogView(view);

        // Initialize the AutoCompleteTextView with the values (WiFi hotspots)
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(FragmentControllerSettings.getWiFiAPList()));
        list.add("list 1");
        list.add("list 2");
        list.add("list 3");
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteTextView.setWidth(460);
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                autoCompleteTextView.showDropDown();
                return false;
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText != null) {
                    editText.setText(s.toString());
                    Log.v(LOG_TAG, "afterTextChanged " + s.toString());
                }
            }
        });

        autoCompleteTextView.setAdapter(dataAdapter);

        // Initialize the AutoCompleteTextView with the current preference value
        autoCompleteTextView.setText(getText());

        view.setLayoutParams(new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

        // get the original EditText from the EditTexPreference
        editText = view.findViewById(android.R.id.edit);
        ViewGroup vg = (ViewGroup) editText.getParent();
        editText.setVisibility(View.GONE);

        // add the AutoCompleteTextView to the existing EditTextPreference
        vg.addView(autoCompleteTextView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
