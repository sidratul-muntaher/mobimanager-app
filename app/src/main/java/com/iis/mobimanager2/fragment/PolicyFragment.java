package com.iis.mobimanager2.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;

import com.android.volley.error.AuthFailureError;
import com.android.volley.error.NetworkError;
import com.android.volley.error.ParseError;
import com.android.volley.error.ServerError;
import com.android.volley.error.TimeoutError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.iis.mobimanager2.R;
import com.iis.mobimanager2.data.HttpsVolleyCallback;
import com.iis.mobimanager2.data.HttpsVolleyMethods;
import com.iis.mobimanager2.utils.ApiConstants;
import com.iis.mobimanager2.utils.AppPreference;
import com.iis.mobimanager2.utils.AppUtil;
import com.iis.mobimanager2.utils.Constants;
import com.iis.mobimanager2.utils.DeviceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.iis.mobimanager2.utils.Constants.isNetworkAvailable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PolicyFragment extends Fragment {

    NetworkStatsManager networkStatsManager;
    TextView tvDataRestrictionType;
    TextView tvPackageSize;
    TextView tvPackageStartDate;
    TextView tvPackageEndDate;
    TextView tvTodayMobileDataUsed;
    TextView tvTodayUsedWifiValue;
    TextView tvTotalUsedMobileData;
    TextView tvTotalUsedWifiValue;
    LinearLayout btn_send_location;
    private LinearLayout btn_send_data_uses;

    private long todayUsedMobileData_global;
    private long todayUsedWifiData_global;
    private ProgressDialog dialog;
    private boolean isNetworkNull;
    Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_policy, container, false);

        initializeViews(view);
        mContext = getContext();
        dialog = new ProgressDialog(mContext);
        setDataPlanInfo();
        isNetworkNull = false;

