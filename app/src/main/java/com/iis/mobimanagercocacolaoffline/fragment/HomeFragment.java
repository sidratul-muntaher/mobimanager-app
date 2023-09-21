package com.iis.mobimanagercocacolaoffline.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iis.mobimanagercocacolaoffline.BuildConfig;
import com.iis.mobimanagercocacolaoffline.IApi;
import com.iis.mobimanagercocacolaoffline.R;
import com.iis.mobimanagercocacolaoffline.adapter.AppListRecycler;
import com.iis.mobimanagercocacolaoffline.data.AppDatabaseHelper;
import com.iis.mobimanagercocacolaoffline.data.HttpsVolleyCallback;
import com.iis.mobimanagercocacolaoffline.data.HttpsVolleyMethods;
import com.iis.mobimanagercocacolaoffline.model.AppList;
import com.iis.mobimanagercocacolaoffline.model.X;
import com.iis.mobimanagercocacolaoffline.receiver.AlarmManagerReceiver;
import com.iis.mobimanagercocacolaoffline.service.AppService;
import com.iis.mobimanagercocacolaoffline.utils.ApiConstants;
import com.iis.mobimanagercocacolaoffline.utils.Constants;
import com.iis.mobimanagercocacolaoffline.utils.DeviceAdminHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.iis.mobimanagercocacolaoffline.utils.AppPreference;
import com.iis.mobimanagercocacolaoffline.utils.FileDownloadTask;
import com.iis.mobimanagercocacolaoffline.utils.SessionManager;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;
import static com.iis.mobimanagercocacolaoffline.utils.Constants.getTodayDateString;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    ImageView imageView, iv;
    RecyclerView appList;
    ArrayList<AppList> appLists;
    AppListRecycler adapter;
    AppDatabaseHelper databaseHelper;
    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.fragment_home, container, false);
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

       // Picasso.get().load("https://43.224.110.67:8443/MobiManager/api/downloadFile1/enroll_qr.png").into(imageView);


       /* startDownloads();
        iv.setOnClickListener(i->{
            appLists = new ArrayList<>();
            adapter.notifyDataSetChanged();
            adapter.setAppLists(appLists);
            startDownloads();

        });*/
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
            Log.e("_mdm_service_", "onCreate: APP Service");
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



        String O = one();

        view.findViewById(R.id.cd1).setOnClickListener(i -> {
            Intent intentOne = new Intent(Intent.ACTION_VIEW);

            intentOne.setDataAndType(FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider",
                            new File(O)),
                    "application/vnd.android.package-archive");

            intentOne.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intentOne.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentOne.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.e(TAG, "startDownloads: ------------------ ");
            startActivity(intentOne);




        });

        String two = two();
        view.findViewById(R.id.cd2).setOnClickListener(i -> {

            Intent intentTwo = new Intent(Intent.ACTION_VIEW);

            intentTwo.setDataAndType(FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider",
                            new File(two)),
                    "application/vnd.android.package-archive");

            intentTwo.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intentTwo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentTwo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.e(TAG, "startDownloads: ------------------ ");
            startActivity(intentTwo);



        });

        String f = four();
        view.findViewById(R.id.cd4).setOnClickListener(i -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider",
                            new File(f)),
                    "application/vnd.android.package-archive");

            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.e(TAG, "startDownloads: ------------------ ");
            startActivity(intent);




        });
        AtomicReference<String> three = new AtomicReference<>(three());
        view.findViewById(R.id.cd3).setOnClickListener(i -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);

            three.set(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA Pricing Service" + ".apk");
            intent.setDataAndType(FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider",
                            new File(three.get())),
                    "application/vnd.android.package-archive");

            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.e(TAG, "startDownloads: ------------------ ");
            startActivity(intent);



        });
