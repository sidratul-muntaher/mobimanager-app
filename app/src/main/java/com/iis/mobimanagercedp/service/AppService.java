package com.iis.mobimanagercedp.service;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static com.iis.mobimanagercedp.utils.Constants.DATA_USES_JOB_INFO_ID;
import static com.iis.mobimanagercedp.utils.Constants.LOCATION_JOB_INFO_ID;
import static com.iis.mobimanagercedp.utils.Constants.STATUS_JOB_INFO_ID;

import com.iis.mobimanagercedp.utils.Constants;
import com.iis.mobimanagercedp.utils.SessionManager;

import java.sql.Timestamp;
import java.util.Calendar;

public class AppService extends Service {

    private Context mContext;
    private Timestamp timestamp_1;
    private  Timestamp timestamp_2;
    private SessionManager sessionManager;
    @Override
    public void onCreate() {
        super.onCreate();
        //create job scheduler for 15 min interval task


        mContext = this.getApplicationContext();


        timestamp_1 = new Timestamp(System.currentTimeMillis());
        sessionManager = new SessionManager(mContext);
        timestamp_2 = new Timestamp(sessionManager.getLastGetDataTimeInMillis());
        if (Constants.compareTwoTimeStamps(timestamp_1, timestamp_2) > Constants.MINIMUM_DATA_GETTING_DIFFERENCE) {
            sessionManager.setGetDataAt(Calendar.getInstance().getTimeInMillis());
            schedulingJob(mContext);
        }

//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("DEVICE MANAGER")
//                .setSmallIcon(R.drawable.home_64)
//                .setContentIntent(pendingIntent)
//                .build();
//        startForeground(1, notification);

    }

    public void schedulingJob(Context mContext){
        Log.d("_mdm_service_", "AppService --> schedulingJob method is called");
        //cancel existing job
        JobScheduler cancelScheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        cancelScheduler.cancelAll();
//        cancelScheduler.cancel(STATUS_JOB_INFO_ID);
//        cancelScheduler.cancel(LOCATION_JOB_INFO_ID);
//        cancelScheduler.cancel(DATA_USES_JOB_INFO_ID);


        // start job for status
        ComponentName statusComponent = new ComponentName(mContext, MDMStatusService.class);
        JobInfo statusJobInfo = new JobInfo.Builder(STATUS_JOB_INFO_ID, statusComponent)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true) // .setPeriodic(15 * 60 * 1000)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler statusScheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        statusScheduler.schedule(statusJobInfo);

        //start job for location
        ComponentName componentName = new ComponentName(mContext, LocationJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(LOCATION_JOB_INFO_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)  // for periodic schedule add this line after this line  : .setPeriodic(60 * 60 * 1000)
                .setPeriodic(15*60*1000)
                .build();
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        if(resultCode == scheduler.RESULT_SUCCESS){
            Log.d("_mdm_loc", "location job successfully scheduled");
        }else {
            Log.d("_mdm_loc", "location job schedule failed!");
        }

        ComponentName dataUsesComponentName = new ComponentName(mContext, DataUsageReportingService.class);
        JobInfo dataJobInfo = new JobInfo.Builder(DATA_USES_JOB_INFO_ID, dataUsesComponentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true) //  for periodic schedule add this line after this line  : .setPeriodic(60 * 60 * 1000)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler dataUsesScheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode_dataUses = dataUsesScheduler.schedule(dataJobInfo);
        if(resultCode_dataUses == dataUsesScheduler.RESULT_SUCCESS){
            Log.d("_mdm_", "send_data_uses job successfully scheduled");
        }else {
            Log.d("_mdm_", "send_data_uses job schedule failed!");
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("_mdm_service_", "AppService --> onStartCommand is called");
        timestamp_1 = new Timestamp(System.currentTimeMillis());
        sessionManager = new SessionManager(mContext);
        timestamp_2 = new Timestamp(sessionManager.getLastGetDataTimeInMillis());
        //if (Constants.compareTwoTimeStamps(timestamp_1, timestamp_2) > Constants.MINIMUM_DATA_GETTING_DIFFERENCE) {
            Log.d("_mdm_service_", "AppService --> service called");

            sessionManager.setGetDataAt(Calendar.getInstance().getTimeInMillis());

            schedulingJob(mContext);
      //  }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("_mdm_", "AppService --> onDestroy Called");

//        try {
//            startService(new Intent(mContext, AppService.class));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}

