package com.iis.mobimanagercedp.utils;

import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.iis.mobimanagercedp.model.AsyncTaskParams;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<AsyncTaskParams, String, String> {
    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     * */



    private String fileName, fileType, fileUrl;
    private Context mContext;

    public DownloadFileFromURL(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(AsyncTaskParams... params) {
        this.fileType = params[0].getFileType();
        this.fileUrl = params[0].getFileUrl();
        this.fileName = params[0].getFileName();

        download(fileName, fileType, fileUrl, mContext);


//        silentlyDownload();


        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
//        pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
//        dismissDialog(progress_bar_type);

        Log.d("_file_", "download complete");
        installCertificates(fileName);
    }

    private void download (String fileName, String fileType, String url, Context context){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setDescription(fileName);
        request.setTitle("MDM FILE DOWNLOAD");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, AppUtil.getDirName(fileType), fileName);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

    }

    private void installCertificates(String fileName){

        File file = new File(mContext.getExternalFilesDir(AppUtil.getDirName("crt")), fileName);
        Toast.makeText(mContext, ""+file, Toast.LENGTH_LONG).show();

        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            final InputStream inputStream = fileInputStream;

            DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);

            if(! installCaCertificateN(inputStream, dpm,  null))
            {
                Toast.makeText(mContext,"installCaCert  Failed",Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("_mdm_file_", "file : "+file.toString());
    }

    private boolean installCaCertificateN(InputStream certificateInputStream, DevicePolicyManager dpm, ComponentName admin) {
        Log.d("_mdm_file_", "called installCaCertificateN");

        byte[] cert = new byte[0];
        try {
            Log.d("_mdm_file_", "trying to install ca file");
            cert = new byte[certificateInputStream.available()];
            certificateInputStream.read(cert);
            dpm.installCaCert(null, cert);
        } catch (IOException e) {
            Log.d("_mdm_file_", "installCaCertificate error: "+e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void silentlyDownload(){
        if (this.fileUrl != null && this.fileName != null && this.fileType != null){
            int count;
            InputStream input = null;
            OutputStream output = null;
            try {
                File folder = new File(AppUtil.getDirName(fileType));
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    // Do something on success
                } else {
                    // Do something else on failure
                }
                URL url = new URL(this.fileUrl);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream
                //extension must change (mp3,mp4,zip,apk etc.)
                output = new FileOutputStream(AppUtil.getDirName(fileType)+"/"+fileName+"."+fileType);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }
                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
        }
    }


}
