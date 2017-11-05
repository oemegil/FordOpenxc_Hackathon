package com.oemegil.smarttracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by oguzcan.emegil on 5.11.2017.
 */

public class EditThreshold  extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button ok, cancel;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    EditText txtAcceleratorThreshold,txtSteeringThreshold;
    public EditThreshold(Activity a,SharedPreferences viewpreferences,SharedPreferences.Editor vieweditor) {
        super(a);
        this.c = a;
        preferences=viewpreferences;
        editor=vieweditor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_threshold);
        ok = (Button) findViewById(R.id.btn_ok);
        txtSteeringThreshold=(EditText)findViewById(R.id.txtSteeringThreshold);
        txtAcceleratorThreshold=(EditText)findViewById(R.id.txtAcceleratorThreshold);
        cancel = (Button) findViewById(R.id.btn_cancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
        String steeringWheelThreasholdValuestr=preferences.getString("swThreashold","");
        String acceleratorThreasholdValuestr=preferences.getString("aThreashold","");
        txtSteeringThreshold.setText(steeringWheelThreasholdValuestr);
        txtAcceleratorThreshold.setText(acceleratorThreasholdValuestr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                editor.putString("swThreashold",txtSteeringThreshold.getText().toString());
                editor.putString("aThreashold",txtAcceleratorThreshold.getText().toString());
                editor.commit();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}