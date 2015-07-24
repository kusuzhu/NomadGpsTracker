package com.nomad_gps.util;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Created by kuandroid on 7/8/15.
 */
public class Utils {
    public static String getImei(Context context){
        if (true)
            //return "356216043025859";
            return "874777730600000";
        String imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();
        if (imei==null){
            imei = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return imei;
    }
}
