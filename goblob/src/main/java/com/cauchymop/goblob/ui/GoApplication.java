package com.cauchymop.goblob.ui;

import android.app.Application;
import android.os.Bundle;

import com.cauchymop.goblob.injection.Injector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

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
