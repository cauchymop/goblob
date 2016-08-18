package com.cauchymop.goblob.ui;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.cauchymop.goblob.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

  @Rule
  public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class, false, false);

  @Test
  public void selectingNewGameOption_displaysGameTypeRadioChoices() throws Exception {

    activityRule.launchActivity(null);

    onView(withId(R.id.toolbar_match_spinner)).perform(click());
    onView(withText(R.string.new_game_label)).perform(click());

    onView(withId(R.id.game_type_radio_group)).check(matches(isDisplayed()));
  }

}