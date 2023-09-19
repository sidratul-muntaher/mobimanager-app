package com.iis.mobimanagercocacola.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.iis.mobimanagercocacola.service.AppService;
import com.iis.mobimanagercocacola.service.ForegroundService;
import com.iis.mobimanagercocacola.utils.Constants;
import com.iis.mobimanagercocacola.utils.SetAlarm;

public class AppBroadcastReceiver  extends BroadcastReceiver {
    MediaPlayer mp;
    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.v("_mdm_", "AppBroadcastReceiver firebase mdm called");
//        Toast.makeText(context, "Time Up... Now Vibrating !!!",
//                Toast.LENGTH_LONG).show();
//        Vibrator vibrator = (Vibrator) context
//                .getSystemService(Context.VIBRATOR_SERVICE);
//        vibrator.vibrate(2000);

        Log.d("_mdm_alarm_", "<<<<<<<<<<<<<<AppBroadcastReceiver called >>>>>>>>>>>>>");

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Intent serviceIntent = new Intent(context, ForegroundService.class);
            ContextCompat.startForegroundService(context,serviceIntent);
        }else {
            Intent aIntent = new Intent(context, AppService.class);
            context.startService(aIntent);
        }

        SetAlarm.setRepeatingAlarm(context, Constants.getNextAlarmTime());
    }

}
