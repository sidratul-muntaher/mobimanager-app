package com.iis.mobimanagercedp.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iis.mobimanagercedp.R;
import com.iis.mobimanagercedp.activity.MainActivity;
import com.iis.mobimanagercedp.activity.NotificationActivity;
import com.iis.mobimanagercedp.data.AppDatabaseHelper;
import com.iis.mobimanagercedp.data.HttpsVolleyCallback;
import com.iis.mobimanagercedp.data.HttpsVolleyMethods;
import com.iis.mobimanagercedp.model.AsyncTaskParams;
import com.iis.mobimanagercedp.model.Message;
import com.iis.mobimanagercedp.model.NotificationFile;
import com.iis.mobimanagercedp.utils.ApiConstants;
import com.iis.mobimanagercedp.utils.AppPreference;
import com.iis.mobimanagercedp.utils.AppUtil;
import com.iis.mobimanagercedp.utils.Constants;
import com.iis.mobimanagercedp.utils.DeviceLocation;
import com.iis.mobimanagercedp.utils.FileDownloaderAsyncTaskForHTTPS;
import com.iis.mobimanagercedp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import static com.iis.mobimanagercedp.application.DeviceManager.CHANNEL_ID_FOR_MESSAGE;
import static com.iis.mobimanagercedp.utils.ApiConstants.BASE_URL_PREF_KEY;
import static com.iis.mobimanagercedp.utils.Constants.APP_PREFERENCE;
import static com.iis.mobimanagercedp.utils.Constants.LOCATION_JOB_INFO_ID;
import static com.iis.mobimanagercedp.utils.Constants.getFileNameByTIMEinMILI;

import org.json.JSONException;
import org.json.JSONObject;

public class AppMessagingService extends FirebaseMessagingService {
    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID = "admin_channel";
    private NotificationManager notificationManager;
    String imeiOne = "";
    String imeiTwo = "";
    private static final int DEFAULT_BUFFER_SIZE = 1024*100;

    Bitmap bitmap;
    SessionManager sessionManager;

    protected String fileId;

    private DownloadManager mgr=null;
    private long lastDownload=-1L;

    private String lastCrtFileName;

    @Override
    public void onNewToken(String s) {
        // Get updated InstanceID token.
        Log.v("_mdm_", "AppMessagingService onNewToken called and newToken is :"+s);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("_mdm_firebase", "New token: " + refreshedToken);
        Context mContext = getApplicationContext();

        SharedPreferences preferences = mContext.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.FIREBASE_TOKEN, refreshedToken);
        editor.commit();

        editor.putBoolean(Constants.TOKEN_SHOULD_SENT, true);
        editor.commit();

        sessionManager = new SessionManager(getApplicationContext());
        sessionManager.isTokenSend(false);

