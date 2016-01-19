package com.cauchymop.goblob.ui;

import android.app.Application;

import com.cauchymop.goblob.injection.DaggerGoApplicationComponent;
import com.cauchymop.goblob.injection.GoApplicationComponent;
import com.cauchymop.goblob.injection.GoApplicationModule;

/**
 * Top level application for Game of Go.
 */
public class GoApplication extends Application {

  private GoApplicationComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    component = DaggerGoApplicationComponent.builder()
        .goApplicationModule(new GoApplicationModule(this))
        .build();
  }

  public GoApplicationComponent getComponent() {
    return component;
  }

}
