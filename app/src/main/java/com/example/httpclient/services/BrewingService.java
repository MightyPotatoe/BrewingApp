package com.example.httpclient.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.httpclient.Activities.BrewingActivity;
import com.example.httpclient.Observer.Observer;
import com.example.httpclient.R;
import com.example.httpclient.SharedPreferences.BrewingSettings;
import com.example.httpclient.SharedPreferences.WiFiSettings;
import com.example.httpclient.Threads.MeasureThread;
import com.example.httpclient.Utilities.MyParser;
import com.example.httpclient.nototifications.MyNotificationBuilder;


public class BrewingService extends Service implements Observer {

    public static final String TIME = "TIME";
    public static final String TEMP = "TEMP";
    public static final String ALARM_DISMISSED = "TIME";

    public static final String BROADCAST_STARTED = "START";

    //---MEDIA PLAYER FOR SOUND NOTIFICATIONS----
    private MediaPlayer player = new MediaPlayer();

    //---TIMER----
    private CountDownTimer countDownTimer;

    //Flags for service operation
    private boolean disconnectedNotificationSend;
    private boolean timerWorking;
    private boolean tempMismatchNotificationSend;
    private int temperatureMismatchCounter;
    private int correctTempCounter;
    private int desiredTemperature;
    private boolean serviceRunning;