//        setTodayUsedDataValue();
//        setTotalUsedDataValue(AppPreference.getStartDate(getContext()));


        long[] todayRange = AppUtil.getTodayRange();
        long[] totalRange = { AppUtil.getTimestampFromDateString(AppPreference.getStartDate(mContext)), System.currentTimeMillis()};

        final Long[] todayUses =  totalUsesData(mContext, todayRange);
        Long[] totalUses =  totalUsesData(mContext, totalRange);

        Log.d("_dMDM_", "today --->> mobile :"+todayUses[0]+" wifi :"+todayUses[1]);
        Log.d("_dMDM_", "total --->> mobile :"+totalUses[0]+" wifi :"+totalUses[1]);

        tvTodayMobileDataUsed.setText(AppUtil.humanReadableByteCount(todayUses[0]));
        tvTodayUsedWifiValue.setText(AppUtil.humanReadableByteCount(todayUses[1]));

        tvTotalUsedMobileData.setText(AppUtil.humanReadableByteCount(totalUses[0]));
        tvTotalUsedWifiValue.setText(AppUtil.humanReadableByteCount(totalUses[1]));


        btn_send_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("_mdm_", "send location button is clicked!");

                dialog.setMessage("Sending location data, please wait.");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                if (isNetworkAvailable(mContext)) {

                    DeviceLocation.LocationResult locationResult = new DeviceLocation.LocationResult() {
                        @Override
                        public void gotLocation(Location location) {
                            if (location != null) {
                                String address = getAddressByLocation(location, mContext);
                                double lat = location.getLatitude();
                                double lon = location.getLongitude();

                                sendLocationToHttpsServer(String.valueOf(lat), String.valueOf(lon), address);

                            } else {
                                // adding for test purpose
                                dialog.dismiss();
                                Log.v("_mdm_", "counter , location null ");
                            }

                        }
                    };

                    DeviceLocation myLocation = new DeviceLocation();
                    myLocation.getLocation(getContext(), locationResult);



                }else {
                    Toast.makeText(mContext, "No internet connection!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });

        btn_send_data_uses.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                btn_send_data_uses.setBackgroundColor(R.color.white);
                dialog.setMessage("Sending Uses data, please wait.");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();
                Log.d("_mdm_", "send_data_uses is clicked");


                if (isNetworkAvailable(getContext())) {
                    sendUsesDataToHTTPSServer(getContext(), todayUses[0].toString(), todayUses[1].toString());
                }else {
                    dialog.dismiss();
                    Toast.makeText(getActivity().getApplicationContext(), "please check your internet connection!!", Toast.LENGTH_SHORT).show();
                }
                btn_send_data_uses.setBackgroundResource(R.drawable.manual_button_background);

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    private void setTotalUsedDataValue(String planStartDate) {
        String totalUsedWifiData = "";
        String totalUsedMobileData = "";
        double wifiMegabyte = 0.000;

        if (Constants.hasNetworkStatPermission(getContext())) {
            networkStatsManager = (NetworkStatsManager) getContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            // WIFI
            NetworkStats.Bucket wifiBucket;


            try {
                wifiBucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                        "",
                        AppUtil.getTimestampFromDateString(planStartDate),
                        System.currentTimeMillis());
                long wifiBytes = wifiBucket.getRxBytes() + wifiBucket.getTxBytes();
                wifiMegabyte =(double) wifiBytes/(1024.0*1024.0);
                totalUsedWifiData = convertLongToDataFormat(wifiMegabyte);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // MOBILE data
            double totalMobileDataMegabyte = (double) getAllRxTXBytesMobileForTotal(getContext(),planStartDate)/(1024.0*1024.0);
            totalUsedMobileData = convertLongToDataFormat(totalMobileDataMegabyte);

            if (totalMobileDataMegabyte > 0.000 && wifiMegabyte > 0.000){
                tvTotalUsedMobileData.setText(String.format(Locale.US, "%.3f", totalMobileDataMegabyte)+"MB");
                tvTotalUsedWifiValue.setText(String.format(Locale.US, "%.3f", wifiMegabyte)+"MB");
            }else {
                tvTotalUsedMobileData.setText("0.0MB");
                tvTotalUsedWifiValue.setText("0.0MB");
            }


        } else {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

    }

    private void setTodayUsedDataValue() {

        String todayUsedMobileData = "";
        String todayUsedWiFIData = "";
        double wifiMegabyte = 0.000;

        if (Constants.hasNetworkStatPermission(getContext())) {
            networkStatsManager = (NetworkStatsManager) getContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            // WIFI
            NetworkStats.Bucket wifiBucket;
            try {
                wifiBucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                       "",
                        AppUtil.getStartOfDayInMillis(),
                        AppUtil.getEndOfDayInMillis());
                long wifiBytes = wifiBucket.getRxBytes() + wifiBucket.getTxBytes();
                todayUsedWifiData_global = wifiBytes+1;
                wifiMegabyte = wifiBytes/(1024.0*1024.0);
                todayUsedWiFIData = convertLongToDataFormat(wifiMegabyte);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // MOBILE data
            long todayMobileUsed = getAllRxTXBytesMobileForToday(getContext());
            todayUsedMobileData_global =todayMobileUsed+1;
            double todayMobileDataMegabyte = todayMobileUsed/(1024.0*1024.0);
            todayUsedMobileData = convertLongToDataFormat(todayMobileDataMegabyte);

        } else {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private String convertLongToDataFormat(double bytes) {
        return bytes + "";
    }

    private void initializeViews(View view) {
        tvDataRestrictionType = view.findViewById(R.id.tvDataRestrictionType);
        tvPackageSize = view.findViewById(R.id.tvPackageSize);
        tvPackageStartDate = view.findViewById(R.id.tvPackageStartDate);
        tvPackageEndDate = view.findViewById(R.id.tvPackageEndDate);
        tvTodayMobileDataUsed = view.findViewById(R.id.tvTodayDataUsed);
        tvTotalUsedMobileData = view.findViewById(R.id.tvTotalMobileDataValue);
        tvTotalUsedWifiValue = view.findViewById(R.id.tvTotalUsedWifiValue);
        tvTodayUsedWifiValue = view.findViewById(R.id.tvTodayUsedWifiValue);
        btn_send_location = view.findViewById(R.id.btn_send_location);
        btn_send_data_uses = view.findViewById(R.id.btn_send_data_uses);
    }


    private void setDataPlanInfo() {

        String network_restriction = AppPreference.getNetworkRestrictionType(getContext());
        String data_usage_type = AppPreference.getDataUsageType(getContext());
        String data_usage_total_limit = AppPreference.getDataUsageTotalLimit(getContext());
        String start_date = AppPreference.getStartDate(getContext());
        String end_date = AppPreference.getEndDate(getContext());
        String packageSize = data_usage_total_limit.toUpperCase() + " " + data_usage_type.toUpperCase();
        tvDataRestrictionType.setText(network_restriction.toUpperCase());
        tvPackageSize.setText(packageSize);
        tvPackageStartDate.setText(start_date.toUpperCase());
        tvPackageEndDate.setText(end_date.toUpperCase());
        tvTodayMobileDataUsed.setText("");
        tvTotalUsedMobileData.setText("");
    }

    public long getAllRxTXBytesMobileForToday(Context context) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    AppUtil.getStartOfDayInMillis(),
                    AppUtil.getEndOfDayInMillis());


        } catch (RemoteException e) {
            return 0;
        }
        return bucket.getRxBytes() + bucket.getTxBytes();
    }

    public long getAllRxTXBytesMobileForTotal(Context context, String planStartDate) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    AppUtil.getTimestampFromDateString(planStartDate),
                    AppUtil.getEndOfDayInMillis());
        } catch (RemoteException e) {
            return 0;
        }
        return bucket.getRxBytes() + bucket.getTxBytes();
    }

    //Here Manifest.permission.READ_PHONE_STATS is needed
    private String getSubscriberId(Context context, int networkType) {
        String subscriberId = "";
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return subscriberId;
            } else {
                if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
                    subscriberId = tm.getSubscriberId();
                } else {
                    return subscriberId;
                }
            }

        }
        return subscriberId;
    }

    private void sendLocationDataToServer(Context mContext, String API_URL,  final String allLocationString) {

        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, API_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                if (response.equalsIgnoreCase("updated") || response.equalsIgnoreCase("success")) {
                    if (response.equalsIgnoreCase("success")) {
                        Toast.makeText(getActivity().getBaseContext(), "send successfully", Toast.LENGTH_SHORT).show();
                        // delete all data from location table

                        // send device info if not sent ever
//                        SharedPreferences preferences =
//                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                        boolean isDeviceInfoSent = preferences.getBoolean(DEVICE_INFO_SENT_KEY_STRING, false);
//                        if (!isDeviceInfoSent) {
//                            sendDeviceInfo(getApplicationContext());
//                        }

                    }

                }

                Log.d("_mdm_", "sending location data response:" + response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                if (error instanceof NetworkError) {
                    Log.d("_mdm_", "location sending response: Network error");

                } else if (error instanceof ServerError) {
                    Log.d("_mdm_", "location sending response: Server error");
                } else if (error instanceof AuthFailureError) {
                    Log.d("_mdm_", "location sending response: Auth. error");
                } else if (error instanceof ParseError) {
                    Log.d("_mdm_", "location sending response: Parse error");
                } else if (error instanceof TimeoutError) {
                    Log.d("_mdm_", "location sending response: timeout error");
                }

                Log.d("sending_error", "location sending responseError:" + error.toString());
                error.printStackTrace();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("locationinfo", allLocationString);
                Log.v("_mdm_ Data string: ", allLocationString);

                Log.d("_mdm_ " , "params : "+params.toString());
                return params;
            }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }

    public class AsyncTaskRunner extends AsyncTask<String, String, String> {

        ProgressDialog myDialog;
        Context mContext_asy;
        String lat, lon, address;
        public AsyncTaskRunner(Context context, String lat, String lon, String address){
            mContext_asy = context;
            myDialog = new ProgressDialog(context);
            this.lat = lat;
            this.lat = lon;
            this.address = address;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.v("_locJ_", "do in background, Network: " + isNetworkAvailable(mContext_asy));

            sendLocationToHttpsServer(lat, lat, address);

            return "nothing";
        }

        @Override
        protected void onPreExecute() {
            Log.v("_locJ_", "onPre execute called");
            myDialog.setMessage("Sending location data, please wait.");
            myDialog.setCancelable(false);
            myDialog.setIndeterminate(true);
            myDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {

            dialog.dismiss();
            Log.v("_locJ_", "onPre execute called");

        }




    }

    private String makeJsonBody(String latitude, String longitude, String address){
        JSONArray jsonArray = new JSONArray();
        String imeiOne = AppPreference.getImeiOne(getContext());
        String imeiTwo = AppPreference.getImeiTwo(getContext());

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("imeiOne", imeiOne);
            jsonObject.put("imeiTwo", imeiTwo);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("address", address);
            jsonObject.put("userDataTime", Constants.getCurrentDataTimeString());
            jsonObject.put("startDate", Constants.getCurrentDataTimeString());
        } catch (JSONException e) {
            dialog.dismiss();
            e.printStackTrace();
        }
        jsonArray.put(jsonObject);

        return  jsonArray.toString();
    }

    private void sendLocationToHttpsServer(String latitude, String longitude, String address){
        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
            String jsonArrayString = makeJsonBody(latitude, longitude, address);

            if (!TextUtils.isEmpty(jsonArrayString) || jsonArrayString.length() > 0){
                Log.v("_https_", "SendLocationData -->> json-array:"+jsonArrayString);
                String LOCATION_API = ApiConstants.getSaveLocationApi();
                HttpsVolleyMethods httpsVolleyMethods = new HttpsVolleyMethods();
                httpsVolleyMethods.httpsSendLocationPostRequest(mContext, LOCATION_API, jsonArrayString, new HttpsVolleyCallback() {
                    @Override
                    public void success(String response) {
                        dialog.dismiss();
                        Log.d("_https_", "location response :"+response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            long statusCode = jsonObject.getLong("statusCode");
                            if (statusCode == 200 || statusCode == 201){
                                Toast.makeText(mContext, "send successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
//                        sendLocationDataToServer(getApplicationContext(), jsonArray.toString());
            }else {
                dialog.dismiss();
            }


        } else {
            dialog.dismiss();
            Log.v("_locJ_", "getSendLocationData is null");
        }

        dialog.dismiss();
    }

    private void sendUsesDataToHTTPSServer(Context mContext, String mobileData, String wifiData){

        String imeiOne = AppPreference.getImeiOne(getContext());
        String imeiTwo = AppPreference.getImeiTwo(getContext());

        JSONArray jsonArray = new JSONArray();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imei", imeiOne);
            jsonObject.put("imei2", imeiTwo);
            jsonObject.put("mobileData", mobileData);
            jsonObject.put("wifiData", wifiData);
            jsonObject.put("userDate", Constants.getTodayDateString());

            jsonArray.put(jsonObject);
        } catch (JSONException e) {
            dialog.dismiss();
            e.printStackTrace();
        }


        if (jsonArray.length() > 0){
            HttpsVolleyMethods volleyMethods = new HttpsVolleyMethods();
            String dataUsesApi = ApiConstants.getDataUsesApi();
            Log.d("_https_", "https data uses response: "+jsonArray.toString());
            volleyMethods.sendPostRequestToServer(mContext, dataUsesApi, jsonArray.toString(),
                    new HttpsVolleyCallback() {
                        @Override
                        public void success(String response) {
                            dialog.dismiss();
                            Log.d("_https_", "https data uses response: "+response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                long statusCode = jsonObject.getLong("statusCode");
                                if (statusCode == 200 || statusCode == 201){
                                    Toast.makeText(mContext, "Send Successfully!", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.d("_https_", "https data uses send response: "+response);
                        }
                    });
        }

        dialog.dismiss();

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


    private Long[] totalUsesData(Context mContext, long[] range){
        long totalWifi = 0;
        long totalMobile = 0;
        if (Constants.hasNetworkStatPermission(getContext())) {
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
            return new Long[]{totalMobile, totalWifi};

        } else {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        return new Long[]{totalMobile, totalWifi};
    }
}
