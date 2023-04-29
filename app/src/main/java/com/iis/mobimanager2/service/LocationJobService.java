package com.iis.mobimanager2.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import android.telephony.TelephonyManager;
import android.util.Log;

import com.iis.mobimanager2.data.AppDatabaseHelper;
import com.iis.mobimanager2.data.HttpsVolleyCallback;
import com.iis.mobimanager2.data.HttpsVolleyMethods;
import com.iis.mobimanager2.utils.ApiConstants;
import com.iis.mobimanager2.utils.AppPreference;
import com.iis.mobimanager2.utils.Constants;
import com.iis.mobimanager2.utils.DeviceLocation;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.iis.mobimanager2.utils.Constants.isNetworkAvailable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationJobService extends JobService {


    AppDatabaseHelper appDatabaseHelper;
    String imeiOne = "";
    String imeiTwo = "";
    Context mContext;
    final String DEVICE_INFO_SENT_KEY_STRING = "device_info";


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v("_locJ_", " LocationJobService onStartJob  called ");

        mContext = getApplicationContext();
        appDatabaseHelper = new AppDatabaseHelper(getApplicationContext());
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                imeiOne = AppPreference.getImeiOne(mContext);
                imeiTwo = AppPreference.getImeiTwo(mContext);
                Log.d("_locJ_", "On Job service 1--> imeiOne :"+imeiOne+" imeiTwo :"+imeiTwo);
            } else{
                imeiOne = tm.getDeviceId(0);
                imeiTwo = tm.getDeviceId(1);

                Log.d("_locJ_", "On Job service 2--> imeiOne :"+imeiOne+" imeiTwo :"+imeiTwo);
            }


        }

        DeviceLocation.LocationResult locationResult = new DeviceLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if (location != null) {

                    Log.v("_locJ_", "  counter OK, location result lat: " + location.getLatitude() + " long: " + location.getLongitude());

                    String date = Constants.getTodayDateString();
                    String timestamp = new Date().getTime() + "";
//                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//                    long time = timestamp.getTime();
//
//                    Date my_date = new Date(time);
//                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
//                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
//                    String dateFormatted = formatter.format(my_date);

                    Date newTime = new Timestamp(Long.parseLong(timestamp));

                    String address = getAddressByLocation(location, mContext);

//                    Log.d("_mdm_", "timestamp : "+timestamp);
                    Log.d("_locJ_", "timestampToTime : "+newTime.toString());
                    if (imeiOne.length() > 5) {
                        appDatabaseHelper.insertSendLocationData(imeiOne, imeiTwo, location.getLatitude() + "", location.getLongitude() + "", address, Constants.getCurrentDataTimeString());

                    }
                    new AsyncTaskRunner().execute();
                } else {
                    // adding for test purpose
                    Log.v("_locJ_", "  counter , location null ");

                    String date = Constants.getTodayDateString();
                    String timestamp = new Date().getTime() + "";
//                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Log.d("_locJ_", "timestamp : "+timestamp);
//                    Log.d("_mdm_", "timestampToTime : "+timestamp.getTime());
                    if (imeiOne.length() > 5) {
                        appDatabaseHelper.insertSendLocationData(imeiOne, imeiTwo, "0000000", "00000", "unknown", Constants.getCurrentDataTimeString());

                    }
                    new AsyncTaskRunner().execute();
                }
            }
        };

        DeviceLocation myLocation = new DeviceLocation();

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }else {
            myLocation.getLocation(getApplicationContext(), locationResult);
        }

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Looper.loop();
            }
        }).start();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.v("_locJ_", " LocationJobService onStopJob  called ");
        return false;
    }


    public class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            Log.v("_locJ_", "do in background, Network: " + isNetworkAvailable(getApplicationContext()));


            if (isNetworkAvailable(getApplicationContext())) {
                AppDatabaseHelper databaseHelper = new AppDatabaseHelper(getApplicationContext());

                // if network available then collect from database and send data to server

                ArrayList<HashMap<String, String>> locationDataList = databaseHelper.getSendLocationData();

                JSONArray jsonArray = new JSONArray();
                if (locationDataList != null && locationDataList.size() > 0) {
                    // sendLocationData(arrayList);
                    Log.v("_locJ_", "getSendLocationData not null");

                    String allLocationString = "";


                    for (int a = 0; a < locationDataList.size(); a++) {
                        JSONObject jsonObject = new JSONObject();

                        try {
                            jsonObject.put("imeiOne", locationDataList.get(a).get("imei_one"));
                            jsonObject.put("imeiTwo", locationDataList.get(a).get("imei_two"));
                            jsonObject.put("latitude", locationDataList.get(a).get("latitude"));
                            jsonObject.put("longitude", locationDataList.get(a).get("longitude"));
                            jsonObject.put("address", locationDataList.get(a).get("address"));
                            jsonObject.put("userDataTime", locationDataList.get(a).get("dateTime"));
                            jsonObject.put("startDate", locationDataList.get(a).get("dateTime"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonArray.put(jsonObject);

                    }

                    if (jsonArray.length() > 0){
                        Log.v("_httpss_", "SendLocationData -->> json-array:"+jsonArray.toString());
                        String LOCATION_API = ApiConstants.getSaveLocationApi();
                        HttpsVolleyMethods httpsVolleyMethods = new HttpsVolleyMethods();
                        httpsVolleyMethods.httpsSendLocationPostRequest(mContext, LOCATION_API, jsonArray.toString(), new HttpsVolleyCallback() {
                            @Override
                            public void success(String response) {

                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    long statusCode = jsonObject.getLong("statusCode");
                                    if (statusCode == 200  || statusCode == 201){
                                        databaseHelper.deleteAllDataFromLocationTable();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.d("_https_", "location response :"+response);
                            }
                        });
//                        sendLocationDataToServer(getApplicationContext(), jsonArray.toString());
                    }


                } else {
                    Log.v("_locJ_", "getSendLocationData is null");
                }

            }


            return "nothing";
        }

        @Override
        protected void onPreExecute() {
            Log.v("_locJ_", "onPre execute called");
        }

        @Override
        protected void onPostExecute(String s) {

            Log.v("_locJ_", "onPre execute called");

        }

    }

    private void sendLocationDataToServer(Context mContext, final String allLocationString) {

//        RequestQueue queue = Volley.newRequestQueue(mContext);
//        String LOCATION_API = ApiConstants.getLocationApi(mContext);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, LOCATION_API, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                if (response.equalsIgnoreCase("updated") || response.equalsIgnoreCase("success")) {
//                    if (response.equalsIgnoreCase("success")) {
//
//                        // delete all data from location table
//
//                        appDatabaseHelper.deleteAllDataFromLocationTable();
//
//
//                        // send device info if not sent ever
////                        SharedPreferences preferences =
////                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
////                        boolean isDeviceInfoSent = preferences.getBoolean(DEVICE_INFO_SENT_KEY_STRING, false);
////                        if (!isDeviceInfoSent) {
////                            sendDeviceInfo(getApplicationContext());
////                        }
//                    }
//
//                }
//                Log.d("_mdm_", "sending location data response:" + response.toString());
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//
//                if (error instanceof NetworkError) {
//                    Log.d("_locJ_", "location sending response: Network error");
//
//                } else if (error instanceof ServerError) {
//                    Log.d("_locJ_", "location sending response: Server error");
//                } else if (error instanceof AuthFailureError) {
//                    Log.d("_locJ_", "location sending response: Auth. error");
//                } else if (error instanceof ParseError) {
//                    Log.d("_locJ_", "location sending response: Parse error");
//                } else if (error instanceof TimeoutError) {
//                    Log.d("_locJ_", "location sending response: timeout error");
//                }
//
//                Log.d("sending_error", "location sending responseError:" + error.toString());
//                error.printStackTrace();
//            }
//        }) {
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("locationinfo", allLocationString);
//                Log.v("_locJ_ location string: ", allLocationString);
//
//
//                return params;
//            }
//        };
//        stringRequest.setShouldCache(false);
//        queue.add(stringRequest);
    }


    private String getSerialNumber() {
        String serialNumber = "";
        if (android.os.Build.VERSION.SDK_INT == 26) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                return serialNumber;
            }
            serialNumber = Build.getSerial();
        } else if (android.os.Build.VERSION.SDK_INT < 26){
            serialNumber = Build.SERIAL;
        }else {
            serialNumber = "111111";
        }

        Log.v("_mdm_", "device  serial *** : " + serialNumber);
        return serialNumber;
    }

    private void sendDeviceInfo(Context context) {

//
//        final String model = Build.MODEL;
//        final String manufacturer = Build.MANUFACTURER;
//        final String serialNumber = getSerialNumber();
//        final String buildVersion = Build.DISPLAY;
//        final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        final String language = Locale.getDefault().getDisplayLanguage();
//        final String timeZone = TimeZone.getDefault().getDisplayName() + " " + TimeZone.getDefault().getID();
//
//
//        String URL = ApiConstants.getDeviceInfoApi(context);
//        RequestQueue queue = Volley.newRequestQueue(context);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                if (response.equalsIgnoreCase("updated") || response.equalsIgnoreCase("success")
//                        || response.equalsIgnoreCase("already exist")) {
//
//                    // save sending status to shared preference
//                    SharedPreferences preferences =
//                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                    preferences.edit().putBoolean(DEVICE_INFO_SENT_KEY_STRING, true).apply();
//
//
//                }
//                Log.d("_mdm_", "sending device info response:" + response.toString());
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//
//                if (error instanceof NetworkError) {
//                    Log.d("_mdm_", "device info response: Network error");
//                } else if (error instanceof ServerError) {
//                    Log.d("_mdm_", "device info response: Server error");
//                } else if (error instanceof AuthFailureError) {
//                    Log.d("_mdm_", "device info response: Auth. error");
//                } else if (error instanceof ParseError) {
//                    Log.d("_mdm_", "device info response: Parse error");
//                } else if (error instanceof TimeoutError) {
//                    Log.d("_mdm_", "device info response: timeout error");
//                }
//
//                Log.d("_mdm_", "device info responseError:" + error.toString());
//                error.printStackTrace();
//            }
//        }) {
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("imei_one", imeiOne);
//                params.put("imei_two", imeiTwo);
//                params.put("model", model);
//                params.put("manufacturer", manufacturer);
//                params.put("serial", serialNumber);
//                params.put("build", buildVersion);
//                params.put("language", language);
//                params.put("timezone", timeZone);
//                params.put("battery_temperature", "0");
//                params.put("battery_health", "0");
//                params.put("battery_tech", "0");
//                params.put("battery_level", "0");
//                params.put("battery_voltage", "0");
//                params.put("device_type", "android");
//                params.put("os_version", "0");
//                params.put("location_services", "0");
//                params.put("app_status", deviceId);
//
//                Log.d("_mdm_ params" , "LocationJobService --> params"+params.toString());
//                return params;
//            }
//        };
//        stringRequest.setShouldCache(false);
//        queue.add(stringRequest);

    }


    private String getAddressByLocation(Location gps, Context mContext) {
        String locationAddress = "unknown";
        if (gps != null) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();


            Log.v("_sf", "lat: " + latitude + " long: " + longitude);


            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(mContext, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 5); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                    String city = addresses.get(0).getLocality();
//                    String state = addresses.get(0).getAdminArea();
//                    String country = addresses.get(0).getCountryName();
//                    String postalCode = addresses.get(0).getPostalCode();
//                    String knownName = addresses.get(0).getFeatureName(); // On
//                    Log.v("_sf", "address : " + address + "\n city: " + city + " state: " + state + " country: " + country + " knownName: " + knownName);

                    locationAddress = address;
                    Log.d("_mdm_", "Location address: "+address);

                } else {
                    Log.d("_mdm_", "Location address not found, Try again");
//                    Toast.makeText(mContext, "Location not found, Try again  ", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("_mdm_", "Location address not found, Try again");
                return locationAddress;
//                Toast.makeText(mContext, "Location not found, Try again  ", Toast.LENGTH_SHORT).show();
            }


        }

        return locationAddress;
    }


}

