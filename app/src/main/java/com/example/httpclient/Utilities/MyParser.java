package com.example.httpclient.Utilities;

public abstract class MyParser {

    public static double parseDouble(String value){
        String temp = value.replaceAll("[^0-9.]","");
        if(!temp.isEmpty()){
            return Double.parseDouble(temp);
        }
        return -1;
    }

}
