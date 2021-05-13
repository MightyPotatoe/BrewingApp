package com.example.httpclient.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.httpclient.R;
import com.example.httpclient.Utilities.IpChecker;
import com.example.httpclient.services.HTTPController;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class DeviceIpActivity extends AppCompatActivity implements HTTPController.OnHttpResponseListener {

    TextInputEditText ipBox;
    TextView errorTV;
    CircularProgressIndicator progressIndicator;
    HTTPController httpController;
    SharedPreferences sharedPreferences;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_ip);
        button = findViewById(R.id.connectBUtton);
        ipBox = findViewById(R.id.IP_TE);
        errorTV = findViewById(R.id.error_TV);
        progressIndicator = findViewById(R.id.progressCircleDeterminate);
        sharedPreferences = getSharedPreferences("WIFI_PREF", Context.MODE_PRIVATE);

        //--Default State
        ui_inputsEnabled(true);
        progressIndicator.setVisibility(View.INVISIBLE);
        errorTV.setVisibility(View.INVISIBLE);

    }

    public void onConnectButtonClick(View view) {

        progressIndicator.setVisibility(View.VISIBLE);
        button.setEnabled(false);

        String ipAddress = ipBox.getText().toString();
        if(!IpChecker.isIp(ipAddress)){
            ui_showError("Please provide correct IP address", R.color.red);
        }
        else {
            //updating view
            ui_loading();
            //sending request to device
            httpController = new HTTPController(this, ipAddress);
            httpController.setHttpResponseListener(this);
            httpController.sendRequest("/hello");
        }

    }

    @Override
    public void onResponseReceived(String response) {
        if(response.contains("HELLO")){
            //updating view
            ui_showError("Connected!", R.color.green);
            ipBox.setEnabled(false);
            //storing device ID in sharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("IP", ipBox.getText().toString());
            editor.commit();
            //starting new Activity
            Intent intent = new Intent(this, DeviceSettingActivity.class);
            startActivity(intent);
        }
        else {
            ui_showError(response, R.color.red);
        }
    }

    private void ui_showError(String errorMessage, int colorId){
        progressIndicator.setVisibility(View.INVISIBLE);
        errorTV.setVisibility(View.VISIBLE);
        errorTV.setTextColor(getColor(colorId));
        errorTV.setText(errorMessage);
        ui_inputsEnabled(true);
    }

    private void ui_loading(){
        errorTV.setVisibility(View.INVISIBLE);
        progressIndicator.setVisibility(View.VISIBLE);
        ui_inputsEnabled(false);
    }

    private void ui_inputsEnabled(boolean isEnabled){
        ipBox.setEnabled(isEnabled);
        button.setEnabled(isEnabled);
    }
}