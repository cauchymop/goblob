package com.cauchymop.goblob.ui;

import android.app.Application;

import com.cauchymop.goblob.injection.Injector;

import dagger.ObjectGraph;

/**
 * Top level application for Game of Go.
 */
public class GoApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    ObjectGraph objectGraph = ObjectGraph.create(new GoApplicationModule(this));
    Injector.setObjectGraph(objectGraph);
  }
}
