package com.iis.mobimanagercocacola.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class DeviceManager extends Application {
    public static final String CHANNEL_ID = "mdm_service_channel";
    public static final String CHANNEL_ID_FOR_MESSAGE = "mdm_message_channel";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        createNotificationChannelForMessage();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mdmServiceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "MDM Service Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mdmServiceChannel);
        } else {

        }
    }

    private void createNotificationChannelForMessage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mdmServiceChannel = new NotificationChannel(
                    CHANNEL_ID_FOR_MESSAGE,
                    "MDM Message",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mdmServiceChannel);
        } else {

        }
    }
}

