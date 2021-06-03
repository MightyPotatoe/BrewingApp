package com.example.httpclient.Tests;

import com.example.httpclient.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class TestActions {

    public static void clickOn(int id){
        onView(withId(id)).perform(click());
    }

    public static void sendKeys(int id, String value){
        onView(withId(id)).perform(typeText(value));
    }
}
