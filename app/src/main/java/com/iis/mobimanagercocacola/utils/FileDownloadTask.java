package com.iis.mobimanagercocacola.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class FileDownloadTask extends AsyncTask<String, Integer, Boolean> {
    private static final String TAG = "FileDownloadTask";

    private DownloadCallback callback;

    public FileDownloadTask(DownloadCallback callback) {
        this.callback = callback;
    }
    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

/*    public static HttpsURLConnection setUpHttpsConnection(String urlString)
    {
        try
        {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            // My CRT file that I put in the assets folder
            // I got this file by following these steps:
            // * Go to https://littlesvr.ca using Firefox
            // * Click the padlock/More/Security/View Certificate/Details/Export
            // * Saved the file as littlesvr.crt (type X.509 Certificate (PEM))
            // The MainActivity.context is declared as:
            // public static Context context;
            // And initialized in MainActivity.onCreate() as:
            // MainActivity.context = getApplicationContext();
            InputStream caInput = new BufferedInputStream(context.getAssets().open("littlesvr.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());

            return urlConnection;
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
    }*/
    @Override
    protected Boolean doInBackground(String... params) {
        String fileUrl = params[0];
        String destinationPath = params[1];

        Log.e(TAG, "doInBacfgfgkground: " + fileUrl );
        try {
            URL url = new URL(fileUrl);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
           // setUpHttpsConnection(fileUrl);
          //  connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.connect();

            Log.e(TAG, "doInBackground: " + url );
            int fileLength = connection.getContentLength();
            Log.e(TAG, "doInBackground: r" + fileLength );
            InputStream input = new BufferedInputStream(url.openStream());
            Log.e(TAG, "doInBackground: ff"  );
            FileOutputStream output = new FileOutputStream(destinationPath);
            Log.e(TAG, "doInBackground: fgff"  );


            byte[] data = new byte[1024];
            long total = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
               // Log.e(TAG, "doInBackground: " + count );
            }

            output.flush();
            output.close();
            input.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error downloading file: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        callback.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            callback.onDownloadComplete();
        } else {
            callback.onDownloadFailed();
        }
    }

    public interface DownloadCallback {
        void onProgressUpdate(int progress);

        void onDownloadComplete();

        void onDownloadFailed();
    }
}

