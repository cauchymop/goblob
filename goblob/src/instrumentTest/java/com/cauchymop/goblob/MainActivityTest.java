package com.cauchymop.goblob;

import android.test.ActivityInstrumentationTestCase2;

import com.cauchymop.goblob.ui.MainActivity;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.cauchymop.goblob.MainActivityTest \
 * com.cauchymop.goblob.tests/android.test.InstrumentationTestRunner
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

  public MainActivityTest() {
    super(MainActivity.class);
  }
}
