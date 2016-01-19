package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.cauchymop.goblob.injection.GoApplicationModule;

/**
 * Created by abela on 11/19/15.
 */
public class GoogleApiClientProviderProvider implements Application.ActivityLifecycleCallbacks {
  private GoApplicationModule.GoogleApiClientProvider googleApiClientProvider;

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    if (activity instanceof GoApplicationModule.GoogleApiClientProvider) {
      googleApiClientProvider = (GoApplicationModule.GoogleApiClientProvider) activity;
    }
  }

  public GoApplicationModule.GoogleApiClientProvider get() {
    return googleApiClientProvider;
  }

  @Override
  public void onActivityStarted(Activity activity) {

  }

  @Override
  public void onActivityResumed(Activity activity) {

  }

  @Override
  public void onActivityPaused(Activity activity) {

  }

  @Override
  public void onActivityStopped(Activity activity) {

  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override
  public void onActivityDestroyed(Activity activity) {

  }

}
