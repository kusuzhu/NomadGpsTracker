package com.nomad_gps.tracker;

/**
 * Created by kuandroid on 7/3/15.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
        if (messages.getMessageBody().toLowerCase().contains("nomad activate")) {
            Toast.makeText(context, "Nomad tracking enabled (SMS)",
                    Toast.LENGTH_LONG).show();

            context.startService(new Intent(context,TrackingService.class));
        }
    }
}