package com.cauchymop.goblob.ui;

import com.cauchymop.goblob.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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