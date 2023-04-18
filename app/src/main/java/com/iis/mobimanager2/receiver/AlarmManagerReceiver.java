package com.iis.mobimanager2.receiver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iis.mobimanager2.service.DataUsageReportingService;

public class AlarmManagerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("_mdm_", "AlarmManagerReceiver onReceive called");

        ComponentName componentName = new ComponentName(context.getApplicationContext(), DataUsageReportingService.class);
        JobInfo jobInfo = new JobInfo.Builder(93, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getApplicationContext().getSystemService(context.getApplicationContext().JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);
        Log.v("_mdm_", "jobscheduler result code: " + resultCode);

    }

}


