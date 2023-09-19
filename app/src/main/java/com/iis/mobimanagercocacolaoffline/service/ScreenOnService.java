package com.iis.mobimanagercocacolaoffline.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.iis.mobimanagercocacolaoffline.utils.Constants;
import com.iis.mobimanagercocacolaoffline.utils.SetAlarm;

public class ScreenOnService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);

    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String s=intent.getAction();
            if(s.equalsIgnoreCase(Intent.ACTION_SCREEN_ON))
            {
                Intent serviceIntent = new Intent(context, ForegroundService.class);
                ContextCompat.startForegroundService(context,serviceIntent);

                SetAlarm.setRepeatingAlarm(context, Constants.getNextAlarmTime());
                Log.d("alarm", "screen is on");
            }
        }
    };
}