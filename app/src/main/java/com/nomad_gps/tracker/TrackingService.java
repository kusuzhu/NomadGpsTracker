package com.nomad_gps.tracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.nomad_gps.database.DBHelper;
import com.nomad_gps.database.ServerConfig;
import com.nomad_gps.util.ServiceManager;
import com.nomad_gps.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by kuandroid on 7/3/15.
 */
public class TrackingService extends Service implements LocationListener {
    private final static int SERVER_AUTHORIZATION_ACCEPT_CODE=1;
    private TeltonikaEmulator emulator;
    private LocationManager locationManager;
    private Socket curSocket;
    private String host;
    private int port,satellite;
    private boolean sosMode,intervalInTimeMode;
    private int gpsGetInt,gpsSendInt;
    private static final String TAG = TrackingService.class.getSimpleName();
    private DBHelper dbHelper;
    private final IBinder iBinder = new Binder();
    private Handler handlerSender;
    private Location lastLocation;
    private Runnable runnableSender;
    private int battery_status;
    private ArrayList<PointRecord> pointsCache = new ArrayList<>();
    @Override
    public void onCreate(){
        super.onCreate();
        dbHelper = DBHelper.instiniate(this);
        emulator = getEmulator();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                if (gpsStatus != null) {
                    Iterable<GpsSatellite> satel = gpsStatus.getSatellites();
                    Iterator<GpsSatellite> sat = satel.iterator();
                    int satelcount = 0;
                    while (sat.hasNext()) {
                        satelcount++;
                    }
                    satellite = satelcount;
                }
            }
        });
        updateSettings();
    }
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent==null || intent.getExtras()==null) {
            stopService();
            return 0;
        }
        int actionType = intent.getExtras().getInt(Constants.ACT_TYPE);
        switch (actionType){
            case Constants.ACT_START_SERVICE:
                startService();
                break;
            case Constants.ACT_STOP_SERVICE:
                stopService();
                break;
            case Constants.ACT_ENABLE_SOS:
                setSosMode(true);
                break;
            case Constants.ACT_DISABLE_SOS:
                setSosMode(false);
                break;
            case Constants.ACT_PREF_CHANGED:
                updateSettings();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        locationManager.removeUpdates(this);
    }
    @Override
    public void onLocationChanged(Location location) {
        if (!intervalInTimeMode){
            if (lastLocation==null){
                lastLocation = location;
            }else if (lastLocation.distanceTo(location)>=gpsSendInt) {
                handlerSender.postDelayed(runnableSender, 0);
                lastLocation = location;
            }
        } else {
            lastLocation = location;
            PointRecord record = new PointRecord();
            record.setTimestamp((int) (System.currentTimeMillis() / 1000));
            record.setLatitude((int) (lastLocation.getLatitude() * 10000000));
            record.setLongitude((int) (lastLocation.getLongitude() * 10000000));
            record.setAltitude((int) lastLocation.getAltitude());
            record.setSpeed((int) (lastLocation.getSpeed() * 3.6));
            record.setAngle((int) lastLocation.getBearing());
            record.setPriority(isSosModeEnabled() ? 1 : 0);
            record.setBattery(battery_status);
            record.setSatellites(satellite);
            record.setSignal(0);
            dbHelper.addRecord(record);
            sendBufferCount(dbHelper.getRecords(0).size());
            sendLocation(lastLocation);
            Log.i(TAG, record.toString());
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG,provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, provider);
        sendStatus(Constants.STATUS_GPS_DISABLED);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }
    private void setSosMode(boolean sosMode){
        this.sosMode=sosMode;
    }
    private boolean isSosModeEnabled(){
        return sosMode;
    }

    public class BatteryLevelReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            battery_status = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
    };
    public class ConnectionChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            Log.e(TAG,"NETWORK RECEIVER");
            if (!checkInternet(context)){
                sendStatus(Constants.STATUS_NETWORK_DISABLED);
                if (handlerSender!=null)
                    handlerSender.removeCallbacks(runnableSender);
                runnableSender=null;
                handlerSender=null;
                resetSocket();
                stopSelf();
            }
        }
        boolean checkInternet(Context context) {
            ServiceManager serviceManager = new ServiceManager(context);
            if (serviceManager.isNetworkAvailable()) {
                return true;
            } else {
                return false;
            }
        }
    }
    private void startService(){
        handlerSender = new Handler();
        runnableSender = new Runnable() {
            @Override
            public void run() {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (intervalInTimeMode)
                            handlerSender.postDelayed(runnableSender, gpsSendInt * 1000);
                        if (authorize() && lastLocation!=null) {
                            sendStatus(Constants.STATUS_SUCCESS);
                            List<PointRecord> records = dbHelper.getRecords(0);
                            dbHelper.deleteAll();
                            pointsCache.addAll(records);
                            if (!sendPointsToServer(records)){
                                sendStatus(Constants.STATUS_ERROR);
                                dbHelper.addAll(records);
                            } else{
                                sendBufferCount(dbHelper.getRecords(0).size());
                            }
                            resetSocket();
                        }
                    }
                });
                thread.start();
            }
        };

        handlerSender.postDelayed(runnableSender, 0);
    }
    private void stopService(){
        sendStatus(Constants.STATUS_STOP);
        if (handlerSender!=null)
            handlerSender.removeCallbacks(runnableSender);
        runnableSender=null;
        handlerSender=null;
        resetSocket();
        stopSelf();
    }
    private void updateSettings(){
        Log.i(TAG,"Update Settings");
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        String address = ServerConfig.addresses[sharedPreferences.getInt(Constants.PREF_ADDRESS,0)];
        String[] splitStr = address.split(":");
        host = splitStr[0];
        port = Integer.parseInt(splitStr[1]);
        intervalInTimeMode = sharedPreferences.getBoolean(Constants.PREF_SEND_INTERVAL_MODE_IN_SEC, true);
        if (intervalInTimeMode)
            gpsSendInt = sharedPreferences.getInt(Constants.PREF_SEND_INTERVAL, Constants.DEF_GPS_SEND_SEC);
        else
            gpsSendInt = sharedPreferences.getInt(Constants.PREF_SEND_INTERVAL, Constants.DEF_GPS_SEND_METER);
        gpsGetInt = sharedPreferences.getInt(Constants.PREF_GET_INTERVAL, Constants.DEF_GPS_GET_SEC);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsGetInt * 1000, 0, this);
    }
    private boolean authorize(){
        try {
            byte[] message = Converter.convertBytes(emulator.getAuthentication());
            resetSocket();
            Socket socket = getSocket();
            if (socket==null)
                return false;
            socket.getOutputStream().write(message);

            int authResultCode = new BufferedReader(new InputStreamReader(socket.getInputStream()),8192).read();
            if (authResultCode!=SERVER_AUTHORIZATION_ACCEPT_CODE){
                Log.w(TAG, "Connection rejected by server with result code: " + authResultCode);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            resetSocket();
            Log.w(TAG,e);
            return  false;
        }
        Log.i(TAG,"Authorization accepted");
        return true;
    }
    private Socket getSocket(){
        if(curSocket == null){
            try {
                curSocket = new Socket();
                //curSocket.connect(new InetSocketAddress("92.46.121.106", 9020),Constants.CONNECT_TIMEOUT);
                curSocket.connect(new InetSocketAddress(host, port),Constants.CONNECT_TIMEOUT);
                curSocket.setSoTimeout(Constants.READ_TIMEOUT);
            }catch(IOException e) {
                e.printStackTrace();
                sendStatus(Constants.STATUS_ERROR);
            }
        }
        return curSocket;
    }
    private void resetSocket(){
        if(curSocket != null)
            try {
                curSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                curSocket=null;
            }
        curSocket = null;
    }
    private void sendStatus(int status) {
        Intent intent = new Intent(Constants.ACTION);
        intent.putExtra(Constants.EXTRA_STATUS, status);
        sendBroadcast(intent);
        Log.i(TAG,"sendStatus:"+status);
    }
    private void sendBufferCount(int count){
        Intent intent = new Intent(Constants.ACTION);
        intent.putExtra(Constants.EXTRA_BUFFER_COUNT, count);
        sendBroadcast(intent);
    }
    private void sendLocation(Location location){
        Intent intent = new Intent(Constants.ACTION);
        intent.putExtra(Constants.EXTRA_LOCATION,location);
        sendBroadcast(intent);
    }
    private TeltonikaEmulator getEmulator(){
        String imei = Utils.getImei(this);
        if (imei==null){
            sendStatus(Constants.STATUS_ERROR);
            return null;
        }
        return new TeltonikaEmulator(imei);
    }
    private boolean sendPointsToServer(List<PointRecord> recordList){
        Log.i(TAG,"sendPointsToServer");
        List<PointRecord> recordList1 = recordList;
        byte[] message = Converter.convertBytes(emulator.generatePackage(recordList1));
        try{
            getSocket().getOutputStream().write(message);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(getSocket().getInputStream()),8192);
            int result;
            int cnt = 0;
            boolean check = false;

            while( cnt < 4 && (result = br.read()) != -1){
                Log.i(TAG,"result = " + result);
                cnt++;
                if(cnt == 4 && result > 0){
                    check=true;
                    break;
                }
            }
            if (!check) {
                Log.w(TAG, "ERROR SENDING PACKETS");
                return false;
            } else {
                Log.i(TAG, "PACKETS SENT SUCCESSFULLY");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }
}