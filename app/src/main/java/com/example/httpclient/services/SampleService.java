package com.example.httpclient.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.example.httpclient.Activities.MainActivity;
import com.example.httpclient.Observer.Observer;
import com.example.httpclient.R;
import com.example.httpclient.Threads.MeasureThread;
import com.example.httpclient.nototifications.MyNotificationBuilder;

public class SampleService extends Service implements Observer {

    private MediaPlayer player;
    MeasureThread measureThread;

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }


    @Override
    public void onCreate() {
        Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
        measureThread = MeasureThread.getInstance(getApplicationContext(), "192.168.50.61");
        player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        measureThread.attach(this);
        measureThread.start();
        Toast.makeText(this, "Service Started!", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stopping the player when service is destroyed
        player.stop();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void update(String param) {
        if (Double.parseDouble(param) - 30 > 0 && !player.isPlaying()) {
            // This will play the ringtone continuously until we stop the service.
            player.setLooping(true);
            // It will start the player
            player.start();

            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            MyNotificationBuilder builder = new MyNotificationBuilder(this, pendingIntent, notificationManager);
            builder.setContentTitle("Tytuł Powiadomienia");
            builder.setContentText( "Treść powiadomienia znajduje się tutaj");
            builder.setSmallIcon( R.drawable.ic_launcher_background);
            builder.setLargeIcon(getResources(), R.drawable.ic_launcher_background);
            builder.build();
        }
    }
}
