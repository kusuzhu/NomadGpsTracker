package com.nomad_gps.tracker;

/**
 * Created by kuandroid on 7/3/15.
 */
public class Constants {
    //Connection
    public final static int CONNECT_TIMEOUT=50000,READ_TIMEOUT=50000;

    //Shared preferences
    public final static String SHARED_PREF="SHARED_PREFERENCES",PREF_GET_INTERVAL="GET_INTERVAL",
            PREF_SEND_INTERVAL="SEND_INTERVAL",PREF_ADDRESS="PREF_ADDRESS",
            PREF_SEND_INTERVAL_MODE_IN_SEC="INTERVAL_IN_SEC",PREF_PASSWORD="PREF_PWD",
            PREF_PASSWORD_ENABLED="PASSWORD_ENABLED";

    //Program settings
    public final static int DEF_GPS_SEND_SEC=5,DEF_GPS_SEND_METER=5,DEF_GPS_GET_SEC=5;

    //Action
    public final static String ACT_TYPE = "ACTION_TYPE",ACTION="ACTION";
    public final static int ACT_START_SERVICE=1,
            ACT_STOP_SERVICE=2, ACT_ENABLE_SOS=3, ACT_DISABLE_SOS=4,ACT_PREF_CHANGED=6;

    //Status
    public final static int STATUS_SUCCESS=1,STATUS_ERROR=-1,STATUS_STOP=0,
            STATUS_GPS_DISABLED=2, STATUS_NETWORK_DISABLED=3;
    public final static String EXTRA_STATUS="EXTRA_STATUS",EXTRA_BUFFER_COUNT="EXTRA_BUFFER_COUNT",
            EXTRA_LOCATION="EXTRA_LOCATION";
}
