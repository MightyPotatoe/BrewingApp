package com.example.httpclient.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.httpclient.Adapters.BrewingStepsAdapter;
import com.example.httpclient.DataBase.AppDatabase;
import com.example.httpclient.DataBase.Dictionary;
import com.example.httpclient.Observer.Observer;
import com.example.httpclient.R;
import com.example.httpclient.Threads.MeasureThread;
import com.example.httpclient.Utilities.SharedPreferencesEditor;
import com.example.httpclient.services.BrewingService;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class BrewingActivity extends AppCompatActivity implements Observer {

    private final int COLOR_BAR_NONE = 0;
    private final int COLOR_BAR_DARK_BLUE = 1;
    private final int COLOR_BAR_LIGHT_BLUE = 2;
    private final int COLOR_BAR_GREEN = 3;
    private final int COLOR_BAR_ORANGE = 4;
    private final int COLOR_BAR_RED = 5;

    //Shared Preferences
    SharedPreferences wiFiSharedPreferences;
    SharedPreferencesEditor brewingSharedPreferencesEditor;

    //Adapter for viewing next steps
    BrewingStepsAdapter brewingStepsAdapter;

    //Ui Elements
    TextView tempTV;
    ImageView blueBar;
    ImageView lightBlueBar;
    ImageView greenBar;
    ImageView orangeBar;
    ImageView redBar;
    TextView currentStepTempTV;
    TextView currentStepTimeTv;
    TextView remainingTimeTV;
    TextView statusTV;
    TextView statusHintTV;
    TextView nextStepLabel;
    CircularProgressIndicator timeProgressBar;
    MaterialCheckBox doNotUseThermometerCheckbox;

    //Variable for device response
    String deviceResponse = "Disconnected";
    //Brewing Service
    Intent brewingServiceIntent;

    //PowerManager
    PowerManager mgr;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewing);

        final AppDatabase db = AppDatabase.getInstance(this);
        db.setApplicationStatus(Dictionary.STATUS_PENDING);

        brewingServiceIntent = new Intent(this, BrewingService.class);

        //Connecting to SharedPreferences
        wiFiSharedPreferences = getSharedPreferences("WIFI_PREF", Context.MODE_PRIVATE);

        //Initializing Brewing Steps Adapter
        brewingStepsAdapter = new BrewingStepsAdapter(this);

        //Registering Broadcast Receiver for BrewingServiceMessages
        registerReceiver(broadcastReceiver, new IntentFilter(BrewingService.TIMER_ACTION)); //<----Register

        //Temperature TextView
        tempTV = findViewById(R.id.tempValueTV);

        //Getting device Ip and Current app status
        String ip = wiFiSharedPreferences.getString("IP", "0.0.0.0");
        //Starting measure thread
        MeasureThread measureThread = MeasureThread.getInstance(this, ip);
        measureThread.attach(this);
        measureThread.start();

        //Temperature section
        blueBar = findViewById(R.id.blueBar);
        lightBlueBar = findViewById(R.id.lightBlueBar);
        greenBar = findViewById(R.id.greenbar);
        orangeBar = findViewById(R.id.orangeBar);
        redBar = findViewById(R.id.redBar);

        //Current Step Section
        currentStepTempTV = findViewById(R.id.currentStepTemp);
        currentStepTimeTv = findViewById(R.id.currentStepTimeTV);

        //TimeRemaining Section
        remainingTimeTV = findViewById(R.id.remainingTimeTV);
        timeProgressBar = findViewById(R.id.progressIndicator);

        //Next Steps Section
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(brewingStepsAdapter);

        //Do not use thermometer checkbox
        doNotUseThermometerCheckbox = findViewById(R.id.doNotUseThermometerCheckbox);
        doNotUseThermometerCheckbox.setEnabled(false);

        //statusCard
        statusTV = findViewById(R.id.statusTV);
        statusHintTV = findViewById(R.id.statusHintTV);

        //NextStepLabel
        nextStepLabel = findViewById(R.id.nextStepLabel);

        //Setting up power management
        mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:mywakelog");
        wakeLock.acquire(120*60*1000L /*120 minutes*/);


        //Calling default settings
        callDefaultSettings();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        registerReceiver(broadcastReceiver, new IntentFilter(BrewingService.TIMER_ACTION)); //<----Register broadcast receiver
