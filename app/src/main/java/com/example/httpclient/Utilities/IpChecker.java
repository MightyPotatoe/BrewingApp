package com.example.httpclient.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpChecker {
    public static boolean isIp(String ip) {
        // Check if the string is not null
        if (ip == null) {
            return false;
        }
        // Get the parts of the ip
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String s : parts) {
            try {
                int value = Integer.parseInt(s);
                // out of range
                if (value <= 0 || value >= 255) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}