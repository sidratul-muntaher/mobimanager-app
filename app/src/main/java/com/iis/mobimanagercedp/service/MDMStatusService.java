package com.iis.mobimanagercedp.service;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;

import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;


import com.iis.mobimanagercedp.data.HttpsVolleyCallback;
import com.iis.mobimanagercedp.data.HttpsVolleyMethods;
import com.iis.mobimanagercedp.utils.ApiConstants;
import com.iis.mobimanagercedp.utils.AppPreference;

import java.util.Date;

import static com.iis.mobimanagercedp.utils.Constants.isNetworkAvailable;

public class MDMStatusService extends JobService {

    String imeiOne = null;
    String imeiTwo = null;
    Context mContext;

    @Override
    public boolean onStartJob(JobParameters params) {
        mContext = getApplicationContext();
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                imeiOne = AppPreference.getImeiOne(mContext);
                imeiTwo = AppPreference.getImeiTwo(mContext);
                Log.d("_mdm_", "MDMStatusService --> imeiOne : "+imeiOne+" imeiTwo : "+imeiTwo);
            } else{
                imeiOne = tm.getDeviceId(0);
                imeiTwo = tm.getDeviceId(1);

                Log.d("_mdm_", "MDMStatusService --> 2|  imeiOne : "+imeiOne+" imeiTwo : "+imeiTwo);
            }
                if (imeiOne != null && imeiTwo!= null) {
                    new AsyncTaskRunner().execute();
                }

        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public class AsyncTaskRunner extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {
            Log.v("_mdm_", "do in background, Network: " + isNetworkAvailable(getApplicationContext()));

            if (isNetworkAvailable(getApplicationContext())) {
                if (imeiOne != null && imeiOne.length() > 1) {
                    if (TextUtils.isEmpty(imeiTwo)){
                        imeiTwo = imeiOne;
                    }
                    Log.e("TAG", "doInBackground: ----- > " + imeiOne + " " + imeiTwo );
                    String timestamp = new Date().getTime() + "";
                    String STATUS_API = ApiConstants.getStatusInfoApi()+imeiOne+"/"+imeiTwo+"/online/"+timestamp;
                    Log.d("_https_", "Status api :"+STATUS_API);
                    HttpsVolleyMethods httpsVolleyMethods = new HttpsVolleyMethods();
                    httpsVolleyMethods.httpsPutRequestWithPathParams(mContext, STATUS_API, new HttpsVolleyCallback() {
                        @Override
                        public void success(String response) {
                            Log.d("_https_", "Status api response: "+response);
                        }
                    });

//                    sendStatusDataToServer(mContext);
                }
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
}
