package com.nomad_gps.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nomad_gps.R;
import com.nomad_gps.util.Utils;


public class ActivityMain extends Activity implements View.OnClickListener{
    private TextView tvIMEI,tvStatus,tvLat,tvLong,tvAlt,tvBear,tvBuff,tvBattery;
    private ToggleButton swSOS;
    private Button btnService;
    private TrackingReceiver receiver;
    private View progressBar,ivSettings;
    private static final String TAG = ActivityMain.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private boolean isServiceRunning=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF,Context.MODE_PRIVATE);
        initViews();
        initReceivers();
    }
    private class TrackingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.i(TAG,"onReceive");
            Bundle extras = intent.getExtras();
            if (extras.get(Constants.EXTRA_STATUS)!=null){
                progressBar.setVisibility(View.INVISIBLE);
                btnService.setClickable(true);
                switch(extras.getInt(Constants.EXTRA_STATUS)){
                    case Constants.STATUS_SUCCESS:
                        Log.i(TAG,"STATUS_SUCCESS");
                        tvStatus.setText(R.string.active);
                        tvStatus.setTextColor(getResources().getColor(R.color.green));
                        btnService.setText(R.string.stop_service);
                        isServiceRunning=true;
                        break;
                    case Constants.STATUS_STOP:
                        Log.i(TAG, "STATUS_STOP");
                        tvStatus.setText(R.string.passive);
                        tvStatus.setTextColor(getResources().getColor(R.color.orange));
                        btnService.setText(R.string.start_service);
                        isServiceRunning=false;
                        break;
                    case Constants.STATUS_ERROR:
                        Log.i(TAG,"STATUS_ERROR");
                        tvStatus.setText(R.string.conn_error);
                        tvStatus.setTextColor(getResources().getColor(R.color.red));
                        btnService.setText(R.string.start_service);
                        isServiceRunning=false;
                        break;
                    case Constants.STATUS_GPS_DISABLED:
                        Log.i(TAG,"STATUS_GPS_DISABLED");
                        tvStatus.setText(R.string.gps_disabled);
                        tvStatus.setTextColor(getResources().getColor(R.color.red));
                        btnService.setText(R.string.start_service);
                        isServiceRunning=false;
                        break;
                    case Constants.STATUS_NETWORK_DISABLED:
                        Log.i(TAG,"STATUS_NETWORK_DISABLED");
                        tvStatus.setText(R.string.network_disabled);
                        tvStatus.setTextColor(getResources().getColor(R.color.red));
                        btnService.setText(R.string.start_service);
                        isServiceRunning=false;
                        break;
                }
            }
            if (extras.get(Constants.EXTRA_BUFFER_COUNT)!=null){
                tvBuff.setText("\n"+extras.getInt(Constants.EXTRA_BUFFER_COUNT));
            }
            if (extras.get(Constants.EXTRA_LOCATION)!=null){
                Location location = (Location)extras.get(Constants.EXTRA_LOCATION);
                tvLat.setText(location.getLatitude()+"");
                tvLong.setText(location.getLongitude()+"");
                tvAlt.setText((int)location.getAltitude()+" м");
                tvBear.setText((int)location.getBearing()+"°");
            }
        }

    }
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int rawLevel = intent.getIntExtra("level", -1);
            tvBattery.setText(rawLevel+"%");
        }
    };

    private void initViews(){
        tvIMEI = (TextView)findViewById(R.id.tvIMEI);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        tvLat = (TextView)findViewById(R.id.tvLat);
        tvLong = (TextView)findViewById(R.id.tvLong);
        tvAlt = (TextView)findViewById(R.id.tvAltitude);
        tvBear = (TextView)findViewById(R.id.tvBearing);
        tvBuff = (TextView)findViewById(R.id.tvBuffer);
        tvBattery = (TextView)findViewById(R.id.tvBattery);
        progressBar = findViewById(R.id.progress);
        swSOS = (ToggleButton)findViewById(R.id.swSOS);
        btnService = (Button)findViewById(R.id.btnService);
        ivSettings = findViewById(R.id.ivSettings);

        if (isServiceRunning){
            btnService.setText(R.string.stop_service);
            tvStatus.setText(R.string.active);
            tvStatus.setTextColor(getResources().getColor(R.color.green));
        }else{
            btnService.setText(R.string.start_service);
            tvStatus.setText(R.string.passive);
            tvStatus.setTextColor(getResources().getColor(R.color.orange));
        }

        btnService.setOnClickListener(this);
        ivSettings.setOnClickListener(this);
        tvIMEI.setText(Utils.getImei(this));
        swSOS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(ActivityMain.this,TrackingService.class);
                if (isChecked){
                    intent.putExtra(Constants.ACT_TYPE, Constants.ACT_ENABLE_SOS);
                    startService(intent);
                }else{
                    intent.putExtra(Constants.ACT_TYPE,Constants.ACT_DISABLE_SOS);
                    startService(intent);
                }
            }
        });
    }
    private void initReceivers(){
        receiver = new TrackingReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION);
        registerReceiver(receiver, intentFilter);
        registerReceiver(mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    @Override
    public void onClick(View v) {
        Log.e(TAG,"VIEW CLICKED");
        switch (v.getId()){
            case R.id.btnService:
                if (isServiceRunning){
                    Intent intent = new Intent(this,TrackingService.class);
                    intent.putExtra(Constants.ACT_TYPE,Constants.ACT_STOP_SERVICE);
                    progressBar.setVisibility(View.VISIBLE);
                    btnService.setClickable(false);
                    startService(intent);
                }else{
                    Intent intent = new Intent(this,TrackingService.class);
                    intent.putExtra(Constants.ACT_TYPE,Constants.ACT_START_SERVICE);
                    progressBar.setVisibility(View.VISIBLE);
                    startService(intent);
                }
                break;
            case R.id.ivSettings:
                Log.e(TAG,"IVICON CLICKED");
                if (sharedPreferences.getBoolean(Constants.PREF_PASSWORD_ENABLED,false))
                    showPasswordDialog();
                else
                    startActivity(new Intent(ActivityMain.this, ActivitySettings.class));
                break;
            default:
                break;
        }
    }
    private void showPasswordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View rootView = getLayoutInflater().inflate(R.layout.dialog_pwd,null,false);
        final EditText etPwd = (EditText) rootView.findViewById(R.id.etPwd);
        builder.setView(rootView);
        builder.setTitle(R.string.password);
        builder.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (etPwd.getText().toString().
                        equals(sharedPreferences.getString(Constants.PREF_PASSWORD, ""))) {
                    dialog.dismiss();
                    startActivity(new Intent(ActivityMain.this, ActivitySettings.class));
                }
                else
                    showToast(getResources().getString(R.string.wrong_password));
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
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}