package com.example.httpclient.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.httpclient.R;
import com.example.httpclient.services.HTTPController;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DeviceIpActivity extends AppCompatActivity implements HTTPController.OnHttpResponseListener {

    TextView headerTV;
    TextView captionTV;
    CircularProgressIndicator progressIndicator;
    Button button;

    HTTPController httpController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_ip);
        button = findViewById(R.id.DAI_refresh);
        headerTV = findViewById(R.id.DAI_header);
        captionTV = findViewById(R.id.DAI_caption);
        progressIndicator = findViewById(R.id.DAI_progressIndicator);

        //Connecting view
        loadConnectingView();
    }

    public void onConnectButtonClick(View view) {
        loadConnectingView();
    }

    @Override
    public void onResponseReceived(String response) {
        if(response.contains(HTTPController.RECEIVE_HELLO)){
            captionTV.setText(R.string.DIA_connected);
            //starting new Activity
            Intent intent = new Intent(this, DeviceSettingActivity.class);
            startActivity(intent);
        }
        else {
            loadConnectionFailedView();
        }
    }


    //-------------------CONNECTING VIEW------------------------------------------------------------
    private void loadConnectingView(){
        headerTV.setText(R.string.DIA_connecting);
        captionTV.setText(R.string.DIA_please_wait);
        progressIndicator.setVisibility(View.VISIBLE);
        button.setVisibility(View.INVISIBLE);

        //Connect to httpController
        httpController = new HTTPController(this,null);
        httpController.setHttpResponseListener(this);
        httpController.sendRequest(HTTPController.SEND_HELLO);
    }

    //-------------------CONNECTION FAILED VIEW-----------------------------------------------------
    private void loadConnectionFailedView(){
        headerTV.setText(R.string.DIA_connection_failed);
        captionTV.setText(R.string.DIA_connection_failed_details);
        progressIndicator.setVisibility(View.INVISIBLE);
        button.setVisibility(View.VISIBLE);
    }
}