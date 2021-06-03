package com.example.httpclient.Tests;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;

public class CircularProgressBarUtils {

    public static int getProgress(Matcher<View> matcher) {
        final int[] progress = {0};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(CircularProgressIndicator.class);
            }

            @Override
            public String getDescription() {
                return "CircularProgressIndicator";
            }

            @Override
            public void perform(UiController uiController, View view) {
                CircularProgressIndicator circularProgressIndicator = (CircularProgressIndicator)view;
                progress[0] = circularProgressIndicator.getProgress();
            }
        });
        return progress[0];
    }

    public static int getMax(Matcher<View> matcher) {
        final int[] progress = {0};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(CircularProgressIndicator.class);
            }

            @Override
            public String getDescription() {
                return "CircularProgressIndicator";
            }

            @Override
            public void perform(UiController uiController, View view) {
                CircularProgressIndicator circularProgressIndicator = (CircularProgressIndicator)view;
                progress[0] = circularProgressIndicator.getMax();
            }
        });
        return progress[0];
    }

}
