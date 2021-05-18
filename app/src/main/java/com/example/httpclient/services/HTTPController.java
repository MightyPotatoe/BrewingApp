package com.example.httpclient.services;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class HTTPController {

    private final RequestQueue queue;
    private final String requestUrl;
    private String httpResponse = "";
    OnHttpResponseListener onHttpResponseListener;

    public static final String DEVICE_IP = "192.168.4.1";

    public static final String ERROR_RESPONSE = "Cannot access the Device!\nMake sure device is connected and IP is set correctly.";

    public static final String SEND_HELLO = "/hello";
    public static final String RECEIVE_HELLO = "HELLO";

    public HTTPController(Context context, String url) {
        this.queue = Volley.newRequestQueue(context);
        if (url != null){
            this.requestUrl = url;
        }
        else requestUrl = DEVICE_IP;
    }

    public void sendRequest(String request){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://" + requestUrl + request,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onHttpResponseListener.onResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                httpResponse = ERROR_RESPONSE;
                onHttpResponseListener.onResponseReceived(httpResponse);
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(11000,
                0,
                0));
        queue.add(stringRequest);
    }

    public interface OnHttpResponseListener{
        void onResponseReceived(String response);
    }

    public void setHttpResponseListener(OnHttpResponseListener onHttpResponseListener) {
        this.onHttpResponseListener = onHttpResponseListener;
    }

}

