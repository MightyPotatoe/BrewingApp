package com.example.httpclient.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.httpclient.Activities.BrewingActivity;
import com.example.httpclient.Observer.Observer;
import com.example.httpclient.R;
import com.example.httpclient.Threads.MeasureThread;
import com.example.httpclient.Utilities.MyParser;
import com.example.httpclient.Utilities.SharedPreferencesEditor;
import com.example.httpclient.nototifications.MyNotificationBuilder;



public class BrewingService extends Service implements Observer{

    //Media Plater
    private MediaPlayer player = new MediaPlayer();
    //MeasureThread
    MeasureThread measureThread;
    //SharedPreferences
    SharedPreferences wiFiSharedPreferences;
    SharedPreferencesEditor brewingSharedPreferences;
    //Desired Temperature
    int desiredTemp;
    //Countdown Timer
    CountDownTimer countDownTimer;

    //Flags for service operation
    boolean disconnectedNotificationSend = false;
    boolean timerWorking = false;
    boolean tempMismatchNotificationSend = false;
    boolean newProcess;
    int temperatureMismatchCounter = 0;
    int correctTempCounter = 0;


    public static String TIMER_ACTION = "TIMER_ACTION";
    public static String BROADCAST_TIMER_VALUE = "TIMER_VALUE";
    public static String BROADCAST_UPDATE_VALUE = "UPDATE_VALUE";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Connecting to sharedPreferences
        wiFiSharedPreferences = getSharedPreferences("WIFI_PREF", Context.MODE_PRIVATE);
        brewingSharedPreferences = new SharedPreferencesEditor(getSharedPreferences("BREWING_SETTINGS", Context.MODE_PRIVATE));
        //Connecting to measuring Thread and starting it
        String ip = wiFiSharedPreferences.getString("IP", "0.0.0.0");
        measureThread = MeasureThread.getInstance(getApplicationContext(), ip);
        measureThread.attach(this);
        measureThread.start();
        //Initializing MediaPlayer
        player = MediaPlayer.create(this, R.raw.alarm);
        player.setLooping(true);
        Toast.makeText(this, "Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Getting data passed by INTENT
        desiredTemp = intent.getIntExtra("TEMP", 0);
        boolean forcedStart = intent.getBooleanExtra("FORCE_TIMER", false);
        int currentStepTime = intent.getIntExtra("TIME", 0);
        tempMismatchNotificationSend = intent.getBooleanExtra("ALARM_DISMISSED", false);
        newProcess = intent.getBooleanExtra("NEW_PROCESS", true);

        //Creating countdown Timer
        initializeCountDownTimer(currentStepTime, forcedStart);

        //Forcing timer to start if needed
        if(forcedStart){
            countDownTimer.start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stopping player
        player.stop();
        // Stopping timer
        countDownTimer.cancel();
    }

    public void onFinishMethod(){
            // This will play the ringtone continuously until we stop the service.
            player.setLooping(true);
            // It will start the player
            player.start();
            //Sending notification
            buildNotification("Process Finished!", "Your brewing step is complete");
    }

    //--------On Measure Received------------------
    @Override
    public void update(String param) {
        Log.v("TAG","Counter: " + temperatureMismatchCounter);
        //If timer is working and device is disconnected
        if(timerWorking && (param.equals("Disconnected") || param.isEmpty())){
            //If notification wasn't previously send
            if(!disconnectedNotificationSend){
                //Sending one-time notification
                disconnectedNotificationSend = true;
                buildNotification("Device is disconnected", "Check your device connection");
            }
        }
        else{ //    <-- if device is connected
            //Allow for sending notification ig connection is lost
            disconnectedNotificationSend = false;
            //Parsing double form String
            double currentTemp = MyParser.parseDouble(param);
            //If timer is not working
            if(!timerWorking){
               waitForTimerToStart(currentTemp);
            }
            else { //    <-- if timer is working check if temperature is correct
                checkTemperatureBias(currentTemp);
                sendTempNotificationIfNeeded();
            }
        }
    }


    public void sendTempNotificationIfNeeded(){
        //If checkTemp counter is too high
        if(temperatureMismatchCounter > 10){
            //if temp notifications are enabled
            if(!tempMismatchNotificationSend){
                tempMismatchNotificationSend = true;
                buildNotification("Attention Required", "Your temperature is no longer correct.");
                sendBroadcast(BROADCAST_UPDATE_VALUE, "WARNING");
                player.start();
            }
        }

    }


    public void checkTemperatureBias(double currentTemp){
        //If difference in temperature is greater than 2
        if(Math.abs(desiredTemp - currentTemp) > 2){
            if(temperatureMismatchCounter <= 10){
                temperatureMismatchCounter++;
            }
        }
        else {
            //temp Ok reset counter and turn on notifications
            temperatureMismatchCounter = 0;
            tempMismatchNotificationSend = false;
        }
    }

    public void waitForTimerToStart(double currentTemp){
        //If temp is +-2C from target
        if(Math.abs(desiredTemp - currentTemp) < 2){
            if(correctTempCounter <= 10){
                correctTempCounter++;
            }
            else{
                startTimer();
                sendBroadcast(BROADCAST_UPDATE_VALUE, "START");
                correctTempCounter = 0;
            }
        }
        //Else reset the counter
        else {
            correctTempCounter = 0;
        }
    }

    //----------------------------------------NOTIFICATIONS--------------------------------------------

    public void buildNotification(String title, String content){
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, BrewingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        MyNotificationBuilder builder = new MyNotificationBuilder(this, pendingIntent, notificationManager);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon( R.drawable.beer_hop);
        builder.setLargeIcon(getResources(), R.drawable.beer_hop);
        builder.build();
    }


    //--------------------------------BROADCASTING---------------------------
    public void sendBroadcast(String broadcastName, int value){
        Intent sendLevel = new Intent();
        sendLevel.setAction(TIMER_ACTION);
        sendLevel.putExtra( broadcastName, value);
        sendBroadcast(sendLevel);
    }

    public void sendBroadcast(String broadcastName, String value){
        Intent sendLevel = new Intent();
        sendLevel.setAction(TIMER_ACTION);
        sendLevel.putExtra( broadcastName, value);
        sendBroadcast(sendLevel);
    }

    public void sendBroadcast(String broadcastName, boolean value){
        Intent sendLevel = new Intent();
        sendLevel.setAction(TIMER_ACTION);
        sendLevel.putExtra( broadcastName, value);
        sendBroadcast(sendLevel);
    }


    //----------------COUNTDOWN TIMER-----------------------------------------------
    //---Definition
    public void initializeCountDownTimer(int currentStepTime, boolean forcedStart){
        int timerValue = currentStepTime*60*1000;
        if(forcedStart && !newProcess){
            timerValue = brewingSharedPreferences.getInt(SharedPreferencesEditor.TIME_REMAIN, 0)*1000;
        }
        countDownTimer = new CountDownTimer(timerValue, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsToFinish = (int) millisUntilFinished / 1000;
                onTimerTick(secondsToFinish);
            }

            public void onFinish() {
                onFinishMethod();
            }
        };
    }
    //----Starting timer
    public void startTimer(){
        countDownTimer.start();
        timerWorking = true;
    }

    //---OnTimerTickMethod
    public void onTimerTick(int timeLeft) {
        //Updating time remain
        brewingSharedPreferences.putInt(SharedPreferencesEditor.TIME_REMAIN, timeLeft);
        //Sending broadcast
        sendBroadcast(BROADCAST_TIMER_VALUE, timeLeft);
    }
}