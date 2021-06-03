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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.httpclient.Adapters.BrewingStepsAdapter;
import com.example.httpclient.Observer.Observer;
import com.example.httpclient.R;
import com.example.httpclient.Threads.MeasureThread;
import com.example.httpclient.Utilities.SharedPreferencesEditor;
import com.example.httpclient.services.BrewingService;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class BrewingActivity extends AppCompatActivity implements Observer {

    private final String STATUS_INACTIVE = "INACTIVE";
    private final String STATUS_ACTIVE = "ACTIVE";
    private final String STATUS_WARNING = "WARNING";

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

    PowerManager mgr;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewing);
        brewingServiceIntent = new Intent(this, BrewingService.class);

        //Connecting to SharedPreferences
        wiFiSharedPreferences = getSharedPreferences("WIFI_PREF", Context.MODE_PRIVATE);
        brewingSharedPreferencesEditor = new SharedPreferencesEditor(getSharedPreferences("BREWING_SETTINGS", Context.MODE_PRIVATE));

        //Initializing Brewing Steps Adapter
        brewingStepsAdapter = new BrewingStepsAdapter(this, brewingSharedPreferencesEditor);

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

        //Current Section
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

        callDefaultSettings();

        mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:mywakelog");
        wakeLock.acquire(120*60*1000L /*120 minutes*/);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(BrewingService.TIMER_ACTION)); //<----Register broadcast receiver
        //Updating view to current status
        updateStatus();
    }

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

    @Override
    public void update(String param) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int softBias = 2;
                int strongBias = 5;
                deviceResponse = param;
                //Device not connected
                if(param.equals("Disconnected") || param.equals("-")){
                    tempTV.setTextColor(getColor(R.color.red));
                    setProgressOnColorBar(0);
                }
                else{
                    tempTV.setTextColor(getColor(R.color.black));
                    double currentTemp = Double.parseDouble(param.replaceAll("[^0-9.]",""));
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
                tempTV.setText(param);
                updateStatus();
            }
        });
    }

    public void onActionButtonClick(View view) {
        String status = brewingSharedPreferencesEditor.getString(SharedPreferencesEditor.STATUS, STATUS_INACTIVE);
        switch (status){
            case STATUS_INACTIVE:
                status = "PENDING";
                updateStatus();
                if(!isMyServiceRunning(BrewingService.class)){
                    startBrewingService(false, false, true);
                }
                break;
            case "PENDING":
                status = STATUS_ACTIVE;
                updateStatus();
                if(!isMyServiceRunning(BrewingService.class)){
                    startBrewingService(true, false, true);
                }
                break;
            case "STEP FINISHED":
                status = "PENDING";
                brewingStepsAdapter.nextStep();
                if(brewingStepsAdapter.hasNextStep()){
                    nextStepLabel.setVisibility(View.INVISIBLE);
                }
                updateStatus();
                this.stopService(brewingServiceIntent);
                break;
            case "PROCESS FINISHED":
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                this.finish();
                break;
            case STATUS_WARNING:
                status = STATUS_ACTIVE;
                updateStatus();
                startBrewingService(true, true, false);
                break;
        }

        brewingSharedPreferencesEditor.putString("STATUS", status);
        brewingStepsAdapter.updateSteps();

    }




    public void updateStatus(){

        String statusText;
        String statusHint;
        String buttonText;

        String status = brewingSharedPreferencesEditor.getString(SharedPreferencesEditor.STATUS, STATUS_INACTIVE);
        switch (status){
            case STATUS_INACTIVE:
                statusText = "Waiting for process to start";
                statusHint = "When you are ready please click on 'Start' button to begin";
                buttonText = "Start";
                break;
            case "PENDING":
                brewingSharedPreferencesEditor.putInt(SharedPreferencesEditor.TIME_REMAIN, -1);
                statusText = ("Waiting for reaching correct temperature");
                if(deviceResponse.equals("Disconnected")){
                    statusHint = "Your device seems to be disconnected. Start the timer by clicking 'Start Timer' button, when  you are ready to start the timer";
                }
                else{
                    statusHint = "You will receive the notification and timer will start automatically when your wort reaches desired temperature. You can also start the timer by clicking on 'Start Timer' button";
                }
                buttonText = "Start Timer";
                updateCurrentStepCard();
                updateTimerCard(-1);
                break;
            case STATUS_ACTIVE:
                statusText =  "Brewing in " + brewingStepsAdapter.getCurrentStepTemperature() + "\u2103 for " + brewingStepsAdapter.getCurrentStepTime() + " min";
                statusHint = "...";
                buttonText = "Cancel Process";
                int timeRemain = brewingSharedPreferencesEditor.getInt("TIME_REMAIN", -1);
                if(timeRemain == 0){
                    if(brewingStepsAdapter.nextStep()){
                        updateTimerCard(timeRemain);
                        status = "STEP FINISHED";
                        brewingSharedPreferencesEditor.getString(SharedPreferencesEditor.STATUS, status);
                        updateStatus();
                    }
                }
                break;
            case "STEP FINISHED":
                statusText = "Step finished!";
                statusHint = "Click on 'Next Step' button to continue";
                buttonText = "Next Step";
                break;
            case "PROCESS FINISHED":
                statusText = "FINISHED!";
                statusHint = "Your brewing process is complete. Click oin 'Finish' button to return to main menu";
                buttonText = "Finish";
                break;
            default:
                statusText = "Error!";
                statusHint = "Something went wrong";
                buttonText = "X";
            case STATUS_WARNING:
                statusText =  "Brewing in " + brewingStepsAdapter.getCurrentStepTemperature() + "\u2103 for " + brewingStepsAdapter.getCurrentStepTime() + " min";
                statusHint = "Your wort requires your attention";
                buttonText = "Dismiss Alarm";
                int remainingTime = brewingSharedPreferencesEditor.getInt("TIME_REMAIN", -1);
                if(remainingTime == 0){
                    if(brewingStepsAdapter.nextStep()){
                        updateTimerCard(remainingTime);
                        status = "STEP FINISHED";
                        brewingSharedPreferencesEditor.getString(SharedPreferencesEditor.STATUS, status);
                        updateStatus();
                    }
                }
                break;

        }

        updateStatusCard(statusText, statusHint);
        updateButton(buttonText);
    }


    //---------------------UI UPDATING------------------------
    public void updateStatusCard(String statusText, String statusHintText){
        statusTV.setText(statusText);
        statusHintTV.setText(statusHintText);
    }

    public void updateButton(String text){
//        actionButton.setText(text);
    }

    public void updateCurrentStepCard(){
        currentStepTempTV.setText(brewingStepsAdapter.getCurrentStepTemperature() + "\u2103");
        currentStepTimeTv.setText(brewingStepsAdapter.getCurrentStepTime() + " min");
    }

    @SuppressLint("SetTextI18n")
    public void updateTimerCard(int timeRemain){
        int currentStepTime = brewingStepsAdapter.getCurrentStepTime()*60;
        //if 0 i passed as timeRamain get default step time
        if(timeRemain == -1){
            timeRemain = currentStepTime;
        }
        int min = timeRemain/60;
        int sec = timeRemain%60;
        remainingTimeTV.setText(min + " min " + sec + " sec");
        timeProgressBar.setMax(currentStepTime);
        timeProgressBar.setProgress(currentStepTime - timeRemain);
    }

    //-----------------------------------------------------

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String updateValue = intent.getStringExtra("UPDATE_VALUE");
            if(updateValue != null){
                if(updateValue.equals("START")){
                    brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, STATUS_ACTIVE);
                    updateStatus();
                }
                else if(updateValue.equals(STATUS_WARNING)){
                    brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, STATUS_WARNING);
                    updateStatus();
                }
            }

            int timeRemain = brewingSharedPreferencesEditor.getInt("TIME_REMAIN", -1);
            updateTimerCard(timeRemain);
            if(timeRemain == 0){
                if(brewingStepsAdapter.hasNextStep()){
                    String statusToUpdate = "STEP FINISHED";
                    brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, statusToUpdate);
                    updateStatus();
                }
                else{
                    String statusToUpdate = "STEP FINISHED";
                    brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, statusToUpdate);
                    updateStatus();
                }
            }
        }
    };


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void callDefaultSettings(){
        brewingSharedPreferencesEditor.putInt(SharedPreferencesEditor.CURRENT_STEP, 0);
        brewingSharedPreferencesEditor.putString(SharedPreferencesEditor.STATUS, STATUS_INACTIVE);
        brewingStepsAdapter.reset();
        updateCurrentStepCard();
        updateTimerCard(-1);
        updateStatus();
        if(isMyServiceRunning(BrewingService.class)){
            stopService(brewingServiceIntent);
        }
    }

    public void setProgressOnColorBar(int barToShow){
        //0 - none
        //1 - dark blue
        //2 - light blue
        //3 - green
        //4 - orange
        //5 = red
        blueBar.setVisibility(View.INVISIBLE);
        lightBlueBar.setVisibility(View.INVISIBLE);
        greenBar.setVisibility(View.INVISIBLE);
        orangeBar.setVisibility(View.INVISIBLE);
        redBar.setVisibility(View.INVISIBLE);

        switch (barToShow){
            case 1:
                blueBar.setVisibility(View.VISIBLE);
                break;
            case 2:
                lightBlueBar.setVisibility(View.VISIBLE);
                break;
            case 3:
                greenBar.setVisibility(View.VISIBLE);
                break;
            case 4:
                orangeBar.setVisibility(View.VISIBLE);
                break;
            case 5:
                redBar.setVisibility(View.VISIBLE);
                break;
        }
    }


    public void startBrewingService(boolean forcedStart, boolean alarmDismissed, boolean newProcess){
        if(isMyServiceRunning(BrewingService.class)){
            this.stopService(brewingServiceIntent);
        }
        brewingServiceIntent.putExtra("TIME", brewingStepsAdapter.getCurrentStepTime());
        brewingServiceIntent.putExtra("TEMP",  brewingStepsAdapter.getCurrentStepTemperature());
        brewingServiceIntent.putExtra("FORCE_TIMER",  forcedStart);
        brewingServiceIntent.putExtra("ALARM_DISMISSED",  alarmDismissed);
        brewingServiceIntent.putExtra("NEW_PROCESS",  newProcess);
        this.startService(brewingServiceIntent);
    }

}