    public static String TIMER_ACTION = "TIMER_ACTION";
    public static String BROADCAST_TIMER_VALUE = "TIMER_VALUE";
    public static String BROADCAST_UPDATE_VALUE = "UPDATE_MESSAGE";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //-----------------------------SERVICE METHODS--------------------------------------------------
    @Override
    public void onCreate() {
        //Getting device IP
        WiFiSettings wiFiSettings = new WiFiSettings(getSharedPreferences(WiFiSettings.PREFERENCE_NAME, Context.MODE_PRIVATE));
        String ip = wiFiSettings.getIP();
        //Initializing MeasureThread
        MeasureThread measureThread = MeasureThread.getInstance(getApplicationContext(), ip);
        measureThread.attach(this);
        measureThread.start();
        //Initializing MediaPlayer
        player = MediaPlayer.create(this, R.raw.alarm);
        player.setLooping(true);
        Log.v("DEBUG:", "Brewing service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("DEBUG:", "Brewing service started");
        //Initialize flags
        disconnectedNotificationSend = false;
        timerWorking = false;
        tempMismatchNotificationSend = false;
        temperatureMismatchCounter = 0;
        correctTempCounter = 0;

        //Assigning data passed by intent
        desiredTemperature = intent.getIntExtra(TEMP, 0);
        tempMismatchNotificationSend = intent.getBooleanExtra(ALARM_DISMISSED, false);

        //Creating countdown Timer
        int currentStepTime = intent.getIntExtra(TIME, 0);
        initializeCountDownTimer(currentStepTime);


        serviceRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        player.stop();
        countDownTimer.cancel();
        serviceRunning = false;
        Log.v("DEBUG:", "Service Destroyed");
        super.onDestroy();
    }

    //----------------------------------------------------------------------------------------------

    public void onFinishMethod() {
        // This will play the ringtone continuously until we stop the service.
        player.setLooping(true);
        // It will start the player
        player.start();
        //Sending notification
        buildNotification("Process Finished!", "Your brewing step is complete");
    }

    /**
     * On measure received.
     * @param param - received measure
     */
    //--------On Measure Received------------------
    @Override
    public void update(String param) {
        Log.v("TAG", "Counter: " + temperatureMismatchCounter);
        //If timer is working and device is disconnected
        if (timerWorking && (param.equals(MeasureThread.DISCONNECTED) || param.isEmpty())) {
            //If notification wasn't previously send
            if (!disconnectedNotificationSend) {
                //Sending one-time notification
                disconnectedNotificationSend = true;
                buildNotification("Device is disconnected", "Check your device connection");
            }
        } else { //    <-- if device is connected
            //Allow for sending notification eg. connection is lost
            disconnectedNotificationSend = false;
            //Parsing double form String
            double currentTemp = MyParser.parseDouble(param);
            //If timer is not working
            if (!timerWorking) {
                waitForTimerToStart(currentTemp);
            } else { //    <-- if timer is working check if temperature is correct
                checkTemperatureBias(currentTemp);
                sendTempNotificationIfNeeded();
            }
        }
    }


    public void sendTempNotificationIfNeeded() {
        //If checkTemp counter is too high
        BrewingSettings brewingSettings = new BrewingSettings(getSharedPreferences(BrewingSettings.PREFERENCE_NAME, Context.MODE_PRIVATE));
        if (temperatureMismatchCounter > brewingSettings.getTempTolerance()) {
            //if temp notifications are enabled
            if (!tempMismatchNotificationSend) {
                tempMismatchNotificationSend = true;
                buildNotification("Attention Required", "Your temperature is no longer correct.");
                sendBroadcast(BROADCAST_UPDATE_VALUE, "WARNING");
                player.start();
            }
        }

    }


    public void checkTemperatureBias(double currentTemp) {
        BrewingSettings brewingSettings = new BrewingSettings(getSharedPreferences(BrewingSettings.PREFERENCE_NAME, Context.MODE_PRIVATE));
        if (Math.abs(desiredTemperature - currentTemp) > brewingSettings.getTempTolerance()) {
            if (temperatureMismatchCounter <= 30) {
                temperatureMismatchCounter++;
            }
        } else {
            //temp Ok reset counter and turn on notifications
            temperatureMismatchCounter = 0;
            tempMismatchNotificationSend = false;
        }
    }

    /**
     * check if temperature is in correct range from desired temp.
     * If 10 measures in row are within the range of desired temperature
     * startTimer and send broadcast informing about status change.
     *
     * @param currentTemp
     */
    public void waitForTimerToStart(double currentTemp) {
        if(serviceRunning) {
            //check if temperature is +- 2C from desired temp.
            BrewingSettings brewingSettings = new BrewingSettings(getSharedPreferences(BrewingSettings.PREFERENCE_NAME, Context.MODE_PRIVATE));
            if (Math.abs(desiredTemperature - currentTemp) < brewingSettings.getTempTolerance()) {
                if (correctTempCounter <= 10) {
                    correctTempCounter++;
                }
                //If 10 measures in row are within the range of desired temperature
                //startTimer and send broadcast informing about status change.
                else {
                    startTimer();
                    sendBroadcast(BROADCAST_UPDATE_VALUE, BROADCAST_STARTED);
                    buildNotification("Your brewing has started!", "Your brew reached correct temperature. Timer has started");
                    correctTempCounter = 0;
                }
            }
            //Else reset the counter
            else {
                correctTempCounter = 0;
            }
            Log.v("CORRECT_MEASURE_COUNTER", " " + correctTempCounter);
        }
    }

    //----------------------------------------NOTIFICATIONS--------------------------------------------

    public void buildNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, BrewingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        MyNotificationBuilder builder = new MyNotificationBuilder(this, pendingIntent, notificationManager);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.beer_hop);
        builder.setLargeIcon(getResources(), R.drawable.beer_hop);
        builder.build();
    }


    //--------------------------------BROADCASTING---------------------------
    public void sendBroadcast(String broadcastName, int value) {
        Intent sendLevel = new Intent();
        sendLevel.setAction(TIMER_ACTION);
        sendLevel.putExtra(broadcastName, value);
        sendBroadcast(sendLevel);
    }

    public void sendBroadcast(String broadcastName, String value) {
        Intent sendLevel = new Intent();
        sendLevel.setAction(TIMER_ACTION);
        sendLevel.putExtra(broadcastName, value);
        sendBroadcast(sendLevel);
    }

    public void sendBroadcast(String broadcastName, boolean value) {
        Intent sendLevel = new Intent();
        sendLevel.setAction(TIMER_ACTION);
        sendLevel.putExtra(broadcastName, value);
        sendBroadcast(sendLevel);
    }


    //----------------COUNTDOWN TIMER-----------------------------------------------
    //---Definition
    public void initializeCountDownTimer(int currentStepTime) {
        Log.v("DEBUG:", "Initializing countdown timer");
        int timerValue = currentStepTime * 60 * 1000;
        //Creating countdownTimer
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
    public void startTimer() {
        Log.v("DEBUG:", "Starting counter");
        countDownTimer.start();
        timerWorking = true;
    }

    //---OnTimerTickMethod
    public void onTimerTick(int timeLeft) {
        //Sending broadcast
        sendBroadcast(BROADCAST_TIMER_VALUE, timeLeft);
    }
}
