package com.example.httpclient.Threads;

import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.os.Looper;
import android.widget.Toast;

import com.example.httpclient.Observer.Observable;
import com.example.httpclient.Observer.Observer;
import com.example.httpclient.services.HTTPController;

import java.util.HashSet;
import java.util.Set;

public class MeasureThread extends Thread implements HTTPController.OnHttpResponseListener, Observable {

    private HTTPController httpController;
    private String tempNow = "-";
    private Set<Observer> observers = new HashSet<>();
    public static volatile MeasureThread instance = null;
    private boolean stop = false;

    private MeasureThread(Context context, String url) {
        if(instance != null) {
            throw new RuntimeException("Not allowed. Please use getInstance() method");
        }
        httpController = new HTTPController(context, url);
        httpController.setHttpResponseListener(this);
    }

    public static MeasureThread getInstance(Context context, String url) {
        if (instance == null) {
            instance = new MeasureThread(context, url);
        }
        return instance;
    }

    @Override
    public void run() {
        stop = false;
        while (!stop){
            httpController.sendRequest("/getTemp");
            notifyObservers();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        instance = null;
    }


    @Override
    public void onResponseReceived(String response){
        if(response.contains("Cannot access the Device")){
            tempNow = "Disconnected";
        }
        else{
            tempNow = response.substring(0,4) + "\u00B0C";
        }
        notifyObservers();
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(tempNow);
        }
    }

    @Override
    public synchronized void start() {
        if(!isAlive()){
            super.start();
        }
    }

    public void stopThread(){
        stop = true;
    }
}