//askForPermissions();
        return view;
    }

    public  String four()

    {

        AssetManager assetManager = getContext().getAssets();
        String x = "";
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open("SFA.apk");
            Log.e(TAG, "startDownloads: ----------fdfg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA" + ".apk");
            }


            Log.e(TAG, "startDownloads: 00000000000000000000000000000000000000000");
            byte[] buffer = new byte[1024];

            int read;
            while ((read = in.read(buffer)) != -1) {

                out.write(buffer, 0, read);

            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;


            x = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA" + ".apk";


            String APKFilePath =x; //For example...
            PackageManager pm = getContext().getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ImageView iv = view.findViewById(R.id.icon4);
            TextView  status, ver, pkg;
            ver = view.findViewById(R.id.appVersion);
            pkg = view.findViewById(R.id.apppkg4);
            status = view.findViewById(R.id.appStatus);
            ver.setText("Version: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);


        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }

        return x;

    }

    public   String three()

    {

        AssetManager assetManager = getContext().getAssets();
        String x = "";
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open("SFA Pricing Service.apk");
            Log.e(TAG, "startDownloads: ----------fdfg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA Pricing Service" + ".apk");
            }

            Log.e(TAG, "startDownloads: 00000000000000000000000000000000000000000");
            byte[] buffer = new byte[1024];

            int read;
            while ((read = in.read(buffer)) != -1) {

                out.write(buffer, 0, read);

            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;


            x = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA Pricing Service" + ".apk";
            String APKFilePath =x; //For example...
            PackageManager pm = getContext().getPackageManager();
            PackageInfo    pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ImageView iv = view.findViewById(R.id.icon3);
            TextView  status, ver, pkg;
            ver = view.findViewById(R.id.verSPS);
            pkg = view.findViewById(R.id.appPKG3);
            status = view.findViewById(R.id.appStatus3);
            ver.setText("Version: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);

        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }

        return x;

    }

    public    String two() {

        AssetManager assetManager = getContext().getAssets();
        String x = "";
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open("PROGOTI.apk");
            Log.e(TAG, "startDownloads: ----------fdfg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "Progoti" + ".apk");
            }

            Log.e(TAG, "startDownloads: 00000000000000000000000000000000000000000");
            byte[] buffer = new byte[1024];

            int read;
            while ((read = in.read(buffer)) != -1) {

                out.write(buffer, 0, read);

            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;


            x = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "Progoti" + ".apk";
            String APKFilePath =x; //For example...
            PackageManager pm = getContext().getPackageManager();
            PackageInfo    pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ImageView iv = view.findViewById(R.id.icon2);
            TextView  status, ver, pkg;
            ver = view.findViewById(R.id.verSAF);
            pkg = view.findViewById(R.id.appPkg2);
            status = view.findViewById(R.id.appStatus2);
            ver.setText("Version: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);
        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }

        return x;

    }
    public String  one() {
        AssetManager assetManager = getContext().getAssets();
        String x = "";
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open("Happy Partner.apk");
            Log.e(TAG, "startDownloads: ----------fdfg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "Happy Partner" + ".apk");
            }

            Log.e(TAG, "startDownloads: 00000000000000000000000000000000000000000");
            byte[] buffer = new byte[1024];

            int read;
            while ((read = in.read(buffer)) != -1) {

                out.write(buffer, 0, read);

            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;


            x = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "Happy Partner" + ".apk";
            ImageView iv = view.findViewById(R.id.icon1);
            TextView  status, ver, pkg;
            ver = view.findViewById(R.id.happyV);
            pkg = view.findViewById(R.id.appPkg1);
            status = view.findViewById(R.id.appStat1);
            String APKFilePath =x; //For example...
            PackageManager pm = getContext().getPackageManager();
            PackageInfo    pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ver.setText("Version: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getContext().getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);
            Log.e(TAG, "onCreate: xxxxxxxxxxxxxx " );
        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }
        return x;
    }

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=getContext().getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
    public static OkHttpClient.Builder getUnsafeOkHttpClient() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String
                                authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String
                                authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } }

    private void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }
    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = Uri.fromFile(apkFile);

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android 8.0 and above, request the user's consent to install
            if (!getContext().getPackageManager().canRequestPackageInstalls()) {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                permissionIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
                startActivity(permissionIntent);
                return;
            }
        }

        startActivity(intent);
    }

    /*public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
        }
    }*/
    private void startDownloads() {
        // Replace these URLs with the actual file URLs you want to download
        String[] fileUrls = {
                "http://www.quran.gov.bd/quran/pdf/abe/fabe.pdf",
                "https://r-static-assets.androidapks.com/rdata/c995e8e1482299345aabe7c626ae51bc/cn.xiaofengkj.fitpro_v2.3.4-125_Android-6.0.apk",
                "https://d.apkpure.com/b/APK/com.delivery.india.client?version=latest"
        };

        AssetManager assetManager = getContext().getAssets();

        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open("SFA.apk");
            Log.e(TAG, "startDownloads: ----------fdfg" );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA" + ".apk");
            }

            Log.e(TAG, "startDownloads: 00000000000000000000000000000000000000000" );
            byte[] buffer = new byte[1024];

            int read;
            while((read = in.read(buffer)) != -1) {

                out.write(buffer, 0, read);

            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            Intent intent = new Intent(Intent.ACTION_VIEW);

            String x = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA" + ".apk";
            intent.setDataAndType(FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider",
                    new File(x)),
                    "application/vnd.android.package-archive");

            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.e(TAG, "startDownloads: ------------------ " );
            startActivity(intent);

        } catch(Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e );
        }

        OkHttpClient.Builder client = new OkHttpClient().newBuilder();
        client.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        OkHttpClient c = client.build();


        Retrofit retrofit = new Retrofit.Builder().
                baseUrl(ApiConstants.BASE_HTTPS_URL).client(getUnsafeOkHttpClient().build()).addConverterFactory(GsonConverterFactory.create())
                .build();


        IApi api = retrofit.create(IApi.class);
        Call<X> call = api.getAppData();
        call.enqueue(new Callback<X>() {
            @Override
            public void onResponse(Call<X> call, Response<X> response) {
                Log.e("TAG", "onResponse: " + response.toString());
                databaseHelper.deleteApps();
                if ( response.body().getAppLists() == null) return;
                for (AppList a: response.body().getAppLists()
                     ) {

                    Log.e("TAG", "onResponse: " + a.appDownloadUrl );
                    AppList item = new AppList(a.getAppDownloadUrl());
                    item.setPackageName(a.getPackageName());
                    item.setAppIcon(a.getAppIcon());
                    item.setAppName(a.getAppName());
                    item.setMinVersion(a.getMinVersion());


                    databaseHelper.insertAppData(a.getAppName(), a.getPackageName(), a.getMinVersion(), a.getAppIcon(), a.getAppDownloadUrl());
                    appLists.add(item);
                    adapter.notifyDataSetChanged();
                    String value = (a.getPackageName() + " - " + a.getMinVersion());
                    String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + a.getAppName() + ".apk";
                    // absolutePath = absolutePath.substring(1);
                    File apkFile = new File(absolutePath);

                    //if () {
                    if (!apkFile.exists() ){
                        Log.e("TAG", "onResponse: " + a.getAppName() + apkFile.exists() );

                        //for (String fileUrl : fileUrls) {


                            FileDownloadTask downloadTask = new FileDownloadTask(new FileDownloadTask.DownloadCallback() {
                                @Override
                                public void onProgressUpdate(int progress) {
                                    item.setStatus(" downloading... " );
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onDownloadComplete() {
                                    item.setStatus("downloaded");
                                    adapter.notifyDataSetChanged();
                                    preferences.edit().putString(a.getPackageName(), value).commit();
                                }

                                @Override
                                public void onDownloadFailed() {
                                    item.setStatus("failed");
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            Log.e("TAG", "startyDownloads: " + getContext().getFilesDir().getAbsolutePath() );
                            downloadTask.execute(a.getAppDownloadUrl(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + a.getAppName() + ".apk");
                      //  }

                    }else{
                        Log.e(TAG, "onResponse: -------------" );
                        if(isAppInstalled(getContext(), a.getPackageName())){
                            item.setStatus("installed");
                        }else{
                            item.setStatus("downloaded");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<X> call, Throwable t) {

                Log.e("TAG", "onFailure: yuy" + t.toString() );
            }
        });

        if(!isNetworkConnected(getContext())) {


            for (AppList a : databaseHelper.getApplist()
            ) {
                Log.e("TAG", "startDownloads: " + a.packageName);
                //appLists.add(a);

                String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + a.getAppName() + ".apk";
                // absolutePath = absolutePath.substring(1);
                File apkFile = new File(absolutePath);

                //if () {
                if (apkFile.exists()) {

                    if (isAppInstalled(getContext(), a.getPackageName())) {

                        a.setStatus("installed");
                        appLists.add(a);
                    } else {
                        a.setStatus("not installed");
                        appLists.add(a);
                    }
                    for (AppList b : adapter.getAppLists()
                    ) {
                        if (!a.getPackageName().equals(b.getPackageName())) {

                        }
                    }

                } else {
                    a.setStatus("not found");
                    appLists.add(a);

                }
            }

        }
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.e("map values", entry.getKey() + ": " + entry.getValue().toString());

            if (entry.getKey().contains("com")){

            }
        }

    }
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
    private static final String TAG = "HomeFragment";
    public  boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            Log.e(TAG, "isAppInstalled: oio" );
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            Log.e("TAG", "isAppInstalled: " + appInfo );
            return appInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false; // Package name not found, app is not installed
        }
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
        Log.e("TAG", "getUserInfo: " + imei );
        if (!TextUtils.isEmpty(imeiOne) && imeiOne.length() > 5) {
            SessionManager sessionManager = new SessionManager(getContext());
            Log.e("_mdm_man", "session manager token send? "+sessionManager.isTokenSend());
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

        Log.e("TAG", "setIMEIValues: " + imeiOne + " " + imeiTwo );
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
