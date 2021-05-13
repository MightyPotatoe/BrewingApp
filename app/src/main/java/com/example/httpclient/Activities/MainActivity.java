package com.example.httpclient.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.httpclient.Observer.Observer;
import com.example.httpclient.R;
import com.example.httpclient.Threads.MeasureThread;
import com.example.httpclient.services.SampleService;


public class MainActivity extends AppCompatActivity implements Observer {

    TextView textView;
    MeasureThread measureThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        measureThread = MeasureThread.getInstance(this, "192.168.50.61");
        measureThread.attach(this);
        measureThread.start();
    }

    public void onClick(View view) {
        measureThread.stopThread();
        Intent intent = new Intent(this, DeviceIpActivity.class);
        startActivity(intent);
    }

    public void startService(View view) {
        startService(new Intent(this, SampleService.class));
    }

    public void stopService(View view) {
        stopService(new Intent(this, SampleService.class));
    }

    @Override
    public void update(String param) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    textView.setText(""+param);
                }
                catch (NumberFormatException e){
                    textView.setText("Nie można połączyć z urządzeniem");
                }
            }
        });
    }

}