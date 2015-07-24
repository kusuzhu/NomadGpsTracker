package com.nomad_gps.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.nomad_gps.R;
import com.nomad_gps.database.ServerConfig;
import com.nomad_gps.views.NumPicker;

/**
 * Created by kuandroid on 7/17/15.
 */
public class ActivitySettings extends Activity implements View.OnClickListener {
    private View svSend,svGet,svPassword,svServer;
    private CheckBox cbPwd;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private View btnBack;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initViews();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.svSend:
                showSendIntervalDialog();
                break;
            case R.id.svGet:
                showGetIntervalDialog();
                break;
            case R.id.svPassword:
                showPasswordDialog();
                break;
            case R.id.svServer:
                showServerDialog();
                break;
            case R.id.back:
                finish();
                break;
        }
    }
    private void initViews(){
        svSend = findViewById(R.id.svSend);
        svGet = findViewById(R.id.svGet);
        svPassword = findViewById(R.id.svPassword);
        svServer = findViewById(R.id.svServer);
        cbPwd = (CheckBox)findViewById(R.id.cbPwd);
        btnBack = findViewById(R.id.back);

        btnBack.setOnClickListener(this);
        svSend.setOnClickListener(this);
        svGet.setOnClickListener(this);
        svPassword.setOnClickListener(this);
        svServer.setOnClickListener(this);
        cbPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    editor.putBoolean(Constants.PREF_PASSWORD_ENABLED, false);
                    editor.commit();
                }
                setPasswordFieldState(isChecked);
            }
        });
        boolean passwordEnabled = sharedPreferences.getBoolean(Constants.PREF_PASSWORD_ENABLED, false);
        setPasswordFieldState(passwordEnabled);
        cbPwd.setChecked(passwordEnabled);
    }
    private void setPasswordFieldState(boolean enabled){
        if (!enabled) {
            if (Build.VERSION.SDK_INT < 11) {
                final AlphaAnimation animation = new AlphaAnimation((float).3,(float) .3);
                animation.setDuration(0);
                animation.setFillAfter(true);
                svPassword.startAnimation(animation);
            } else
                svPassword.setAlpha((float) .3);
            svPassword.setClickable(false);
        }else {
            if (Build.VERSION.SDK_INT < 11) {
                final AlphaAnimation animation = new AlphaAnimation(1,1);
                animation.setDuration(0);
                animation.setFillAfter(true);
                svPassword.startAnimation(animation);
            } else
                svPassword.setAlpha(1);
            svPassword.setClickable(true);
        }

    }
    private void showGetIntervalDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View rootView = getLayoutInflater().inflate(R.layout.dialog_get_interval,null,false);
        builder.setView(rootView);
        builder.setTitle(getResources().getString(R.string.get_interval));
        final NumPicker numPicker1 = (NumPicker)rootView.findViewById(R.id.picker1);
        final NumPicker numPicker2 = (NumPicker)rootView.findViewById(R.id.picker2);
        int interval = sharedPreferences.getInt(Constants.PREF_GET_INTERVAL, Constants.DEF_GPS_GET_SEC);
        numPicker1.setCurValue(interval / 60);
        numPicker1.setMaxValue(59);
        numPicker1.setMinValue(0);
        numPicker1.setTitle("мин");
        numPicker2.setCurValue(interval % 60);
        numPicker2.setMaxValue(59);
        numPicker2.setMinValue(1);
        numPicker2.setTitle("сек");
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(Constants.PREF_GET_INTERVAL,
                        numPicker1.getCurValue() * 60 + numPicker2.getCurValue());
                editor.commit();
                dialog.dismiss();
                sendToService();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showSendIntervalDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View rootView = getLayoutInflater().inflate(R.layout.dialog_send_interval,null,false);
        builder.setView(rootView);
        builder.setTitle(getResources().getString(R.string.send_interval));
        final RadioGroup radioGroup = (RadioGroup)rootView.findViewById(R.id.rgMode);
        final NumPicker numPicker1 = (NumPicker)rootView.findViewById(R.id.picker1);
        final NumPicker numPicker2 = (NumPicker)rootView.findViewById(R.id.picker2);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbTime)
                    setSendTimeMode(rootView);
                else
                    setSendDistMode(rootView);
            }
        });
        if (sharedPreferences.getBoolean(Constants.PREF_SEND_INTERVAL_MODE_IN_SEC,true)){
            radioGroup.check(R.id.rbTime);
            int interval = sharedPreferences.getInt(Constants.PREF_SEND_INTERVAL, Constants.DEF_GPS_SEND_SEC);
            numPicker1.setCurValue(interval / 60);
            numPicker2.setCurValue(interval % 60);
        }else{
            radioGroup.check(R.id.rbDist);
            int interval = sharedPreferences.getInt(Constants.PREF_SEND_INTERVAL, Constants.DEF_GPS_SEND_METER);
            numPicker1.setCurValue(interval / 1000);
            numPicker2.setCurValue(interval % 1000);
        }
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (radioGroup.getCheckedRadioButtonId()==R.id.rbTime) {
                    editor.putBoolean(Constants.PREF_SEND_INTERVAL_MODE_IN_SEC, true);
                    editor.putInt(Constants.PREF_SEND_INTERVAL,
                            numPicker1.getCurValue() * 60 + numPicker2.getCurValue());
                } else {
                    editor.putBoolean(Constants.PREF_SEND_INTERVAL_MODE_IN_SEC, false);
                    editor.putInt(Constants.PREF_SEND_INTERVAL,
                            numPicker1.getCurValue() * 1000 + numPicker2.getCurValue());
                }
                editor.commit();
                dialog.dismiss();
                sendToService();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void setSendTimeMode(View dialog){
        NumPicker numPicker1 = (NumPicker)dialog.findViewById(R.id.picker1);
        NumPicker numPicker2 = (NumPicker)dialog.findViewById(R.id.picker2);
        RadioButton rbTime = (RadioButton)dialog.findViewById(R.id.rbTime);

        numPicker1.setMaxValue(59);
        numPicker1.setMinValue(0);
        numPicker1.setTitle("мин");

        numPicker2.setMaxValue(59);
        numPicker2.setMinValue(1);
        numPicker2.setTitle("сек");

        rbTime.setChecked(true);
    }
    private void setSendDistMode(View dialog){
        NumPicker numPicker1 = (NumPicker)dialog.findViewById(R.id.picker1);
        NumPicker numPicker2 = (NumPicker)dialog.findViewById(R.id.picker2);
        RadioButton rbDist = (RadioButton)dialog.findViewById(R.id.rbDist);

        numPicker1.setMaxValue(999);
        numPicker1.setMinValue(0);
        numPicker1.setTitle("км");

        numPicker2.setMaxValue(999);
        numPicker2.setMinValue(1);
        numPicker2.setTitle("м");

        rbDist.setChecked(true);
    }

    private void showPasswordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View rootView = getLayoutInflater().inflate(R.layout.dialog_password,null,false);
        final EditText etPwd1 = (EditText) rootView.findViewById(R.id.etPwd1);
        final EditText etPwd2 = (EditText) rootView.findViewById(R.id.etPwd2);
        builder.setView(rootView);
        builder.setTitle(getResources().getString(R.string.password));
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd1 = etPwd1.getText().toString();
                String pwd2 = etPwd2.getText().toString();
                if (pwd1 == null || pwd2 == null || pwd1.length() == 0 || pwd2.length() == 0)
                    showToast(getResources().getString(R.string.empty_password));
                else if (!pwd1.equals(pwd2))
                    showToast(getResources().getString(R.string.passwords_do_not_match));
                else {
                    editor.putBoolean(Constants.PREF_PASSWORD_ENABLED, true);
                    editor.putString(Constants.PREF_PASSWORD, pwd1);
                    editor.commit();
                    alertDialog.dismiss();
                }
            }
        });
    }
    private void showServerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View rootView = getLayoutInflater().inflate(R.layout.dialog_server,null,false);
        final Spinner spinner = (Spinner)rootView.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ServerConfig.addresses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(sharedPreferences.getInt(Constants.PREF_ADDRESS, 0), false);
        builder.setView(rootView);
        builder.setTitle(getResources().getString(R.string.server));
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(Constants.PREF_ADDRESS, spinner.getSelectedItemPosition());
                editor.commit();
                dialog.dismiss();
                sendToService();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
    private void sendToService(){
        Intent intent = new Intent(ActivitySettings.this,TrackingService.class);
        intent.putExtra(Constants.ACT_TYPE, Constants.ACT_PREF_CHANGED);
        startService(intent);
    }
}
