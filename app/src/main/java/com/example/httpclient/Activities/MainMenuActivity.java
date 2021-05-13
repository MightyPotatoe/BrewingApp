package com.example.httpclient.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.httpclient.Observer.Observer;
import com.example.httpclient.R;
import com.example.httpclient.SharedPreferences.WiFiSettings;
import com.example.httpclient.Threads.DeviceStatusThread;
import com.example.httpclient.services.HTTPController;

public class MainMenuActivity extends AppCompatActivity implements Observer {


    private DeviceStatusThread deviceStatusThread;

    private ImageView deviceControlIconIV;
    private TextView deviceStatusTV;
    private TextView infoTV;
    private Button configureNowButton;
    private Button startBrewingButton;

    WiFiSettings wiFiSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        
        //----Initialize views----
        //device control view
        deviceStatusTV = findViewById(R.id.deviceStatusTV);
        deviceControlIconIV = findViewById(R.id.deviceControlIconIV);

        infoTV = findViewById(R.id.infoTV);
        configureNowButton = findViewById(R.id.configureNowButton);
        startBrewingButton = findViewById(R.id.startBrewingButton);
    
        //Connecting to Shared Preferences
        wiFiSettings = new WiFiSettings(getSharedPreferences(WiFiSettings.PREFERENCE_NAME, Context.MODE_PRIVATE));

        //Setting View
        updateView();
    }


    @Override
    protected void onPause() {
        Log.v("MMA", "onPause called!");
        super.onPause();
        if(deviceStatusThread != null){
            deviceStatusThread.stopThread();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }


    //-----StartBrewing Button---------
    public void onStartBrewingButtonClick(View view) {
        Intent intent = new Intent(this, BrewingSettingsActivity.class);
        startActivity(intent);
    }

    //-----ConfigureNow Button---------
    public void onConfigureNowButtonClick(View view) {
        Intent intent = new Intent(this, DeviceIpActivity.class);
        startActivity(intent);
    }

    //--------Settings button---------
    public void onSettingsButtonClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    //==============================================================================================
    //---------------------------   UI METHODS  ----------------------------------------------------
    //==============================================================================================

    //Updating View
    public void updateView(){
        Log.v("TAG", "Calling UpdateView()");
        //Starting thread
        //Subscribing to DeviceStatus Thread
        deviceStatusThread = DeviceStatusThread.getInstance(this, wiFiSettings.getIP());
        deviceStatusThread.attach(this);
        deviceStatusThread.start();
        //Updating device control view
        updateDeviceControlView(
                getResources().getString(R.string.MMA_CONNECTING),
                R.drawable.temperature_icon_yellow);
        //Updating Info View
        infoTV.setText(getResources().getString(R.string.MMA_CHECKING_DEVICE_STATUS));
        configureNowButton.setVisibility(View.INVISIBLE);
    }

    //Updating Device Control View
    public void updateDeviceControlView(String deviceStatusText, int deviceStatusIconID){
        deviceStatusTV.setText(deviceStatusText);
        deviceControlIconIV.setImageResource(deviceStatusIconID);
    }


    //==============================================================================================
    //-------------------- Receiving update from DeviceStatusThread --------------------------------
    //==============================================================================================
    @Override
    public void update(String param) {
        if(param.equals(HTTPController.ERROR_RESPONSE)){
            //Set connection control values
            updateDeviceControlView(
                    getResources().getString(R.string.MMA_DISCONNECTED),
                    R.drawable.temperature_icon_red
            );
            //Set info message view
            infoTV.setText(getResources().getString(R.string.MMA_DEVICE_DISCONNECTED_INFO));
            configureNowButton.setVisibility(View.VISIBLE);
        }
        else{
            deviceStatusTV.setText(getResources().getString(R.string.MMA_CONNECTED));
            infoTV.setText(getResources().getString(R.string.MMA_START_BREWING));
            configureNowButton.setVisibility(View.INVISIBLE);
        }
        startBrewingButton.setEnabled(true);
    }
}