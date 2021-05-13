package com.example.httpclient;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.httpclient.Activities.MainMenuActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainMenuActivity.class);

    private MainMenuActivity mainMenuActivity = null;

    @Before
    public void setUp() throws Exception{
        mainMenuActivity = mainActivityActivityTestRule.getActivity();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.httpclient", appContext.getPackageName());
    }

    //@BREW-23
    @Test
    public void StartBrewingButtonShouldBeAlwaysEnabled() throws Exception {
        // Context of the app under test.
        TextView connectingMessage = mainMenuActivity.findViewById(R.id.deviceStatusTV);
        Assert.assertEquals(connectingMessage.getText(), mainMenuActivity.getResources().getString(R.string.MMA_CONNECTING));

        Button startBrewing = mainMenuActivity.findViewById(R.id.startBrewingButton);
        Assert.assertTrue(startBrewing.isEnabled());

        Wait.untilTextViewContentIsChanged(connectingMessage, mainMenuActivity.getResources().getString(R.string.MMA_CONNECTING), 10);
        Assert.assertTrue(startBrewing.isEnabled());
    }
}