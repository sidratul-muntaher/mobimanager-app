package com.iis.mobimanagercocacola.receiver;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.iis.mobimanagercocacola.service.AppService;
import com.iis.mobimanagercocacola.service.DataUsageReportingService;
import com.iis.mobimanagercocacola.service.ForegroundService;
import com.iis.mobimanagercocacola.service.ForegroundServiceForScreenService;
import com.iis.mobimanagercocacola.service.LocationJobService;
import com.iis.mobimanagercocacola.service.MDMStatusService;
import com.iis.mobimanagercocacola.service.ScreenOffService;
import com.iis.mobimanagercocacola.service.ScreenOnService;
import com.iis.mobimanagercocacola.utils.SessionManager;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.iis.mobimanagercocacola.utils.Constants.LOCATION_JOB_INFO_ID;
import static com.iis.mobimanagercocacola.utils.Constants.STATUS_JOB_INFO_ID;

public class BootReceiver extends BroadcastReceiver {
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v("_mdm_", "BootReceiver onReceive called");

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            Intent serviceIntent = new Intent(context, ForegroundService.class);
            ContextCompat.startForegroundService(context,serviceIntent);

            Intent screen = new Intent(context, ForegroundServiceForScreenService.class);
            ContextCompat.startForegroundService(context,screen);
            schedulingJob(context);

        }else {
            Intent aIntent = new Intent(context, AppService.class);
            context.startService(aIntent);

            Intent myIntent = new Intent(context, ScreenOffService.class);
            context.startService(myIntent);
            Intent lockintent = new Intent(context, ScreenOnService.class);
            context.startService(lockintent);
        }
    }

    public void schedulingJob(Context mContext){
        SessionManager sessionManager = new SessionManager(mContext);
        Log.d("_mdm_", "BootReceiver --> schedulingJob method is called");
        //cancel existing job
        JobScheduler cancelScheduler = (JobScheduler) mContext.getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        cancelScheduler.cancelAll();
//        cancelScheduler.cancel(STATUS_JOB_INFO_ID);
//        cancelScheduler.cancel(LOCATION_JOB_INFO_ID);
//        cancelScheduler.cancel(DATA_USES_JOB_INFO_ID);

        ComponentName dataUsesComponentName = new ComponentName(mContext.getApplicationContext(), DataUsageReportingService.class);
        JobInfo dataUsesJobInfo = new JobInfo.Builder(93, dataUsesComponentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler dataUsesJobScheduler = (JobScheduler) mContext.getApplicationContext().getSystemService(mContext.getApplicationContext().JOB_SCHEDULER_SERVICE);
        int dataUsesResultCode = dataUsesJobScheduler.schedule(dataUsesJobInfo);
        Log.v("_mdm_", "jobscheduler result code: " + dataUsesResultCode);



        // start job for status
        ComponentName statusComponent = new ComponentName(mContext, MDMStatusService.class);
        JobInfo statusJobInfo = new JobInfo.Builder(STATUS_JOB_INFO_ID, statusComponent)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true) // .setPeriodic(15 * 60 * 1000)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler statusScheduler = (JobScheduler) mContext.getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        int statusResultCode = statusScheduler.schedule(statusJobInfo);

        //start job for location
        ComponentName componentName = new ComponentName(mContext, LocationJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(LOCATION_JOB_INFO_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)  // for periodic schedule add this line after this line  : .setPeriodic(60 * 60 * 1000)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler scheduler = (JobScheduler) mContext.getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        if(resultCode == scheduler.RESULT_SUCCESS){
            Log.d("_mdm_", "location job successfully scheduled");
            sessionManager.setGetDataAt(Calendar.getInstance().getTimeInMillis());
        }else {
            Log.d("_mdm_", "location job schedule failed!");
        }

        if (resultCode == scheduler.RESULT_SUCCESS && statusResultCode == statusScheduler.RESULT_SUCCESS
                && dataUsesResultCode == dataUsesJobScheduler.RESULT_SUCCESS){
            Log.d("_mdm_", "after boot all job has scheduled successfully!");
            sessionManager.setGetDataAt(Calendar.getInstance().getTimeInMillis());
        }

    }


    public void startAlert(Context context, int intervalSec) {
        Intent intent = new Intent(context, AppBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 280192, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + (2 * 1000), intervalSec
                , pendingIntent);

        Log.d("_mdm_", "(BootReceiver.java) Alarm will set in " + intervalSec + " seconds");

//            alarmManager.cancel(pendingIntent);
    }
}
