package com.iis.mobimanagercocacola.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.iis.mobimanagercocacola.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class AppUtil {

    public static final String LOG_TAG = "AppUtil ";
    public static final String DIR_IMAGE = "image";
    public static final String DIR_AUDIO = "audio";
    public static final String DIR_VIDEO = "video";
    public static final String DIR_DOC = "pdf";


    // method for getting Dir using dirName
    public static File getFileStorageDir(Context context, String fileType) {

        String directoryName = getDirName(fileType);
        File file = new File(context.getExternalFilesDir(directoryName), directoryName);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Directory not created");
            }
        }
        return file;
    }

    public static String getDirName(String fileType) {

        if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")
                || fileType.equalsIgnoreCase("png")) {
            return  Environment.DIRECTORY_PICTURES;
        } else if (fileType.equalsIgnoreCase("mp3")) {
            return  Environment.DIRECTORY_MUSIC;
        } else if (fileType.equalsIgnoreCase("mp4")) {
            return  Environment.DIRECTORY_MOVIES;
        } else if (fileType.equalsIgnoreCase("apk")) {
            return  Environment.DIRECTORY_DOCUMENTS;
        } else {
            return  Environment.DIRECTORY_DOCUMENTS;
        }
    }

    public static File getDirNameForDownloadFiles(Context mContext, String fileType) {

        if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")
                || fileType.equalsIgnoreCase("png")) {
            return  mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else if (fileType.equalsIgnoreCase("mp3")) {
            return  mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        } else if (fileType.equalsIgnoreCase("mp4")) {
            return  mContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        } else if (fileType.equalsIgnoreCase("apk")) {
            return  mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        } else {
            return  mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        }
    }

    public static String getIntentViewType(String fileType) {

        if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")
                || fileType.equalsIgnoreCase("png")) {
            return  "image/*";
        } else if (fileType.equalsIgnoreCase("mp3")) {
            return  "audio/*";
        } else if (fileType.equalsIgnoreCase("mp4")) {
            return  "video/*";
        }
        else if (fileType.equalsIgnoreCase("pdf")) {
            return  "application/*";
        }else {
            return  "*/*";
        }
    }


    // return true if second paramenter
    public static int isAboveDailyLimit(int dailyLimit, int currentUsage){

        return 0;
    }


    // change status bar color
    public static void changeStatusBarColor(int color, Activity activity, Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(context,color));
        }else {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(context);
            View view = new View(context);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) window.getDecorView()).addView(view);
            view.setBackgroundColor(ContextCompat.getColor(context, color));
        }

    }

    // A method to find height of the status bar
    public static  int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // get vector drawable
    // get drawable support for all API version for android
    public static Drawable getDrawable(Context context, int vectorId) {
        Resources resources = context.getResources();
        Resources.Theme theme = context.getTheme();
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 26) {
            drawable = VectorDrawableCompat.create(resources, vectorId, theme);
        } else {
            drawable = context.getResources().getDrawable(vectorId);
        }

        return drawable;
    }

    public static long getTimestampFromDateString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = dateFormat.parse(dateString);
            return parsedDate.getTime();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long[] getTodayRange() {
        long timeNow = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new long[]{cal.getTimeInMillis(), timeNow};
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static long getStartOfDayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfDayInMillis() {
        // Add one day's time to the beginning of the day.
        // 24 hours * 60 minutes * 60 seconds * 1000 milliseconds = 1 day
        return getStartOfDayInMillis() + (24 * 60 * 60 * 1000);
    }

    private SSLSocketFactory getSocketFactory(Context mContext) {

        CertificateFactory cf = null;
        try {

            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = mContext.getResources().openRawResource(R.raw.mycertificate);
            Certificate ca;
            try {

                ca = cf.generateCertificate(caInput);
                Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }


            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);


            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);


            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {

                    Log.e("CipherUsed", session.getCipherSuite());
                    return hostname.compareTo("45.64.135.29")==0;
                  //  https://45.64.135.29:8443/
                    //The Hostname of your server.

                }
            };


            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            SSLContext context = null;
            context = SSLContext.getInstance("TLS");

            context.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            SSLSocketFactory sf = context.getSocketFactory();


            return sf;

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return  null;
    }

}
