package com.iis.mobimanagercedp.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Isfahani on 28-Mei-16.
 * Modified from AndroidHive.info
 */
public class SessionManager {
    private static String TAG = "mdm_test";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    long timeInMillis = 0;
    long timeInMillisJob = 0;


    private static final String PREF_NAME = "TOKEN";
    private static final String KEY_IS_SEND_TOKEN = "send_token";
    private static final String KEY_LAST_ALARM_TIME_IN_MILLIS = "alarmTimeInMillis";
    private static final String KEY_GET_DATA_AT = "last_data_job";
    private static final String KEY_HTTPS_DEVICE_ID_IS_SEND = "https_device_id";
    private static final String KEY_IS_CALLED_FROM_OCR = "isFromOcr";
    private static final String KEY_IMEI_WAS_SUBMITTED = "WAS_SUBMITTED";
    private static final String KEY_SEND_STATUS_DATA = "status_data_submit";

    private static final String  KEY_CONTEXT = "wContextKey";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void isTokenSend(boolean isSend) {
        editor.putBoolean(KEY_IS_SEND_TOKEN, isSend);
        editor.commit();
        Log.d(TAG, "User login session modified!");
    }

    public boolean isTokenSend(){
        return pref.getBoolean(KEY_IS_SEND_TOKEN, false);
    }

    public void setNewAlarmTimeInMillis(long timeInMillis) {
        editor.putLong(KEY_LAST_ALARM_TIME_IN_MILLIS, timeInMillis);
        editor.commit();
    }

    public long getLastAlarmTimeInMillis(){
        return pref.getLong(KEY_LAST_ALARM_TIME_IN_MILLIS, this.timeInMillis);
    }

    public void setGetDataAt(long timeInMillis) {
        editor.putLong(KEY_GET_DATA_AT, timeInMillis);
        editor.commit();
    }

    public long getLastGetDataTimeInMillis(){
        return pref.getLong(KEY_GET_DATA_AT, this.timeInMillisJob);
    }

    public void isDeviceIdSend_HTTPS(boolean isSend) {
        editor.putBoolean(KEY_HTTPS_DEVICE_ID_IS_SEND, isSend);
        editor.commit();
        Log.d(TAG, "User DeviceIdSend_HTTPS modified!");
    }

    public boolean isDeviceIdSend_HTTPS(){
        return pref.getBoolean(KEY_HTTPS_DEVICE_ID_IS_SEND, false);
    }


    public void isCalledFromOCR(boolean isFromOcr) {
        editor.putBoolean(KEY_IS_CALLED_FROM_OCR, isFromOcr);
        editor.commit();
        Log.d(TAG, "is from ocr:"+isFromOcr);
    }

    public boolean isCalledFromOCR(){
        return pref.getBoolean(KEY_IS_CALLED_FROM_OCR, false);
    }

    public void isIMEISubmitted(boolean submit) {
        editor.putBoolean(KEY_IMEI_WAS_SUBMITTED, submit);
        editor.commit();
        Log.d(TAG, "is from ocr:"+submit);
    }

    public boolean isIMEISubmitted(){
        return pref.getBoolean(KEY_IMEI_WAS_SUBMITTED, false);
    }

    public void shouldDataUsesInfoBeSend(boolean shouldSend) {
        editor.putBoolean(KEY_SEND_STATUS_DATA, shouldSend);
        editor.commit();
        Log.d(TAG, "is from ocr:"+shouldSend);
    }

    public boolean shouldDataUsesInfoBeSend(){
        return pref.getBoolean(KEY_SEND_STATUS_DATA, false);
    }
}