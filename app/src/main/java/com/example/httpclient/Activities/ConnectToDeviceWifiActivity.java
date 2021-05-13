package com.example.httpclient.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.httpclient.R;

import org.w3c.dom.Text;

public class ConnectToDeviceWifiActivity extends AppCompatActivity {

    String ssid;
    TextView infoTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_device_wifi);

        infoTV = findViewById(R.id.connectToWifiInfoTV);


    }


    public void onClick(View view) {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo connecion = wifiManager.getConnectionInfo();
        ssid = connecion.getSSID();
        if(ssid.equals("BrewIt_AP")){
            Intent intent = new Intent(this, DeviceIpActivity.class);
            startActivity(intent);
        }
        else{
            infoTV.setText("Please connect to device WiFi (BrewIt_AP).\n\nCurrently you are connected to: " + ssid);
        }
    }
}