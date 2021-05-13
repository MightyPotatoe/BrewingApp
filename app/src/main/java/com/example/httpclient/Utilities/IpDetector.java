package com.example.httpclient.Utilities;

public class IpDetector {

    // Detects an Ip given a phrace
    public static boolean detect(String ip) {
        return extractIP(ip) != null;
    }


    public static String extractIP(String s) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(?<!\\d|\\d\\.)" +
                        "(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])" +
                        "(?!\\d|\\.\\d)").matcher(s);
        return m.find() ? m.group() : null;
    }
}
