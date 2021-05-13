package com.example.httpclient.nototifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import com.example.httpclient.R;

public class MyNotificationBuilder{

    NotificationManager notificationManager;
    Notification.Builder builder;

    public MyNotificationBuilder(Context context, PendingIntent pendingIntent, NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Powiadomienia z aplikacji BrewIt";
            String channelId = "i.apps.notifications";
            NotificationChannel notificationChannel = new NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            builder = new Notification.Builder(context, channelId)
                    .setContentIntent(pendingIntent);
        }
        else {
            builder = new Notification.Builder(context)
                    .setContentIntent(pendingIntent);
        }
    }


    public void setSmallIcon(int imageId){
        builder.setSmallIcon(R.drawable.ic_launcher_background);
    }

    public void setLargeIcon(Resources resources, int imageId){
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, imageId));
    }

    public void setContentText(String message){
        builder.setContentText(message);
    }

    public void setContentTitle(String title){
        builder.setContentTitle(title);
    }

    public void build(){
        notificationManager.notify(1234, builder.build());
    }
}
