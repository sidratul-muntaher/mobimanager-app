package com.iis.mobimanagercedp.utils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.iis.mobimanagercedp.R;
import com.iis.mobimanagercedp.model.AsyncTaskParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public class FileDownloaderAsyncTaskForHTTPS extends AsyncTask<AsyncTaskParams, Void, Bitmap> {

//    private final WeakReference<ImageView> imageViewReference;
    Resources resources = null;
    Context mContext;
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private String fileName, fileType, fileUrl;
    int lengthOfFile = 0;


    public FileDownloaderAsyncTaskForHTTPS(Context mContext) {
//        imageViewReference = new WeakReference<ImageView>(imageView);
        this.mContext = mContext;
    }

    @Override
    protected Bitmap doInBackground(AsyncTaskParams... params) {
        this.fileType = params[0].getFileType();
        this.fileUrl = params[0].getFileUrl();
        this.fileName = params[0].getFileName();

        return download(fileUrl);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
//        if (isCancelled()) {
//            bitmap = null;
//        }
//
//        if (imageViewReference != null) {
//            ImageView imageView = imageViewReference.get();
//            if (imageView != null) {
//                if (bitmap != null) {
//                    imageView.setImageBitmap(bitmap);
//                } else {
//                    Log.d("Downloading the image: ", "No Image found");
//                }
//            }
//
//        }



        if (!TextUtils.isEmpty(fileType) && fileType.equalsIgnoreCase("crt")){
            installCertificates(fileName);
        }
    }

    //URL connection to download the image
    private Bitmap download(String url) {

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnection2 = null;

        CertificateFactory cf = null;
        try {

            //check to see if the image is coming from a HTTP connection
            //then download via a HTTP connection
            //if not then use a HTTPS connection
            if (url.contains("https")) {
                try {
                    Log.d("Use HTTPS", url);
                    URL urlHTTPS = new URL(url);
                    urlConnection2 = (HttpsURLConnection) urlHTTPS.openConnection();

                    // Load CAs from an InputStream
                    // (could be from a resource or ByteArrayInputStream or ...)
                    cf = CertificateFactory.getInstance("X.509");
                    InputStream caInput = mContext.getResources().openRawResource(R.raw.inovex_server);
                    Certificate ca;
                    try {

                        ca = cf.generateCertificate(caInput);
                        Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
                    } finally {
                        caInput.close();
                    }

                    // Create a KeyStore containing our trusted CAs
                    String keyStoreType = KeyStore.getDefaultType();
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);



                    // Create a TrustManager that trusts the CAs in our KeyStore
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(keyStore);

                    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            Log.d("hostname", "hostname: "+hostname);
                            Log.e("CipherUsed", session.getCipherSuite());
                            return hostname.compareTo("43.224.110.67")==0; //The Hostname of your server. e.g : "43.224.110.67"

                        }
                    };

                    // Create an SSLContext that uses our TrustManager
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);
                    urlConnection2.setSSLSocketFactory(context.getSocketFactory());

//                    this.lengthOfFile = urlConnection2.getContentLength();

                    int statusCode = urlConnection2.getResponseCode();
                    Log.d("URL2 Status: ", Integer.toString(statusCode));
                    //check if the HTTP status code is equal to 200, which means that it is ok
                    if (statusCode != 200) {
                        return null;
                    }
                    InputStream in = urlConnection2.getInputStream();
                    if (in != null) {
                        Bitmap bitmap = null;
                        if (isImage()){
                            bitmap = BitmapFactory.decodeStream(in);
                            downloadImageFile(bitmap);
                        }else {
                            downloadFile(in);
                        }

                        return bitmap;
                    }
                } catch (Exception e) {
                    urlConnection2.disconnect();
                    Log.d("Downloading the image:", "Error downloading image from " + url);
                }

            } else {
                URL uri = new URL(url);
                urlConnection = (HttpURLConnection) uri.openConnection();
                urlConnection.setRequestMethod("GET");

//                this.lengthOfFile = urlConnection.getContentLength();
                int statusCode = urlConnection.getResponseCode();
                //check if the HTTP status code is equal to 200, which means that it is ok
                if (statusCode != 200) {
                    return null;
                }

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    Bitmap bitmap = null;
                    if (isImage()){
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        downloadImageFile(bitmap);
                    }else {
                        downloadFile(inputStream);
                    }

                    return bitmap;
                }
            }

        } catch (Exception e) {
            urlConnection.disconnect();
            Log.d("ImageDownloader", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (urlConnection2 != null) {
                urlConnection2.disconnect();
            }
        }
        return null;
    }



    private void downloadFile(InputStream inputStream) {
        if (checkPermission()) {
            return;
        } else {
            String finalFileName =  fileName + "." + fileType;
            File file = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager", finalFileName);
            //File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+
                    //File.separator+fileName+"."+fileType);
            if (!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            try {
//                file = File.createTempFile(
//                        this.fileName,      /* prefix */
//                        "."+this.fileType,         /* suffix */
//                        storageDir          /* directory */
////                        AppUtil.getDirNameForDownloadFiles(mContext, this.fileType)          /* directory */
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            if (file != null && !isImage()){
                int count;
                OutputStream output = null;

                try {
                    output = new FileOutputStream(file);
                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = inputStream.read(data)) != -1) {
                        total += count;

                        output.write(data, 0, count);
                    }
                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else {
                if(file.exists() && file.length() < 1){
                    try {
                        file.getCanonicalFile().delete();
                        if(file.exists()){
                            mContext.deleteFile(file.getName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }



    private void downloadImageFile(Bitmap bitmap) {
        if (checkPermission()) {
            return;
        } else {
            String finalFileName =  fileName + "." + fileType;
            File file = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager", finalFileName);

            //File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)+
                    //File.separator+fileName+"."+fileType);
            if (!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if (file != null && isImage()) {
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                if(file.exists() && file.length() < 1){
                    try {
                        file.getCanonicalFile().delete();
                        if(file.exists()){
                            mContext.deleteFile(file.getName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private void installCertificates(String fileName){
        String finalFileName =  fileName + "." + fileType;
        File file = new File(Environment.getExternalStorageDirectory() + "/Download/Mobimanager", finalFileName);
        //File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+
                //File.separator+fileName+"."+fileType);
        Log.d("https_download", "crt file :"+file);

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


    private boolean isImage(){
        if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")
                || fileType.equalsIgnoreCase("png")){
            return true;
        }
        return false;
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(mContext, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

}
