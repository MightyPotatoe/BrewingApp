package com.example.httpclient.Threads;

import android.content.Context;

import com.example.httpclient.Observer.Observable;
import com.example.httpclient.Observer.Observer;
import com.example.httpclient.services.HTTPController;

import java.util.HashSet;
import java.util.Set;

public class DeviceStatusThread extends Thread implements HTTPController.OnHttpResponseListener, Observable {

    private HTTPController httpController;
    private String status = "Connecting...";
    private Set<Observer> observers = new HashSet<>();
    public static volatile DeviceStatusThread instance = null;
    private boolean stop = false;

    private DeviceStatusThread(Context context, String url) {
        if (instance != null) {
            throw new RuntimeException("Not allowed. Please use getInstance() method");
        }
        httpController = new HTTPController(context, url);
        httpController.setHttpResponseListener(this);
    }

    public static DeviceStatusThread getInstance(Context context, String url) {
        if (instance == null) {
            instance = new DeviceStatusThread(context, url);
        }
        return instance;
    }

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            httpController.sendRequest("/CHECK_STATUS");
        }
        instance = null;
    }


    @Override
    public void onResponseReceived(String response) {
        status = response;
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
            observer.update(status);
        }
    }

    @Override
    public synchronized void start() {
        if (!isAlive()) {
            super.start();
        }
    }

    public void stopThread() {
        stop = true;
    }
}

