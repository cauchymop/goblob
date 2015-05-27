package com.cauchymop.goblob.ui;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

/**
 * Top level application for Game of Go.
 */
public class GoApplication extends Application {
  private ObjectGraph objectGraph;

  @Override
  public void onCreate() {
    super.onCreate();
    objectGraph = ObjectGraph.create(new GoApplicationModule());
  }

  public void inject(Object instance) {
    objectGraph.inject(instance);
  }

  @Module(
    injects = {
        LocalGameRepository.class,
        MainActivity.class,
        GameFragment.class
    }
  )
  class GoApplicationModule {

    @Provides @Singleton
    Context getApplicationContext() {
      return GoApplication.this;
    }

    @Provides @Singleton
    SharedPreferences getSharedPreferences(Context context) {
      return PreferenceManager.getDefaultSharedPreferences(context);
    }
  }
}
