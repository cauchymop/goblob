package com.cauchymop.goblob.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module to configure dependency injection.
 */
@Module(
    injects = {
        GameConfigurationFragment.class,
        GameDatas.class,
        GameFragment.class,
        GoGameController.class,
        LocalGameRepository.class,
        MainActivity.class,
    }
)
class GoApplicationModule {

  private Context applicationContext;

  public GoApplicationModule(Context applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Provides
  @Named("OpponentDefaultName")
  String provideOpponentDefaultName() {
    return applicationContext.getString(R.string.opponent_default_name);
  }

  @Provides
  @Singleton
  Context getApplicationContext() {
    return applicationContext;
  }

  @Provides
  @Singleton
  SharedPreferences getSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }
}
