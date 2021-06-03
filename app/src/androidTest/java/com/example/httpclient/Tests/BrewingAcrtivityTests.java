package com.example.httpclient.Tests;

import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.httpclient.Activities.MainMenuActivity;
import com.example.httpclient.R;
import com.example.httpclient.Utilities.SharedPreferencesEditor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class BrewingAcrtivityTests {

    @Rule
    public ActivityTestRule<MainMenuActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainMenuActivity.class);

    private MainMenuActivity mainMenuActivity = null;

    @Before
    public void setUp() throws Exception{
        mainMenuActivity = mainActivityActivityTestRule.getActivity();
    }

    //@BREW-TC-3
    @Test
    public void DoNotUseThermometerCheckboxShouldBeDisabledWhenActivityIsStarted(){
        //Click on Start Brewing button
        onView(withId(R.id.startBrewingButton)).perform(click());
        //Click on start brewing
        onView(withId(R.id.startBrewingButton)).perform(click());
        //Verify if 'Don't use thermometer Checkbox' is disabled.
        onView(withId(R.id.doNotUseThermometerCheckbox)).check(matches(not(isEnabled())));
    }

}
