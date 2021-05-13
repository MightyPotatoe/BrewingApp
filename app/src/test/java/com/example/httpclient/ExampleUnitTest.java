package com.example.httpclient;

import com.example.httpclient.Utilities.IpChecker;
import com.example.httpclient.Utilities.IpDetector;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void ipChecker() {
        assertTrue(IpChecker.isIp("192.168.50.61"));
        assertTrue(IpChecker.isIp("192.168.4.1"));
        assertFalse(IpChecker.isIp("asd.as.asda.as"));

        assertTrue(IpDetector.detect("dupa sdasd 192.168.50.61\n asdkjlh"));
        assertTrue(IpDetector.detect("dupa sdasd 192.168.4.1 asdkjlh"));
        assertFalse(IpDetector.detect("dupa sdasd asd.s.4.1 asdkjlh"));


    }
}