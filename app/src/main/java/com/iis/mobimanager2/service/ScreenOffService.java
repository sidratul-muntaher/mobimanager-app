package com.iis.mobimanager2.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ScreenOffService extends Service {
    public class MyBind extends Binder {
        public Service getMyBind()
        {
            return ScreenOffService.this;
        }
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s=intent.getAction();
            if(s.equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)){
                Log.d("alarm", "screen is off");
                Intent myintent=new Intent(ScreenOffService.this, ScreenOnService.class);
                Log.d("_service_", "onCreate: ScreenOnService");

//                myintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(myintent);
            }
        }
    };

    public void onCreate(){
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);
    }

    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
    }
    public IBinder onBind(Intent intent){
        return new MyBind();
    }

    public void set_layout_click(){

    }
}