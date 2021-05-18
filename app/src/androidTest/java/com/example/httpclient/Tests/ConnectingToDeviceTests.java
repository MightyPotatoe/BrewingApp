package com.example.httpclient.Tests;

import android.widget.Button;
import android.widget.TextView;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.example.httpclient.Activities.MainMenuActivity;
import com.example.httpclient.R;
import com.example.httpclient.Wait;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConnectingToDeviceTests {

    @Rule
    public ActivityTestRule<MainMenuActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainMenuActivity.class);

    private MainMenuActivity mainMenuActivity = null;
    private TextView infoTV;
    private Button configureNowButton;

    @Before
    public void setUp() throws Exception{
        mainMenuActivity = mainActivityActivityTestRule.getActivity();

    }

    //BERW-28
    @Test
    public void connectionWithDeviceFailed() throws Exception {

        //Wait until prompt with device disconnected message
        Wait.untilTextViewContentIsEqual(mainMenuActivity.findViewById(R.id.infoTV)
                , mainMenuActivity.getResources().getString(R.string.MMA_DEVICE_DISCONNECTED_INFO) , 10);

        //Verify that "Looks like your device is disconnected.Make sure you are connected to the same WiFi network..."
        onView(withId(R.id.infoTV))
                .check(matches(withText(mainMenuActivity.getResources().getString(R.string.MMA_DEVICE_DISCONNECTED_INFO))));

        //Verify that device status is shown as 'Disconnected'
        onView(withId(R.id.deviceStatusTV))
                .check(matches(withText(mainMenuActivity.getResources().getString(R.string.MMA_DISCONNECTED))));

        //Verify if 'START BREWING' button is enabled.
        onView(withId(R.id.startBrewingButton))
                .check(matches(isEnabled()));

        //Click on 'CONFIGURE NOW' button.
        onView(withId(R.id.configureNowButton)).perform(click());

        //Verify if 'Connecting to device..' TV is visible
        onView(withId(R.id.DAI_header))
                .check(matches(withText(mainMenuActivity.getResources().getString(R.string.DIA_connecting))));

        //Verify that 'Please Wait...' message is displayed
        onView(withId(R.id.DAI_caption))
                .check(matches(withText(mainMenuActivity.getResources().getString(R.string.DIA_please_wait))));

        //Verify that loading indicator is displayed.
        onView(withId(R.id.DAI_progressIndicator))
                .check(matches(isDisplayed()));

        //Wait until prompt with device disconnected message
        Wait.untilTextViewContentIsEqual(mainMenuActivity.findViewById(R.id.DAI_header)
                , mainMenuActivity.getResources().getString(R.string.DIA_connection_failed) , 10);



    }
}