        setIMEIValues();
        sendFCMTokenToHTTPSServer(mContext, refreshedToken);
//        sendFCMTokenToServer(mContext,refreshedToken);

    }
    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.v("_mdm_firebase", "AppMessagingService onMessageReceived called");
        Log.v("_mdm_firebase", "remoteMessage : "+remoteMessage);
        sessionManager = new SessionManager(getApplicationContext());


        String title = remoteMessage.getData().get("title");
        if (title.equalsIgnoreCase("location")) {
            ComponentName componentName = new ComponentName(getApplicationContext(), LocationJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(90, componentName)
                    .setRequiresCharging(false)
                    .setMinimumLatency(15 * 60 * 1000)
                    .build();
            JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(getApplicationContext().JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobInfo);
        } else if (title.equalsIgnoreCase("data_usage")) {
            Log.v("_mdm_firebase", " data_usage matched");
            String network_restriction = remoteMessage.getData().get("network_restriction");
            String data_usage_type = remoteMessage.getData().get("data_usage_type");
            String data_usage_total_limit = remoteMessage.getData().get("data_usage_total_limit");
            String start_date = remoteMessage.getData().get("start_date");
            String end_date = remoteMessage.getData().get("end_date");
            String daily_limit = remoteMessage.getData().get("daily_limit");
            String monthly_limit = remoteMessage.getData().get("monthly_limit");
            AppPreference.setNetworkRestrictionType(getApplicationContext(), network_restriction);
            AppPreference.setDataUsageType(getApplicationContext(), data_usage_type);
            AppPreference.setDataUsageTotalLimit(getApplicationContext(), data_usage_total_limit);
            AppPreference.setStartDate(getApplicationContext(), start_date);
            AppPreference.setEndDate(getApplicationContext(), end_date);
            AppPreference.setDailyLimit(getApplicationContext(), daily_limit);
            AppPreference.setMonthlyLimit(getApplicationContext(), monthly_limit);
        } else if (title.equalsIgnoreCase("message")) {
            // save data to local database
            Log.v("_mdm_firebase", " message matched");

            String messageTitle = remoteMessage.getData().get("message_title");
            String messageDetails = remoteMessage.getData().get("message");
            String imageUrl = remoteMessage.getData().get("image_url");
            String date = remoteMessage.getData().get("date");
            String priority = remoteMessage.getData().get("priority");
            String messageId = String.valueOf(getCurrentTimeStamp()/1000);
            AppDatabaseHelper appDatabaseHelper = new AppDatabaseHelper(getApplicationContext());
            appDatabaseHelper.insertMessageData(messageId,messageTitle,messageDetails,imageUrl,date,priority);

            // show a notification
            if (imageUrl.length() > 0){
                bitmap = getBitmapFromURL(imageUrl);
                generateImageNotification(messageTitle, messageDetails, date, imageUrl, bitmap);
            }else {
                // show a notification
                showNotification(messageTitle, messageDetails, date, imageUrl);
            }
        } else if (title.equalsIgnoreCase("file")) {
            Log.v("_mdm_firebase", " file matched");

            // save data to local database
            String fileName_ = remoteMessage.getData().get("file_name");

            String fileName = fileName_.split("\\.", -1)[0];
            fileName_ = fileName;

            String fileType = remoteMessage.getData().get("type");
            String fileUrl = remoteMessage.getData().get("file_url");
            String date = remoteMessage.getData().get("date");
            String fileSize = remoteMessage.getData().get("size");
            String description = remoteMessage.getData().get("description");
            Log.v("_mdm_firebase files", fileName +"."+fileType +" "+ description);

            if( description.equalsIgnoreCase("delete")){
                Log.v("_mdm_firebase delete", fileName +"."+fileType +" "+ description);
                AppDatabaseHelper appDatabaseHelper = new AppDatabaseHelper(getApplicationContext());
                appDatabaseHelper.deleteNotificationFiles(fileName, fileType);
                File mdm_directory = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager");
                if(mdm_directory.exists()){
                    String finalFileName =  fileName + "." + fileType;
                    File appSpecificExternalDir = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager", finalFileName);
                    if(appSpecificExternalDir.exists()){
                        appSpecificExternalDir.delete();
                        Log.v("_mdm_firebase delete done", fileName +"."+fileType +" "+ description);

                    }
                }

            }
            else{
                long timeInMiliSec = getCurrentTimeStamp();
                String fileId = String.valueOf(timeInMiliSec);
                AppDatabaseHelper appDatabaseHelper = new AppDatabaseHelper(getApplicationContext());
                appDatabaseHelper.insertFileData(fileId,fileName_,fileType,fileUrl,date,fileSize,description,"false");

                if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")) {
                    Bitmap typeImg = BitmapFactory.decodeResource(getResources(), R.drawable.jpg_color);
                    showFileNotification(fileId, getFileNameByTIMEinMILI(timeInMiliSec, fileType), description, fileUrl, typeImg);
                    new FileDownloaderAsyncTaskForHTTPS(getApplicationContext()).execute(new AsyncTaskParams(fileName_, fileType, fileUrl));
                } else if (fileType.equalsIgnoreCase("pdf")) {
                    Bitmap typeImg = BitmapFactory.decodeResource(getResources(), R.drawable.pdf_color);
                    showFileNotification(fileId, getFileNameByTIMEinMILI(timeInMiliSec, fileType), description, fileUrl, typeImg);
                } else if (fileType.equalsIgnoreCase("mp3")) {
                    Bitmap typeImg = BitmapFactory.decodeResource(getResources(), R.drawable.mp3_color);
                    showFileNotification(fileId, getFileNameByTIMEinMILI(timeInMiliSec, fileType), description, fileUrl, typeImg);
                } else if (fileType.equalsIgnoreCase("mp4")) {
                    Bitmap typeImg = BitmapFactory.decodeResource(getResources(), R.drawable.mp4_color);
                    showFileNotification(fileId, getFileNameByTIMEinMILI(timeInMiliSec, fileType), description, fileUrl, typeImg);
                }else if (fileType.equalsIgnoreCase("apk")) {
                    Log.d("_mdm_file_", "app received a apk file");
                    String downloadedFile = getFileNameByTIMEinMILI(timeInMiliSec, fileType);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
                    request.setDescription(fileName_);
                    request.setTitle("MDM FILE DOWNDLOAD");
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(getApplicationContext(), AppUtil.getDirName(fileType), downloadedFile);
                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);



                    String sdcardRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
                    final String apkSavePath = sdcardRoot+"/"+downloadedFile;
                    Log.d("_mdm_ins", "apkSavePath : "+apkSavePath);

                    if (apkSavePath.length() > 0){
                        Log.d("_mdm_ins", "got the apk file path");
                        installApk(apkSavePath);
                    }













//                File file = new File(downloadedFile);
//                File appSpecificExternalDir = new File(file.getAbsolutePath());
//                Uri myfileURI = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", appSpecificExternalDir);
//
//                String filePath = appSpecificExternalDir.getAbsolutePath();
//                Log.d("_mdm_file_", "file exist and file path :"+filePath);
////                Log.d("_mdm_file_", "file exist and file path myfileURI :"+myfileURI.toString());
//                if (filePath != null){
//                    Log.d("_mdm_ins", "got the apk file path");
//                    installApk(filePath);
//                }else {
//                    Log.d("_mdm_ins", "file path is null");
//                }


//                if (appSpecificExternalDir.isFile()){
//
//                }
                }else if (fileType.equalsIgnoreCase("crt")) {
                    // save data to local database
                    Log.d("_mdm_file_", "received a crt file");
                    lastCrtFileName = fileName_;
//                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//                String downloadedFile =fileName_;



                    Log.d("https_download", ""+fileName_);

                    new FileDownloaderAsyncTaskForHTTPS(getApplicationContext()).execute(new AsyncTaskParams(fileName_, fileType, fileUrl));

//                download(downloadedFile, "crt", fileUrl, getApplicationContext());

                }
            }


        }else if (title.equalsIgnoreCase("location_interval")){
            String interval = remoteMessage.getData().get("interval");
            int intervalTime = 15;

            try {
                intervalTime = Integer.parseInt(interval);
            } catch (Exception e) {
                e.printStackTrace();
            }


            ComponentName componentName = new ComponentName(getApplicationContext(),LocationJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(LOCATION_JOB_INFO_ID,componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setPeriodic(intervalTime*60*1000)
                    .build();
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(LOCATION_JOB_INFO_ID);
            scheduler.schedule(jobInfo);
        } else if (title.equalsIgnoreCase("base_url")) {
            String baseUrl = remoteMessage.getData().get("url");
            Log.v("_mdm_","base url:"+ baseUrl);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(BASE_URL_PREF_KEY, baseUrl);
            editor.apply();
        }

        // ************title is "ping" so just send location, do not open application
//        if (title.equalsIgnoreCase("ping")) {
//            Log.v("_mdm_", "ping notification");
//
//
//            ComponentName componentName = new ComponentName(getApplicationContext(), LocationJobService.class);
//            JobInfo jobInfo = new JobInfo.Builder(90, componentName)
//                    .setRequiresCharging(false)
//                    .setMinimumLatency(1000)
//                    .build();
//            JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(getApplicationContext().JOB_SCHEDULER_SERVICE);
//            jobScheduler.schedule(jobInfo);
//
//
//
////            if (ContextCompat.checkSelfPermission(getApplicationContext(),
////                    Manifest.permission.READ_PHONE_STATE)
////                    != PackageManager.PERMISSION_GRANTED ||
////                    ContextCompat.checkSelfPermission(getApplicationContext(),
////                            Manifest.permission.ACCESS_FINE_LOCATION)
////                            != PackageManager.PERMISSION_GRANTED ||
////                    ContextCompat.checkSelfPermission(getApplicationContext(),
////                            Manifest.permission.ACCESS_COARSE_LOCATION)
////                            != PackageManager.PERMISSION_GRANTED
////            ) {
////                // ******** fetch location and then send location to server
////                locationFetch();
////            }
//
//
//        } else {
//
//            // ************title is not "ping" so just open application
//
////            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
////            startActivity(intent1);
//        }


    }

    private void showFileNotification(String fileId, String fileName, String description, String fileUrl, Bitmap bitmap) {
        NotificationFile notificationFile  = new NotificationFile();
        notificationFile.setFileName(fileName);
        notificationFile.setDescription(description);
        notificationFile.setDownloadUrl(fileUrl);


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("From", "fileFrag");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_FOR_MESSAGE)
                .setSmallIcon(R.drawable.home_64)
                .setContentTitle(fileName)
                .setContentText(description)
                .setLargeIcon(bitmap)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1234, builder.build());
    }

    private long getCurrentTimeStamp() {
        Long tsLong = System.currentTimeMillis();
//        String ts = tsLong.toString();
        return tsLong;
    }


    public void locationFetch() {
        DeviceLocation.LocationResult locationResult = new DeviceLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if (location != null) {

                    double latitudeValue = location.getLatitude();
                    double longitudeValue = location.getLongitude();
                    String locationString = makeLocationString(latitudeValue, longitudeValue);
                    if (locationString.length() > 1) {
                        sendLocationDataToServer(getApplicationContext(), locationString);
                    }
                    Log.v("_mdm_", " MainActivity location result lat: " + latitudeValue + " long: " + longitudeValue);
                } else {
                    Log.v("_mdm_", " MainActivity location result NULL ");

                }
            }
        };

        DeviceLocation myLocation = new DeviceLocation();
        myLocation.getLocation(getApplicationContext(), locationResult);
    }

    private String makeLocationString(double latitude, double longitude) {
        String date = Constants.getTodayDateString();
        String timestamp = new Date().getTime() + "";
        String locationString = "";
        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return "";
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            imeiOne = AppPreference.getImeiOne(getApplicationContext());
            imeiTwo = AppPreference.getImeiTwo(getApplicationContext());
        } else{

            imeiOne = tm.getDeviceId(1);
            imeiTwo = tm.getDeviceId(2);
        }
        if (imeiOne == null && imeiOne.length()<1){
            imeiOne = "111111111111111";
            imeiTwo = "222222222222222";
        }
        if (imeiOne.length() > 0) {
            locationString = imeiOne + "," + imeiTwo + "," + date + "," + timestamp + "," + latitude + "," + longitude + "#";

        }

        return locationString;
    }

    private void setIMEIValues(){
        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            imeiOne = AppPreference.getImeiOne(getApplicationContext());
            imeiTwo = AppPreference.getImeiTwo(getApplicationContext());
        } else{
            imeiOne = tm.getDeviceId(1);
            imeiTwo = tm.getDeviceId(2);
        }
        if (imeiOne == null ){
            imeiOne = "111111111111111";
            imeiTwo = "222222222222222";
        }
    }

    private void sendLocationDataToServer(final Context mContext, final String allLocationString) {

//        Log.d("_mdm_", "sending location data string:" + allLocationString);
//
//        RequestQueue queue = Volley.newRequestQueue(mContext);
//        String LOCATION_API = ApiConstants.getLocationApi(mContext);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, LOCATION_API, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                if (response.equalsIgnoreCase("updated") || response.equalsIgnoreCase("success")) {
//                    if (response.equalsIgnoreCase("success")) {
//
//                    }
//
//                }
//                Log.d("_mdm_", "sending location data response:" + response);
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//                if (error instanceof NetworkError) {
//                    Log.d("_mdm_", "location sending response: Network error");
//
//                } else if (error instanceof ServerError) {
//                    Log.d("_mdm_", "location ss sending response: Server error");
//
//                } else if (error instanceof AuthFailureError) {
//                    Log.d("_mdm_", "location sending response: Auth. error");
//                } else if (error instanceof ParseError) {
//                    Log.d("_mdm_", "location sending response: Parse error");
//                } else if (error instanceof TimeoutError) {
//                    Log.d("_mdm_", "location sending response: timeout error");
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
//                Log.v("_mdm_ location string: ", allLocationString);
//
//
//                return params;
//            }
//        };
//        stringRequest.setShouldCache(false);
//        queue.add(stringRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }


    public void showNotification(String notificationTitle,  String notificationBody, String date, String imgUrl){
//        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.home_64) // notification icon
//                .setContentTitle(notificationTitle) // title for notification
//                .setAutoCancel(true); // clear notification after click
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(pi);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, mBuilder.build());
//
        Message message_serializable  = new Message();
        message_serializable.setMessageTitle(notificationTitle);
        message_serializable.setMessageDetails(notificationBody);
        message_serializable.setDateTime(date);
        message_serializable.setImageUrl("");


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notification", message_serializable);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_FOR_MESSAGE)
                .setSmallIcon(R.drawable.home_64)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1234, builder.build());
    }

    private String makeJsonObject(String token){
        JSONObject jsonObject = new JSONObject();;
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

    private void sendFCMTokenToHTTPSServer(final Context mContext, final String token){
        Log.d("_mdm_", "******* sendFCMTokenToHTTPSServer called : AppMessagingService ******");

        if (!TextUtils.isEmpty(imeiOne) && !TextUtils.isEmpty(token)){
            String requestBody = makeJsonObject(token);
            if (TextUtils.isEmpty(requestBody) && Constants.isNetworkAvailable(mContext)){
                String TOKEN_API = ApiConstants.getTokenApi();
                HttpsVolleyMethods httpsVolleyMethods = new HttpsVolleyMethods();
                httpsVolleyMethods.sendPostRequestToServer(mContext, TOKEN_API, requestBody, new HttpsVolleyCallback() {
                    @Override
                    public void success(String response) {
                        Log.d("_https_", "token response: "+response);
                    }
                });
            }
        }
    }


    private void sendFCMTokenToServer(final Context applicationContext, final String token) {
//
//        Log.d("_mdm_", "******* sendFCMTokenToServer called : AppMessagingService ******");
//
//        final String serial = getSerialNumber(); //  http://43.224.110.67:8080/mdmapi/api/mobimanagerusers/save-token-info
//
//        if (imeiOne.length() > 5 && serial.length() > 0 && token.length() > 0) {
//
//            Log.d("_mdm_", "sendFCMTokenToServer called condition ok");
//            RequestQueue queue = Volley.newRequestQueue(applicationContext);
//            String TOKEN_API = ApiConstants.getFcmTokenApi(getApplicationContext());
//            Log.d("_ndm_", "FCM  api address : " + TOKEN_API);
//            StringRequest stringRequest = new StringRequest(Request.Method.GET, TOKEN_API, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.d("_mdm_", "sending token api response ** :" + response+"   ---------->>>>AppMessagingService");
//                    if (response.equalsIgnoreCase("updated") || response.equalsIgnoreCase("success")
//                            || response.equalsIgnoreCase("already exist")) {
//                        Log.d("_mdm_", "FCM  api: " + response+"   ---------->>>>AppMessagingService");
//
//                        SharedPreferences preferences = applicationContext.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = preferences.edit();
//                        editor.putBoolean(Constants.TOKEN_SHOULD_SENT, false);
//                        editor.commit();
//
//                        sessionManager.isTokenSend(true);
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
    private String getSerialNumber() {
        String serialNumber = "";
        if (android.os.Build.VERSION.SDK_INT == 26) {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

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

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateImageNotification(String contentTitle, String contentBody, String date, String imgUrl, Bitmap bitmap){

        Message message_serializable  = new Message();
        message_serializable.setMessageTitle(contentTitle);
        message_serializable.setMessageDetails(contentBody);
        message_serializable.setDateTime(date);
        message_serializable.setImageUrl(imgUrl);

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("notification", message_serializable);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_FOR_MESSAGE)
                .setSmallIcon(R.drawable.home_64)
                .setContentTitle(contentTitle)
                .setContentText(contentBody)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1234, builder.build());
    }

    private void installApk(String path){
        Log.d("_mdm_ins", "try to install");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(path);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);






//        File apkFile = new File(path);
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
//        startActivity(intent);
    }

}




