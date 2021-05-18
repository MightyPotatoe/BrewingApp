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
import com.example.httpclient.Utilities.IpDetector;
import com.example.httpclient.services.HTTPController;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class DeviceSettingActivity extends AppCompatActivity implements HTTPController.OnHttpResponseListener {

    private HTTPController httpController;
    private TextInputEditText ssidET;
    private TextInputEditText passwordET;
    private TextView errorTV;
    private CircularProgressIndicator progressIndicator;
    private SharedPreferences sharedPreferences;
    private TextView ssidSendTV;
    private TextView passwordSendTV;
    private Button button;
    boolean connectionInProccess;
    boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("WIFI_PREF", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("IP", null);
        httpController = new HTTPController(this, ip);
        httpController.setHttpResponseListener(this);

        setContentView(R.layout.activity_device_setting);
        ssidET = findViewById(R.id.ssid_TE);
        passwordET = findViewById(R.id.password_TE);
        progressIndicator = findViewById(R.id.setWifiProgress);
        errorTV = findViewById(R.id.wifi_error_TV);
        ssidSendTV = findViewById(R.id.sendSSID);
        passwordSendTV = findViewById(R.id.sendPassword);
        button = findViewById(R.id.setBUtton);

        sharedPreferences = getSharedPreferences("WIFI_PREF", Context.MODE_PRIVATE);

        //default UI
        progressIndicator.setVisibility(View.INVISIBLE);
        errorTV.setVisibility(View.INVISIBLE);
        ssidSendTV.setVisibility(View.INVISIBLE);
        passwordSendTV.setVisibility(View.INVISIBLE);
    }


    public void onSetButtonClick(View view) {
        String SSID = ssidET.getText().toString();

        if(!SSID.isEmpty()){
            ui_loading();
            httpController.sendRequest("/setSSID/" + SSID);
        }
//        else if(!connected){
        else {
            ui_showErrorMessage("SSID field is required!", R.color.red);
        }
    }


    @Override
    public void onResponseReceived(String response){

        //----SSID SET CORRECTLY
        if(response.contains("SSID SET:")){
            on_SSID_SET_Response();
        }
        //----PASSWORD SET CORRECTLY
        else if(response.contains("PASS SET:")){
            on_PASSWORD_SET_Response();
        }
        //----DEVICE CONNECTED TO NEW WIFI NETWORK
        else if(response.contains("Connected:")){
            on_WIFI_CONNECTED_Response(response);
        }
        //-------STATUS OF THE DEVICE
        else if(response.contains("STATUS:")){
            on_STATUS_Response(response);
        }
        //-------RECHECK STATUS IF RECIEVED ANY OTHER RESPONSE WHILE CONNECTION IN PROCCESS
        else if(connectionInProccess){
            recheckStatus();
        }
        //------SHOW ERROR IN OTHER CASES
        else{
            onErrorDetected(response);
        }
    }


    //----------------------------------------------------------------------------------------------
    private void on_SSID_SET_Response(){
        ssidSendTV.setVisibility(View.VISIBLE);                                     //Showing label informing on successful transaction
        httpController.sendRequest("/setPASS/" + passwordET.getText().toString());  //Sending request to update PASSWORD on device
    }

    //----------------------------------------------------------------------------------------------
    int retry = 0;
    private void on_PASSWORD_SET_Response(){
        passwordSendTV.setVisibility(View.VISIBLE);     //Showing label informing on successful transaction
        connectionInProccess = true;                    //Setting connectionInProccess flag
        retry = 0;                                      //Resetting retry counter
        httpController.sendRequest("/UPDATE_WIFI");     //Sending UPDATE_WIFI request to device
    }

    //----------------------------------------------------------------------------------------------
    private void on_WIFI_CONNECTED_Response(String response){
        //parsing status
        String localeIp = response.replace("Connected:", "");
        //Updating view
        ui_showErrorMessage(response, R.color.green);
        //Storing localIp in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("IP", IpDetector.extractIP(localeIp));
        editor.commit();
        //updating connection flags
        connected = true;
        connectionInProccess = false;
        goToMainMenu();
    }

    //----------------------------------------------------------------------------------------------
    private void on_STATUS_Response(String response){
    //Response is STATUS: + localIp. localIp is empty if device is not connected to network

        //--Parsing response
        response = response.replace("STATUS:","");

        //If response does not contains IP number and connection is not in progress (Device disconnected)
        if(!IpDetector.detect(response) && !response.contains("CONNECTING")){
            connectionInProccess = false;
            //UI Settings
            String errorMessage = "Connection timeout! Make sure you have entered valid credentials";
            ui_showErrorMessage(errorMessage, R.color.red);
            ui_inputsEnabled(true);
        }
        //If reponse conatains IP address
        else{
            on_WIFI_CONNECTED_Response(response);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void recheckStatus(){
        retry++;
        httpController.sendRequest("/CHECK_STATUS");
        if(retry > 5){
            connectionInProccess = false;
        }
    }

    //----------------------------------------------------------------------------------------------
    private void onErrorDetected(String response){
        String errorMessage = "Error: " + response;
        ui_showErrorMessage(errorMessage, R.color.red);
        ui_inputsEnabled(true);
    }

    //----------------------------------------------------------------------------------------------
    //**********************************************************************************************
    //----------------------------------------------------------------------------------------------
    private void ui_showErrorMessage(String message, int colorID){
        errorTV.setVisibility(View.VISIBLE);
        errorTV.setText(message);
        errorTV.setTextColor(getColor(colorID));
        progressIndicator.setVisibility(View.INVISIBLE);
        button.setEnabled(true);
        ssidSendTV.setVisibility(View.INVISIBLE);
        passwordSendTV.setVisibility(View.INVISIBLE);
    }

    private void ui_inputsEnabled(boolean isEnabled){
        ssidET.setEnabled(isEnabled);
        passwordET.setEnabled(isEnabled);
    }

    private void ui_loading(){
        ui_inputsEnabled(false);
        button.setEnabled(false);
        errorTV.setVisibility(View.INVISIBLE);
        progressIndicator.setVisibility(View.VISIBLE);
    }

    private void goToMainMenu(){
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}