package com.iis.mobimanagercocacolaoffline.activity;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.iis.mobimanagercocacolaoffline.BuildConfig;
import com.iis.mobimanagercocacolaoffline.R;
import com.iis.mobimanagercocacolaoffline.adapter.AppListRecycler;
import com.iis.mobimanagercocacolaoffline.data.AppDatabaseHelper;
import com.iis.mobimanagercocacolaoffline.model.AppList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Apps extends AppCompatActivity {

    ArrayList<AppList> appLists;
    AppListRecycler adapter;
    RecyclerView appList;

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || (ContextCompat.checkSelfPermission(this,
                READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(getString(R.string.permission_necessary));
                alertBuilder.setMessage(R.string.storage_permission_is_encessary_to_wrote_event);
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getParent(), new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE,
                        }, 45);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION,
                        READ_PHONE_STATE}, 434);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);


        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                // Request permission to install from unknown sources
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                permissionIntent.setData(Uri.parse("package:" + getPackageName()));
                Log.e("TAG", "onBindViewHolder: ---- ");
                startActivityForResult(permissionIntent, 90);
            }
        }
        if (checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE,
                    READ_PHONE_STATE}, 434);
        }

        String O = one();
        findViewById(R.id.cd1).setOnClickListener(i -> {
            Intent intentOne = new Intent(Intent.ACTION_VIEW);

            intentOne.setDataAndType(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                                new File(O)),
                        "application/vnd.android.package-archive");

            intentOne.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intentOne.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentOne.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.e(TAG, "startDownloads: ------------------ ");
                startActivity(intentOne);




        });

        String two = two();
        findViewById(R.id.cd2).setOnClickListener(i -> {

            Intent intentTwo = new Intent(Intent.ACTION_VIEW);

            intentTwo.setDataAndType(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                                new File(two)),
                        "application/vnd.android.package-archive");

            intentTwo.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intentTwo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentTwo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.e(TAG, "startDownloads: ------------------ ");
                startActivity(intentTwo);



        });

        String f = four();
        findViewById(R.id.cd4).setOnClickListener(i -> {

                Intent intent = new Intent(Intent.ACTION_VIEW);

                 intent.setDataAndType(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                                new File(f)),
                        "application/vnd.android.package-archive");

                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.e(TAG, "startDownloads: ------------------ ");
                startActivity(intent);




        });
        AtomicReference<String> three = new AtomicReference<>(three());
        findViewById(R.id.cd3).setOnClickListener(i -> {

                Intent intent = new Intent(Intent.ACTION_VIEW);

                 three.set(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "SFA Pricing Service" + ".apk");
                intent.setDataAndType(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                                new File(three.get())),
                        "application/vnd.android.package-archive");

                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.e(TAG, "startDownloads: ------------------ ");
                startActivity(intent);



        });


    }



    public  String four()

    {

        AssetManager assetManager = getAssets();
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
            PackageManager pm = getPackageManager();
            PackageInfo    pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ImageView iv = findViewById(R.id.icon4);
            TextView  status, ver, pkg;
            ver = findViewById(R.id.appVersion);
            pkg = findViewById(R.id.apppkg4);
            status = findViewById(R.id.appStatus);
            ver.setText("Version: "+getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);


        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }

        return x;

        }

      public   String three()

    {

        AssetManager assetManager = getAssets();
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
            PackageManager pm = getPackageManager();
            PackageInfo    pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ImageView iv = findViewById(R.id.icon3);
            TextView  status, ver, pkg;
            ver = findViewById(R.id.verSPS);
            pkg = findViewById(R.id.appPKG3);
            status = findViewById(R.id.appStatus3);
            ver.setText("Version: "+getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);

        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }

        return x;

        }

     public    String two() {

        AssetManager assetManager = getAssets();
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
            PackageManager pm = getPackageManager();
            PackageInfo    pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ImageView iv = findViewById(R.id.icon2);
            TextView  status, ver, pkg;
            ver = findViewById(R.id.verSAF);
            pkg = findViewById(R.id.appPkg2);
            status = findViewById(R.id.appStatus2);
            ver.setText("Version: "+getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);
        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }

        return x;

        }
   public String  one() {
        AssetManager assetManager = getAssets();
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
            ImageView iv = findViewById(R.id.icon1);
            TextView  status, ver, pkg;
            ver = findViewById(R.id.happyV);
            pkg = findViewById(R.id.appPkg1);
            status = findViewById(R.id.appStat1);
            String APKFilePath =x; //For example...
            PackageManager pm = getPackageManager();
            PackageInfo    pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir       = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;
            //

            Drawable APKicon = pi.applicationInfo.loadIcon(pm);
            ver.setText("Version: "+getPackageManager().getPackageArchiveInfo(x, 0).versionName);
            pkg.setText("Pkg: "+getPackageManager().getPackageArchiveInfo(x, 0).packageName);
            status.setText(Html.fromHtml(isPackageExisted(getPackageManager().getPackageArchiveInfo(x, 0).packageName) ? "<font color=#000>Status: </font> <font color=#008577>Installed</font>" : "<font color=#000>Status: </font> <font color=#D81B60>Not Installed</font>"));
            iv.setImageDrawable(APKicon);
            Log.e(TAG, "onCreate: xxxxxxxxxxxxxx " );
        } catch (Exception e) {
            Log.e(TAG, "startDownloads:-----------909 " + e);
        }
        return x;
}

    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private static final String TAG = "Apps";
    AppDatabaseHelper databaseHelper = new AppDatabaseHelper(this);

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
    private void startDownloads() {
        // Replace these URLs with the actual file URLs you want to download
        String[] fileUrls = {
                "http://www.quran.gov.bd/quran/pdf/abe/fabe.pdf",
                "https://r-static-assets.androidapks.com/rdata/c995e8e1482299345aabe7c626ae51bc/cn.xiaofengkj.fitpro_v2.3.4-125_Android-6.0.apk",
                "https://d.apkpure.com/b/APK/com.delivery.india.client?version=latest"
        };


    }
}