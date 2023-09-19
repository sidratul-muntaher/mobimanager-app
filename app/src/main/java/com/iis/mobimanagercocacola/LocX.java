package com.iis.mobimanagercocacola;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iis.mobimanagercocacola.service.LocationJobService;

import java.util.Objects;

public class LocX extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {

        ComponentName serviceComponent = new ComponentName(context, LocationJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(1111, serviceComponent).setRequiresCharging(false)
                .setMinimumLatency(1000);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        Objects.requireNonNull(jobScheduler).schedule(builder.build());
        Log.e("TAG", "onReceive: -------------------------------- " );
    }
}
