package com.example.httpclient;

import android.widget.TextView;

import java.util.concurrent.TimeoutException;

public class Wait {

    public static boolean untilTextViewContentIsEqual(TextView textView, String expectedString, int timeout) throws Exception {
        int counter = 0;
        while (counter < timeout*1000){
            if(textView.getText().equals(expectedString)){
                return true;
            }
            Thread.sleep(500);
            counter += 500;
        }
        throw new TimeoutException("Timeout while waiting for field" + textView.getId() + " : " + expectedString);
    }

    public static boolean untilTextViewContentIsChanged(TextView textView, String expectedString, int timeout) throws Exception {
        int counter = 0;
        while (counter < timeout*1000){
            if(textView.getText().equals(expectedString)){
                Thread.sleep(500);
                counter += 500;
            }
            else{
                return true;
            }

        }
        throw new TimeoutException("Timeout while waiting for field" + textView.getId() + " : " + expectedString);
    }
}
