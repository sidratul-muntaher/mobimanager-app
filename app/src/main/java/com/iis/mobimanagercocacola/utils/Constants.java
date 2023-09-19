package com.iis.mobimanagercocacola.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Constants {
    public static final String FIREBASE_TOKEN = "firebase_token";
    public static final String TOKEN_SHOULD_SENT = "token_status";
    public static final String APP_PREFERENCE = "ObhaiAppPrefs";
    public static final int LOCATION_JOB_INFO_ID = 1234;
    public static final int STATUS_JOB_INFO_ID = 1235;
    public static final int DATA_USES_JOB_INFO_ID = 1236;
    public static final int ALARM_REQUEST_CODE = 280192;
    public static final long SHOULD_ALARM_TIME_DIFFERENCE = 30; //minute
    public static final long MINIMUM_DATA_GETTING_DIFFERENCE = 12; //minute



    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static String getCurrentDataTimeString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
//        formatter.setTimeZone(TimeZone.getTimeZone("America/Belize"));
        Log.d("https", "date&time: "+formatter.format(date)+" zone: "+formatter.getTimeZone());
        return formatter.format(date);
    }

    public static String getTodayDateString() {
        String todayDateString = "";
        Calendar calendar = Calendar.getInstance();
        String year = calendar.get(Calendar.YEAR) + "";
        String month = (calendar.get(Calendar.MONTH) + 1) + "";
        String day = calendar.get(Calendar.DAY_OF_MONTH) + "";
        todayDateString = year + "-" + month + "-" + day;
        return todayDateString;

    }

    public static boolean hasNetworkStatPermission(Context context){

        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }else {
            return false;
        }
    }

    public static String getFileNameByTIMEinMILI(long time, String fileType){
        Timestamp timestamp = new Timestamp(time);
        Date date = timestamp;
        String[] dateSplit = date.toString().split(" ")[0].split("-");
        String[] timeSplit = date.toString().split(" ")[1].split(":");


        final String fileName = dateSplit[0]+"_"+dateSplit[1]+"_"+dateSplit[2]+"_"+timeSplit[0]+"_"+timeSplit[1];

        return fileName+"."+fileType;
    }

    public static long getNextAlarmTime(){
        int timeInterval = 15*60*1000;

        return  Calendar.getInstance().getTimeInMillis()+timeInterval;
    }

    public static long compareTwoTimeStamps(java.sql.Timestamp newTime, java.sql.Timestamp oldTime)
    {
        long milliseconds_old = oldTime.getTime();
        long milliseconds_new = newTime.getTime();

        long diff = milliseconds_new - milliseconds_old;
        long diffMinutes = diff / (60 * 1000);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        Log.d("_mdm_service_", "difference in minute: "+diffMinutes+" diff: "+diff +" build_in: "+minutes);


        return minutes;
    }

}
