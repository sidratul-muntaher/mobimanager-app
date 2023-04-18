package com.iis.mobimanager2.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.iis.mobimanager2.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class HttpsVolleyMethods {
    public void getRequestWithPathParam(Context mContext, String urlWithPathData, final HttpsVolleyCallback callback){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithPathData, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("_https_", "response :" + response.toString());
                callback.success(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("https error", ""+error.toString()+"--------->"+urlWithPathData);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(mContext, new HurlStack(null, getSocketFactory(mContext)));
        queue.add(stringRequest);
    }

    public void httpsPutRequestWithPathParams(Context mContext, String urlWithPathData, final HttpsVolleyCallback callback){

        String url = urlWithPathData; //"https://43.224.110.67:8443/MobiManager/api/publish-plan";

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("_https_", "response :" + response.toString());
                callback.success(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("https error", ""+error.toString()+"--------->"+urlWithPathData);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(mContext, new HurlStack(null, getSocketFactory(mContext)));
        queue.add(stringRequest);
    }


    public void httpsSendLocationPostRequest(Context mContext,String url, String locationData, final HttpsVolleyCallback callback){

        //"https://43.224.110.67:8443/MobiManager/api/publish-plan";


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.success(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("_https_ error>>",error.toString()+"");
                Log.e("https error", ""+error.toString()+"--------->"+url);

            }
        }) {
            @Override
            public String getBodyContentType() {

                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return  locationData == null ? null : locationData.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", locationData, "utf-8");
                    return null;
                }
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                String message = ""+response;
                Log.e("_https_ error> ",response.toString());
                if (response != null) {
                    // responseString = String.valueOf(response.data);
                    // can get more details such as response.headers
                    message = new String(response.data);

                }
                return Response.success(message, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        RequestQueue queue = Volley.newRequestQueue(mContext, new HurlStack(null, getSocketFactory(mContext)));
        queue.add(stringRequest);
    }

    public void sendPostRequestToServer(final Context mContext, String URL, final String requestBoddy, final HttpsVolleyCallback callback){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.success(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error>>",error.toString()+"");
                Log.e("https error", ""+error.toString()+"--------->"+URL);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return  requestBoddy == null ? null : requestBoddy.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBoddy, "utf-8");
                    return null;
                }
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                String message = ""+response;
                Log.e("error> ",response.toString());
                if (response != null) {
                    // responseString = String.valueOf(response.data);
                    // can get more details such as response.headers
                    message = new String(response.data);

                }
                return Response.success(message, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        RequestQueue queue = Volley.newRequestQueue(mContext, new HurlStack(null, getSocketFactory(mContext)));
        queue.add(stringRequest);
    }

    private SSLSocketFactory getSocketFactory(Context mContext) {

        CertificateFactory cf = null;
        try {

            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = mContext.getResources().openRawResource(R.raw.inovex_server);
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
                    Log.d("hostname", "hostname: "+hostname);
                    Log.e("CipherUsed", session.getCipherSuite());
                    return hostname.compareTo("43.224.110.67")==0; //The Hostname of your server. e.g : "43.224.110.67"

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
