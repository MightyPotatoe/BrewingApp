package com.example.httpclient.Tests;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.httpclient.Activities.MainMenuActivity;
import com.example.httpclient.DataBase.AppDatabase;
import com.example.httpclient.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.core.internal.deps.guava.base.Preconditions.checkNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withInputType;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class BrewingStepsActivity {

    @Rule
    public ActivityTestRule<MainMenuActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainMenuActivity.class);

    //@BREW-TC-3
    @Test
    public void DoNotUseThermometerCheckboxShouldBeDisabledWhenActivityIsStarted() throws InterruptedException {
        //Preconditions: Clear brewing steps db
        final AppDatabase db = AppDatabase.getInstance(mainActivityActivityTestRule.getActivity());
        db.clearLastBrewingStepDB();
        //Click on Start Brewing button
        TestActions.clickOn(R.id.startBrewingButton);
        //Add new brewing step
        TestActions.clickOn(R.id.addBrewingStepButton);
        TestActions.sendKeys(R.id.tempInput, "36");
        TestActions.sendKeys(R.id.timeInput, "10");
        TestActions.clickOn(R.id.addButton);
        onView(allOf(withId(R.id.tempValueTV), isDescendantOfA(withId(R.id.recyclerView)), hasSibling(withText(containsString("10"))))).check(matches(withText(containsString("36"))));

        //another step
        TestActions.clickOn(R.id.addBrewingStepButton);
        TestActions.sendKeys(R.id.tempInput, "25");
        TestActions.sendKeys(R.id.timeInput, "15");
        TestActions.clickOn(R.id.addButton);
        onView(allOf(withId(R.id.tempValueTV), isDescendantOfA(withId(R.id.recyclerView)), hasSibling(withText(containsString("15"))))).check(matches(withText(containsString("25"))));
        //another step
        TestActions.clickOn(R.id.addBrewingStepButton);
        TestActions.sendKeys(R.id.tempInput, "8");
        TestActions.sendKeys(R.id.timeInput, "7");
        TestActions.clickOn(R.id.addButton);
        onView(allOf(withId(R.id.tempValueTV), isDescendantOfA(withId(R.id.recyclerView)), hasSibling(withText(containsString("7"))))).check(matches(withText(containsString("8"))));

        //verifying if data is added to db.
        Assert.assertEquals(36, (int) db.getAllStepsTemps().get(0));
        Assert.assertEquals(10, (int) db.getAllStepsTimes().get(0));

        Assert.assertEquals(25, (int) db.getAllStepsTemps().get(1));
        Assert.assertEquals(15, (int) db.getAllStepsTimes().get(1));

        Assert.assertEquals(8, (int) db.getAllStepsTemps().get(2));
        Assert.assertEquals(7, (int) db.getAllStepsTimes().get(2));


        //Press back button
        Espresso.pressBack();
        //Click on Start Brewing button
        TestActions.clickOn(R.id.startBrewingButton);

        //Verifying if steps are loaded again
        onView(allOf(withId(R.id.tempValueTV), isDescendantOfA(withId(R.id.recyclerView)), hasSibling(withText(containsString("10"))))).check(matches(withText(containsString("36"))));
        onView(allOf(withId(R.id.tempValueTV), isDescendantOfA(withId(R.id.recyclerView)), hasSibling(withText(containsString("15"))))).check(matches(withText(containsString("25"))));
        onView(allOf(withId(R.id.tempValueTV), isDescendantOfA(withId(R.id.recyclerView)), hasSibling(withText(containsString("7"))))).check(matches(withText(containsString("8"))));


    }

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

}