//        //Updating view to current status
//        manageActivityStatus();
//    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver); //<-- Unregister broadcast receiver to avoid memory leak
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        callDefaultSettings();
        wakeLock.release();
    }

    /**
     * Updating temperature section on device response received
     * @param response - response from device
     */
    @Override
    public void update(String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int softBias = 2;
                int strongBias = 5;
                deviceResponse = response;
                //Device not connected
                if(response.equals("Disconnected") || response.equals("-")){
                    tempTV.setTextColor(getColor(R.color.red));
                    setProgressOnColorBar(0);
                }
                else{
                    tempTV.setTextColor(getColor(R.color.black));
                    double currentTemp = Double.parseDouble(response.replaceAll("[^0-9.]",""));
                    int desiredTemp = brewingStepsAdapter.getCurrentStepTemperature();
                    double deltaT = currentTemp - desiredTemp;
                    if(deltaT >= strongBias ) {
                        setProgressOnColorBar(5);
                    }
                    else if(deltaT > softBias){
                        setProgressOnColorBar(4);
                    }
                    else if(deltaT <= -strongBias){
                        setProgressOnColorBar(1);
                    }
                    else if(deltaT < -softBias){
                        setProgressOnColorBar(2);
                    }
                    else{
                        setProgressOnColorBar(3);
                    }
                }
                tempTV.setText(response);
                updateView();
            }
        });
    }



    public void manageActivityStatus(){
        //Connecting to DB
        final AppDatabase db = AppDatabase.getInstance(this);
        String status = db.getApplicationStatus();
        switch (status){
            case Dictionary.STATUS_PENDING:
                //resetting timer
                updateTimer(-1);
                //starting brewing service
                startBrewingService();
                break;
        }
        updateView();


    }


    //---------------------UI UPDATING------------------------

    private void updateView(){
        final AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        String status = db.getApplicationStatus();
        switch (status){
            case Dictionary.STATUS_PENDING:
                updateView_Pending();
                break;
            case Dictionary.STATUS_RUNNING:
                updateView_Running();
        }
    }

    /**
     * Updating view to Pending Status
     */
    @SuppressLint("SetTextI18n")
    private void updateView_Pending(){
        //Setting up Information Section
        statusTV.setText(getResources().getString(R.string.BA_WaitingForTemperature));
        if(deviceResponse.equals(MeasureThread.DISCONNECTED)){
            statusHintTV.setText(getResources().getString(R.string.BA_DeviceDisconnected));
        }
        else{
            statusHintTV.setText(getResources().getString(R.string.BA_WaitingForTemperatureHint));
        }
        //Setting up current step section
        currentStepTempTV.setText(brewingStepsAdapter.getCurrentStepTemperature() + "\u2103");
        currentStepTimeTv.setText(brewingStepsAdapter.getCurrentStepTime() + " min");
    }

    /**
     * Updating view to Running Status
     */
    private void updateView_Running(){
        statusTV.setText(getResources().getString(R.string.BA_BrewingInProgress));
        statusHintTV.setText(getResources().getString(R.string.BA_BrewingInProgressHint));
    }




