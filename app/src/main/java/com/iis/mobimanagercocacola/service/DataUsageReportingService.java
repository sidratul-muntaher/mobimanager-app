package com.iis.mobimanagercocacola.service;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.iis.mobimanagercocacola.data.AppDatabaseHelper;
import com.iis.mobimanagercocacola.data.HttpsVolleyCallback;
import com.iis.mobimanagercocacola.data.HttpsVolleyMethods;
import com.iis.mobimanagercocacola.utils.ApiConstants;
import com.iis.mobimanagercocacola.utils.AppPreference;
import com.iis.mobimanagercocacola.utils.AppUtil;
import com.iis.mobimanagercocacola.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

import static com.iis.mobimanagercocacola.utils.Constants.getTodayDateString;
import static com.iis.mobimanagercocacola.utils.Constants.isNetworkAvailable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataUsageReportingService extends JobService {

    NetworkStatsManager networkStatsManager;
    long wifiUsage;
    String imeiOne;
    String imeiTwo;
    long mobileUsage;
    AppDatabaseHelper databaseHelper;

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.v("_mdm_", " DataUsageReportingService onStartJob  called ");
        Context mContext = DataUsageReportingService.this;
        databaseHelper = new AppDatabaseHelper(getApplicationContext());
        long[] todayUses = {0,0};
        if (Constants.hasNetworkStatPermission(getApplicationContext())) {

            long[] todayRange = AppUtil.getTodayRange();
            long[] totalRange = { AppUtil.getTimestampFromDateString(AppPreference.getStartDate(mContext)), System.currentTimeMillis()};
            todayUses =  totalUsesData(mContext, todayRange);
            long[] totalUses =  totalUsesData(mContext, totalRange);

            Log.d("_dMDM_", "data uses today --->> mobile :"+todayUses[0]+" wifi :"+todayUses[1]);
            Log.d("_dMDM_", "data uses total --->> mobile :"+totalUses[0]+" wifi :"+totalUses[1]);


            wifiUsage = todayUses[1];
            mobileUsage = todayUses[0];
        }

        // get imei
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imeiOne = AppPreference.getImeiOne(getApplicationContext());
            } else {

                imeiOne = tm.getDeviceId(0);
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imeiTwo = AppPreference.getImeiTwo(getApplicationContext());
            } else {
                imeiTwo = tm.getDeviceId(1);
            }

        }


        if ((!TextUtils.isEmpty(imeiOne)) && (imeiOne.length() > 5)) {
            if ((!TextUtils.isEmpty(imeiTwo)) && (imeiTwo.length() > 5)){
                new AsyncTaskRunner(imeiOne, imeiTwo, todayUses[0], todayUses[1]).execute();
            }else {
                imeiTwo = imeiOne;

                new AsyncTaskRunner(imeiOne, imeiTwo, todayUses[0], todayUses[1]).execute();
            }

        }

        // send data to server
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public long getAllRxTXBytesMobile(Context context) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    AppUtil.getStartOfDayInMillis(),
                    AppUtil.getEndOfDayInMillis());


        } catch (Exception e) {
            return 0;
        }
        return bucket.getRxBytes() + bucket.getTxBytes();
    }

    //Here Manifest.permission.READ_PHONE_STATS is needed
    private String getSubscriberId(Context context, int networkType) {
        String subscriberId = null;
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                return subscriberId;
            } else {
                subscriberId = tm.getSubscriberId();
            }

        }
        return subscriberId;
    }

    public class AsyncTaskRunner extends AsyncTask<String, String, String> {
        String imei_One, imei_Two;
        long mobileData, wifiData;

        public AsyncTaskRunner(String imeiOne, String imeiTwo, long mobileData, long wifiData){
            imei_One = imeiOne;
            imei_Two = imeiTwo;
            this.mobileData = mobileData;
            this.wifiData = wifiData;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.v("_mdm_", "do in background, Network: " + isNetworkAvailable(getApplicationContext()));
            if (mobileData > 1 || wifiData > 1) {
//                String dataUsageString = imei + "," + mobileUsage + "," + getTodayDateString() + "," + wifiUsage + "#";
                if (databaseHelper.isDataAvailable(getTodayDateString())){
                    databaseHelper.updateUsesData(String.valueOf(mobileData), String.valueOf(wifiData), getTodayDateString());
                    Log.d("_mdm_", "Data Reporting -----> updateUsesData with field non-zero!");
                }else {
                    Log.d("_mdm_", "Data Reporting -----> insertUsesData with field non-zero!");
                    databaseHelper.insertSendUsesData(imei_One, imei_Two, String.valueOf(mobileData), String.valueOf(wifiData), getTodayDateString());
                }
            }else {
                if (databaseHelper.isDataAvailable(getTodayDateString())){
                    Log.d("_mdm_", "Data Reporting -----> updateUsesData with field zero!");
                    databaseHelper.updateUsesData("0", "0", getTodayDateString());
                }else {
                    Log.d("_mdm_", "Data Reporting -----> insertUsesData with field zero!");
                    databaseHelper.insertSendUsesData(imei_One, imei_Two, "0", "0", getTodayDateString());
                }
            }


            if (isNetworkAvailable(getApplicationContext())) {
                sendUsesDataToHTTPSServer(getApplicationContext());
            }

            return "nothing";
        }

        @Override
        protected void onPreExecute() {
            Log.v("_mdm_", "onPre execute called");
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v("_mdm_", "onPre execute called");
        }

    }

    private void sendUsesDataToHTTPSServer(Context mContext){
        ArrayList<HashMap<String, String>> data = databaseHelper.getSendUsesData();

        Log.d("_mdm_data_job_", "Data Reporting -----> today date : "+getTodayDateString());
        Log.d("_mdm_data_job_", "Data Reporting -----> total data : "+data.size());
        if (data.size() >= 1){
            JSONArray jsonArray = new JSONArray();
            Log.d("_database_", "got used data :"+data.get(0).toString());
            for (HashMap<String, String> map: data) {
                if (!map.get("date").equals(getTodayDateString())) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("imei", map.get("imei_one"));
                        jsonObject.put("imei2", map.get("imei_two"));
                        jsonObject.put("mobileData", map.get("mobile_data"));
                        jsonObject.put("wifiData", map.get("wifi_data"));
                        jsonObject.put("userDate", map.get("date"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(jsonObject);
                }
            }

            if (jsonArray.length() > 0){
                HttpsVolleyMethods volleyMethods = new HttpsVolleyMethods();
                String dataUsesApi = ApiConstants.getDataUsesApi();
                Log.d("_https_", "https data uses response: "+jsonArray.toString());
                volleyMethods.sendPostRequestToServer(mContext, dataUsesApi, jsonArray.toString(),
                        new HttpsVolleyCallback() {
                            @Override
                            public void success(String response) {
                                Log.d("_https_", "https data uses response: "+response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    long statusCode = jsonObject.getLong("statusCode");
                                    if (statusCode == 200 || statusCode == 201){
                                        AppDatabaseHelper appDatabaseHelper = new AppDatabaseHelper(mContext);
                                        appDatabaseHelper.deleteAllDataFromUsesDataTableExceptToday(getTodayDateString());
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.d("_https_", "https data uses send response: "+response);
                            }
                        });
            }
        }

    }

    private long[] totalUsesData(Context mContext, long[] range){
        long totalWifi = 0;
        long totalMobile = 0;
        if (Constants.hasNetworkStatPermission(mContext)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkStatsManager networkStatsManager = (NetworkStatsManager) mContext.getSystemService(Context.NETWORK_STATS_SERVICE);
                try {
                    if (networkStatsManager != null) {
                        NetworkStats networkStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, "", range[0], range[1]);
                        if (networkStats != null) {
                            while (networkStats.hasNextBucket()) {
                                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                                networkStats.getNextBucket(bucket);
                                totalWifi += (bucket.getTxBytes() + bucket.getRxBytes());
                            }
                        }
                        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            //                            NetworkStats networkStatsM = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, tm.getSubscriberId(), range[0], range[1]);
                            NetworkStats networkStatsM = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, range[0], range[1]);
                            if (networkStatsM != null) {
                                while (networkStatsM.hasNextBucket()) {
                                    NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                                    networkStatsM.getNextBucket(bucket);
                                    totalMobile += (bucket.getTxBytes() + bucket.getRxBytes());
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return new long[]{totalMobile, totalWifi};

        } else {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        return new long[]{totalMobile, totalWifi};
    }
}
