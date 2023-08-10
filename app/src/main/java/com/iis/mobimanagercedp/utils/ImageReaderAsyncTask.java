package com.iis.mobimanagercedp.utils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.iis.mobimanagercedp.R;

import java.io.InputStream;
import java.lang.ref.WeakReference;
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

public class ImageReaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    Resources resources = null;
    Context mContext;
    private static final int DEFAULT_BUFFER_SIZE = 8192;


    public ImageReaderAsyncTask(Context mContext, ImageView imageView) {
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.mContext = mContext;
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        return download(url[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.d("Downloading the image: ", "No Image found");
                }
            }

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
                        Bitmap bitmap = BitmapFactory.decodeStream(in);
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
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
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

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(mContext, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

}