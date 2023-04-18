package com.iis.mobimanager2.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.iis.mobimanager2.R;
import com.iis.mobimanager2.activity.MainActivity;

import static com.iis.mobimanager2.application.DeviceManager.CHANNEL_ID;

public class ForegroundService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("_mdm_","ForegroundService onCreate called ");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("DEVICE MANAGER")
                .setSmallIcon(R.drawable.home_64)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        Intent serviceIntent = new Intent(getApplicationContext(), AppService.class);
        startService(serviceIntent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("_mdm_","ForegroundService onDestroy called");
        stopForeground(true);
        stopSelf();
//        stopSelf();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}