//    public void updateStatusCard(String statusText, String statusHintText){
//        statusTV.setText(statusText);
//        statusHintTV.setText(statusHintText);
//    }


    /**
     * Setting timer value and progress bar
     * @param timeRemain - remaining time (max. time is a time from current brewing step)
     *                   -1 => setting timeRemain to be equal to max.
     */
    @SuppressLint("SetTextI18n")
    public void updateTimer(int timeRemain){
        int currentStepTime = brewingStepsAdapter.getCurrentStepTime()*60;
        //if 0 i passed as timeRamain get default step time
        if(timeRemain == -1){
            timeRemain = currentStepTime;
        }
        int min = timeRemain/60;
        int sec = timeRemain%60;
        remainingTimeTV.setText(min + " min " + sec + " sec");
        timeProgressBar.setMax(currentStepTime);
        timeProgressBar.setProgress(timeRemain);
    }



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resetting brewing steps adapter to show all brewing steps
     * Setting current step to take next one in queue (1st)
     * Updating timer to be equal to current brewing step
     * Updating view to match current application status
     * Stopping brewingService if running
     */
    @SuppressLint("SetTextI18n")
    public void callDefaultSettings(){
        //Resetting next steps section
        brewingStepsAdapter.reset();
        //Setting current step details
        currentStepTempTV.setText(brewingStepsAdapter.getCurrentStepTemperature() + "\u2103");
        currentStepTimeTv.setText(brewingStepsAdapter.getCurrentStepTime() + " min");
        //Resetting timer
        updateTimer(-1);
        //Load settings for current status and stp BrewingService if running
        manageActivityStatus();
        if(isMyServiceRunning(BrewingService.class)){
            stopService(brewingServiceIntent);
        }
    }

    /**
     * Enabling Temperature Color Bar
     * @param barToShow
     *  COLOR_BAR_DARK_BLUE
     *  COLOR_BAR_LIGHT_BLUE
     *  COLOR_BAR_GREEN
     *  COLOR_BAR_ORANGE
     *  COLOR_BAR_RED
     */
    public void setProgressOnColorBar(int barToShow){

        blueBar.setVisibility(View.INVISIBLE);
        lightBlueBar.setVisibility(View.INVISIBLE);
        greenBar.setVisibility(View.INVISIBLE);
        orangeBar.setVisibility(View.INVISIBLE);
        redBar.setVisibility(View.INVISIBLE);

        switch (barToShow){
            case COLOR_BAR_DARK_BLUE:
                blueBar.setVisibility(View.VISIBLE);
                break;
            case COLOR_BAR_LIGHT_BLUE:
                lightBlueBar.setVisibility(View.VISIBLE);
                break;
            case COLOR_BAR_GREEN:
                greenBar.setVisibility(View.VISIBLE);
                break;
            case COLOR_BAR_ORANGE:
                orangeBar.setVisibility(View.VISIBLE);
                break;
            case COLOR_BAR_RED:
                redBar.setVisibility(View.VISIBLE);
                break;
        }
    }


    public void startBrewingService(){
        if(!isMyServiceRunning(BrewingService.class)){
            Log.v("DEBUG:", "Calling startBrewingService()");
            brewingServiceIntent.putExtra(BrewingService.TIME, brewingStepsAdapter.getCurrentStepTime());
            brewingServiceIntent.putExtra(BrewingService.TEMP,  brewingStepsAdapter.getCurrentStepTemperature());
            this.startService(brewingServiceIntent);
        }
    }


    /**
     * On receive broadcast from brewingService
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Connect do DB
            final AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            //Get UPDATE_VALUE from broadcast and process it
            String updateValue = intent.getStringExtra(BrewingService.BROADCAST_UPDATE_VALUE);
            if(updateValue != null){
                if(updateValue.equals(BrewingService.BROADCAST_STARTED)){
                    db.setApplicationStatus(Dictionary.STATUS_RUNNING);
                    manageActivityStatus();
                }
                else if(updateValue.equals("STATUS_WARNING")){
                    brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, "STATUS_WARNING");
                    manageActivityStatus();
                }
            }
            SharedPreferencesEditor brewingSharedPreferencesEditor = new SharedPreferencesEditor(getSharedPreferences("BREWING_SETTINGS", Context.MODE_PRIVATE));
            int timeRemain = brewingSharedPreferencesEditor.getInt("TIME_REMAIN", -1);
            updateTimer(timeRemain);
            if(timeRemain == 0){
                if(brewingStepsAdapter.hasNextStep()){
                    String statusToUpdate = "STEP FINISHED";
                    brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, statusToUpdate);
                    manageActivityStatus();
                }
                else{
                    String statusToUpdate = "STEP FINISHED";
                    brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, statusToUpdate);
                    manageActivityStatus();
                }
            }

            //get BROADCAST_TIMER_VALUE from broadcast and process it
            int timeLeft = intent.getIntExtra(BrewingService.BROADCAST_TIMER_VALUE, -1);
            if(timeLeft != -1){
                updateTimer(timeLeft);
            }

        }
    };


}