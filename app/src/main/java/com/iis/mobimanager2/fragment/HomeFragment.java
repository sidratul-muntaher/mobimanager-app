package com.iis.mobimanager2.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iis.mobimanager2.R;
import com.iis.mobimanager2.data.AppDatabaseHelper;
import com.iis.mobimanager2.data.HttpsVolleyCallback;
import com.iis.mobimanager2.data.HttpsVolleyMethods;
import com.iis.mobimanager2.receiver.AlarmManagerReceiver;
import com.iis.mobimanager2.service.AppService;
import com.iis.mobimanager2.utils.ApiConstants;
import com.iis.mobimanager2.utils.Constants;
import com.iis.mobimanager2.utils.DeviceAdminHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.iis.mobimanager2.utils.AppPreference;
import com.iis.mobimanager2.utils.SessionManager;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;
import static com.iis.mobimanager2.utils.Constants.getTodayDateString;

public class HomeFragment extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST = 1448;

    String imeiOne = "";
    String imeiTwo = "";
    TextView tvImeiOne;
    TextView tvImeiTwo;
    TextView tvManufacturer;
    TextView tvBuild;
    TextView tvDeviceId;

    String model;
    String manufacturer;
    String serialNumber;
    String buildVersion;
    String deviceId;
    TextView tvName;
    TextView tvMobile;
    TextView tvAddress;
    SharedPreferences preferences;
    ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        tvImeiOne = view.findViewById(R.id.tvImeiOne);
        tvImeiTwo = view.findViewById(R.id.tvImeiTwo);
        tvManufacturer = view.findViewById(R.id.tvManufacturer);
        tvBuild = view.findViewById(R.id.tvBuild);
        tvDeviceId = view.findViewById(R.id.tvDeviceId);
        tvName = view.findViewById(R.id.tvName);
        tvMobile = view.findViewById(R.id.tvMobile);
        tvAddress = view.findViewById(R.id.tvAddress);
        imageView = view.findViewById(R.id.imageContact);

        Context mContext = getContext();
        preferences = mContext.getSharedPreferences(Constants.APP_PREFERENCE, Context.MODE_PRIVATE);

        // check all permision
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
        ) {
            // permission not granted
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST);

        } else {
            // All permission granted already-------commentOut by ashikur
//            Intent serviceIntent = new Intent(getContext(), AppService.class);
//            ContextCompat.startForegroundService(getContext(), serviceIntent);

            Intent serviceIntent = new Intent(getContext(), AppService.class);
            Log.d("_mdm_service_", "onCreate: APP Service");
            getContext().startService(serviceIntent);

            setIMEIValues();
            getUserInfo(imeiOne, mContext); //formatter.setTimeZone(TimeZone.getTimeZone("America/Belize"));

//            sendUsesDataToHTTPSServer(mContext);

//            String fileUrl = "https://103.243.142.26:8443/Walton-MDM/api/downloadFile1/securly_ca_2034 .crt";
//            String fileType = "crt";
//            String fileName_ = "securly_ca_2034";
//
//            new FileDownloaderAsyncTaskForHTTPS(mContext, imageView).execute(new AsyncTaskParams(fileName_, fileType, fileUrl));

            String deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            imeiOne = AppPreference.getImeiOne(getContext());
            imeiTwo = AppPreference.getImeiTwo(getContext());

            final SessionManager sessionManager = new SessionManager(getContext());

            if (!sessionManager.isDeviceIdSend_HTTPS() && ((!TextUtils.isEmpty(imeiOne)) || (!TextUtils.isEmpty(imeiTwo))) && !TextUtils.isEmpty(deviceId)) {
//                imeiOne = "355634081690718";//for test purpose

                if (TextUtils.isEmpty(imeiTwo)) {
                    imeiTwo = imeiOne;
                }

                if (TextUtils.isEmpty(imeiOne)) {
                    imeiOne = imeiTwo;
                }
            }
            // ask for admin permission if OS 10
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                DevicePolicyManager manager =
//                        (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
//
//                assert manager != null;
//                if (!manager.isProfileOwnerApp(getContext().getPackageName())) {
//                    provisionManagedProfile();
//                }else {
//
//                    ComponentName componentName = DeviceAdminHelper.getComponentName(getContext());
//                    manager.setProfileName(componentName, getString(R.string.profile_name));
//                    manager.setProfileEnabled(componentName);
//                }
//            }


        }


        return view;
    }


    private void provisionManagedProfile() {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        Intent intent = new Intent(ACTION_PROVISION_MANAGED_PROFILE);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    DeviceAdminHelper.getComponentName(activity));
        } else {
            //noinspection deprecation
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                    activity.getApplicationContext().getPackageName());
            intent.putExtra(EXTRA_DEVICE_ADMIN, DeviceAdminHelper.getComponentName(activity));
        }
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivity(intent);
            activity.finish();
        } else {
            Toast.makeText(activity, "Device provisioning is not enabled. Stopping.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInfo(String imeiOne, final Context mContext) {
        final String imei = imeiOne;
        if (!TextUtils.isEmpty(imeiOne) && imeiOne.length() > 5) {
            SessionManager sessionManager = new SessionManager(getContext());
            Log.d("_mdm_man", "session manager token send? "+sessionManager.isTokenSend());
            if (!sessionManager.isTokenSend()){
                sendFCMTokenToHTTPSServer(mContext, imeiOne);
            }


            String url = ApiConstants.getEmployeeInfoApi()+imei;
            Log.d("_https_", "https user info api : "+ url);
            HttpsVolleyMethods httpsVolleyMethods = new HttpsVolleyMethods();
            httpsVolleyMethods.getRequestWithPathParam(mContext, url, new HttpsVolleyCallback() {
                @Override
                public void success(String response) {
                    Log.d("_https_", "https user info response: "+ response);

                    if (response != null && response.length() > 0) {
//                        Log.d("_https_", "getting  user info  response not null :" + response);
                        try {
                            JSONObject userJsonObject = new JSONObject(response);
                            String data = userJsonObject.getString("data");
                            if (!TextUtils.isEmpty(data) && !data.equals("null")){
                                JSONObject jsonDAtaObj = new JSONObject(data);
                                String userName = jsonDAtaObj.getString("driverName");
                                String mobile = jsonDAtaObj.getString("mobile");
                                String address = jsonDAtaObj.getString("driverAddress");
                                setUserInfo(userName, mobile, address);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }
            });

//            Log.d("_mdm_", "get user info imeiOne ok");
//            RequestQueue queue = Volley.newRequestQueue(mContext);
//            String USER_API = ApiConstants.getUserInfoApi(mContext);
//            StringRequest stringRequest = new StringRequest(Request.Method.GET, USER_API, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.d("_mdm_", "getting  user info  response:" + response);
//                    if (response != null && response.length() > 0) {
//                        Log.d("_mdm_", "getting  user info  response not null :" + response);
//                        try {
//                            JSONObject userJsonObject = new JSONObject(response);
//                            String userName = userJsonObject.getString("name");
//                            String mobile = userJsonObject.getString("mobile");
//                            String address = userJsonObject.getString("address");
//                            setUserInfo(userName, mobile, address);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
////                        boolean isFvmSent = preferences.getBoolean(Constants.TOKEN_SHOULD_SENT, false);
////
////                        Log.d("_mdm_", "isFvmSent = "+isFvmSent);
////                        if (isFvmSent) {
////                            sendFCMTokenToServer(mContext);
////                        }
//                        //sendFCMTokenToServer(mContext);
//
//                    }
//
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                    if (error instanceof NetworkError) {
//                        Log.d("_mdm_", "getting  user info  response: Network error");
//                    } else if (error instanceof ServerError) {
//                        Log.d("_mdm_", "getting  user info  response: Server error");
//                    } else if (error instanceof AuthFailureError) {
//                        Log.d("_mdm_", "getting  user info  response: Auth. error");
//                    } else if (error instanceof ParseError) {
//                        Log.d("_mdm_", "getting  user info  response: Parse error");
//                    } else if (error instanceof TimeoutError) {
//                        Log.d("_mdm_", "getting  user info  response: timeout error");
//                    }
//                    Log.d("sending_error", "getting  user info  response:" + error.toString());
//                    error.printStackTrace();
//                }
//            }) {
//
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> params = new HashMap<String, String>();
//                    params.put("imei", imei);
//                    return params;
//                }
//            };
//            stringRequest.setShouldCache(false);
//            queue.add(stringRequest);
        }
    }

    private void setUserInfo(String userName, String mobile, String address) {
        tvName.setText(userName);
        tvMobile.setText(mobile);
        tvAddress.setText(address);
    }


    public void setIMEIValues() {
        imeiOne = AppPreference.getImeiOne(getContext());
        imeiTwo = AppPreference.getImeiTwo(getContext());

        tvImeiOne.setText(imeiOne);
        tvImeiTwo.setText(imeiTwo);
        setInfoValues();

        /*TelephonyManager tm = (TelephonyManager) getContext().getSystemService(TELEPHONY_SERVICE);

        Log.d("allUser", "setIMEIValues: "+android.os.Build.VERSION.SDK_INT+" "+Build.VERSION_CODES.Q);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imeiOne = AppPreference.getImeiOne(getContext());
                imeiTwo = AppPreference.getImeiTwo(getContext());
            } else {

                imeiOne = tm.getDeviceId(1);
                imeiTwo = tm.getDeviceId(2);
            }

            if (imeiOne == null) {
                imeiOne = "555555555555555";
                imeiTwo = "666666666666666";
            }
            tvImeiOne.setText(imeiOne);
            tvImeiTwo.setText(imeiTwo);
            setInfoValues();
        }*/
    }

    public void setInfoValues() {
        model = Build.MODEL;
        manufacturer = Build.MANUFACTURER;
        serialNumber = getSerialNumber();
        buildVersion = Build.DISPLAY;
        deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        tvManufacturer.setText(manufacturer + " " + model);
        tvBuild.setText(buildVersion);
        tvDeviceId.setText(deviceId);
    }

    private String getSerialNumber() {
        String serialNumber = "";
        if (android.os.Build.VERSION.SDK_INT == 26) {

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                return serialNumber;
            }
            serialNumber = Build.getSerial();
        } else if (android.os.Build.VERSION.SDK_INT < 26) {
            serialNumber = Build.SERIAL;
        } else {
            serialNumber = "111111";
        }

        Log.v("_mdm_", "device  serial *** : " + serialNumber);
        return serialNumber;
    }


    private String makeJsonObject(String token){
        JSONObject jsonObject = new JSONObject();
        if (!TextUtils.isEmpty(imeiOne)){
            if (TextUtils.isEmpty(imeiTwo)){
                imeiTwo = imeiOne;
            }

            try {
                jsonObject.put("imei", imeiOne);
                jsonObject.put("imei2", imeiTwo);
                jsonObject.put("token", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return jsonObject.toString();
    }

    private void sendFCMTokenToHTTPSServer(final Context mContext, String imeiOne){
        Log.d("_mdm_", "******* sendFCMTokenToHTTPSServer called ******");

        final String token = preferences.getString(Constants.FIREBASE_TOKEN, "");
        if ((!TextUtils.isEmpty(imeiOne)) && (!TextUtils.isEmpty(token))){
            String requestBody = makeJsonObject(token);

            Log.i("...", "sendFCMTokenToHTTPSServer: " + requestBody);
            if (!TextUtils.isEmpty(requestBody) && Constants.isNetworkAvailable(mContext)){
                String TOKEN_API = ApiConstants.getTokenApi();
                HttpsVolleyMethods httpsVolleyMethods = new HttpsVolleyMethods();
                httpsVolleyMethods.sendPostRequestToServer(mContext, TOKEN_API, requestBody, new HttpsVolleyCallback() {
                    @Override
                    public void success(String response) {
                        Log.d("_https_", "token response: "+response);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(Constants.TOKEN_SHOULD_SENT, false);
                        editor.commit();


                        SessionManager sessionManager = new SessionManager(getContext());
                        sessionManager.isTokenSend(true);

                    }
                });
            }
        }
    }



    private void sendFCMTokenToServer(Context applicationContext) {

//        Log.d("_mdm_man", "sendFCMTokenToServer called");
//        final SharedPreferences preferences = applicationContext.getSharedPreferences(Constants.APP_PREFERENCE, Context.MODE_PRIVATE);
//        final String serial = getSerialNumber();
//        final String token = preferences.getString(Constants.FIREBASE_TOKEN, "");
//        Log.d("_mdm_", "token = "+token);
//
//        if (imeiOne == null) {
//
//            imeiOne = "555555555555555";
//            imeiTwo = "666666666666666";
//            Log.d("_mdm_man", "sendFCMTokenToServer called"+imeiOne);
//
//        }
//        if (imeiOne.length() > 2 && serial.length() > 0 && token.length() > 0) {
//
//            Log.d("_mdm_man", "sendFCMTokenToServer called condition ok");
//            RequestQueue queue = Volley.newRequestQueue(applicationContext);
//            String TOKEN_API = ApiConstants.getFcmTokenApi(getContext());
//            Log.v("_mdm_man", TOKEN_API);
//            Log.d("_ndm_man", "FCM  api address : " + TOKEN_API);
//            StringRequest stringRequest = new StringRequest(Request.Method.GET, TOKEN_API, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.d("_mdm_man", "sending token api response ** :" + response);
//                    if (response.equalsIgnoreCase("updated") || response.equalsIgnoreCase("success")
//                            || response.equalsIgnoreCase("already exist")) {
//                        Log.d("_ndm_man", "FCM  api: " + response);
//
//
//                        SharedPreferences.Editor editor = preferences.edit();
//                        editor.putBoolean(Constants.TOKEN_SHOULD_SENT, false);
//                        editor.commit();
//
//
//                        SessionManager sessionManager = new SessionManager(getContext());
//                        sessionManager.isTokenSend(true);
//
//                    }
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                    if (error instanceof NetworkError) {
//                        Log.d("_mdm_", "sending token response: Network error");
//                    } else if (error instanceof ServerError) {
//                        Log.d("_mdm_", "sending token response: Server error");
//                    } else if (error instanceof AuthFailureError) {
//                        Log.d("_mdm_", "sending token response: Auth. error");
//                    } else if (error instanceof ParseError) {
//                        Log.d("_mdm_", "sending token response: Parse error");
//                    } else if (error instanceof TimeoutError) {
//                        Log.d("_mdm_", "sending token response: timeout error");
//                    }
//                    Log.d("sending_error", "sending token responseError:" + error.toString());
//                    Log.d("_mdm_man", "error ** :" + error);
//
//                    error.printStackTrace();
//                }
//            }) {
//
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> params = new HashMap<String, String>();
//                    params.put("imei1", imeiOne);
//                    params.put("imei2", imeiTwo);
//                    params.put("serial", serial);
//                    params.put("token", token);
//
//                    return params;
//                }
//            };
//            stringRequest.setShouldCache(false);
//            queue.add(stringRequest);
//        }

    }

    private void setAlarmForDailyDataUsageReport() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long alarmTimeInMillis = calendar.getTimeInMillis();
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getContext(), AlarmManagerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            //  permission granted--------commentOut by ashikur
//            Intent serviceIntent = new Intent(getContext(), AppService.class);
//            ContextCompat.startForegroundService(getContext(), serviceIntent);

            Intent serviceIntent = new Intent(getContext(), AppService.class);
            getContext().startService(serviceIntent);


            setIMEIValues();
            getUserInfo(imeiOne, getContext());
        }
    }

    private void sendUsesDataToHTTPSServer(Context mContext){
        AppDatabaseHelper databaseHelper = new AppDatabaseHelper(mContext);
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
                volleyMethods.sendPostRequestToServer(mContext, dataUsesApi, jsonArray.toString(),
                        new HttpsVolleyCallback() {
                            @Override
                            public void success(String response) {
                                Log.d("_https_", "https data uses response: "+response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    long statusCode = jsonObject.getLong("statusCode");
                                    if (statusCode == 200){
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
}
