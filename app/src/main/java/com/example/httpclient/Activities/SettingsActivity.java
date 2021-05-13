package com.example.httpclient.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.httpclient.Dialogs.AreYouSureDialog;
import com.example.httpclient.R;
import com.example.httpclient.SharedPreferences.WiFiSettings;
import com.example.httpclient.Utilities.SharedPreferencesEditor;

public class SettingsActivity extends AppCompatActivity {

    TextView ipAddressTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ipAddressTV = findViewById(R.id.DeviceIpAddress);

        WiFiSettings wiFiSettings = new WiFiSettings(getSharedPreferences(WiFiSettings.PREFERENCE_NAME, Context.MODE_PRIVATE));
        ipAddressTV.setText(wiFiSettings.getIP());
    }

    public void onDefaultSettingsButtonClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("After restoring default settings you will have to configure your device again")
                .setTitle("Warning");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteSharedPreferences("WIFI_PREF");
                Toast toast = Toast.makeText(getApplicationContext(), "Default Settings Restored", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void onDeviceConfigButtonClick(View view) {
        Intent intent = new Intent(this, DeviceIpActivity.class);
        startActivity(intent);
    }
}