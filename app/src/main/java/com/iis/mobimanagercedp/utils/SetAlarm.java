package com.iis.mobimanagercedp.utils;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iis.mobimanagercedp.receiver.AppBroadcastReceiver;

import java.sql.Timestamp;
import java.util.Calendar;

public class SetAlarm {


    public static boolean setRepeatingAlarm(Context mContext, long alarmAt) {
        Timestamp timestamp_1 = new Timestamp(System.currentTimeMillis());
        SessionManager sessionManager = new SessionManager(mContext);
        Timestamp timestamp_2 = new Timestamp(sessionManager.getLastAlarmTimeInMillis());


        if (Constants.compareTwoTimeStamps(timestamp_1, timestamp_2) > Constants.SHOULD_ALARM_TIME_DIFFERENCE){
            sessionManager.setNewAlarmTimeInMillis(Calendar.getInstance().getTimeInMillis());

            Intent intent = new Intent(mContext, AppBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    mContext.getApplicationContext(), 280192, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + (2 * 1000), 15*60*1000
                    , pendingIntent);
            Log.d("alarm", "alarm set successfully");
            return  true;

        }else {
            Log.d("alarm", "alarm already set earlier");
            return false;

        }

    }

}
