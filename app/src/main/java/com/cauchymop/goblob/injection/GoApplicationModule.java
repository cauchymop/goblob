package com.cauchymop.goblob.injection;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.analytics.AnalyticsEventLogger;
import com.cauchymop.goblob.analytics.FirebaseAnalyticsSender;
import com.cauchymop.goblob.logger.EventLogger;
import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.GoogleAccountManager;
import com.cauchymop.goblob.presenter.FeedbackSender;
import com.cauchymop.goblob.presenter.GameMessageGenerator;
import com.cauchymop.goblob.ui.AndroidFeedbackSender;
import com.cauchymop.goblob.ui.AndroidGameRepository;
import com.cauchymop.goblob.ui.GameMessageGeneratorAndroid;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Module to configure dependency injection.
 */
@Module(includes = GoApplicationModule.Bindings.class)
public class GoApplicationModule {

  @Module
  public interface Bindings {
    @Binds
    @Singleton
    Analytics getAnalytics(FirebaseAnalyticsSender analytics);

    @Binds
    @Singleton
    GameRepository provideGameRepository(AndroidGameRepository androidGameRepository);

    @Binds
    @Singleton
    GameMessageGenerator getGameMessageGenerator(GameMessageGeneratorAndroid gameMessageGeneratorAndroid);

    @Binds
    @Singleton
    FeedbackSender getFeedbackSender(AndroidFeedbackSender androidFeedbackSender);
  }

  private Application application;

  public GoApplicationModule(Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  public Application provideApplication() {
    return application;
  }

  @Provides
  @Named("PlayerOneDefaultName")
  public String providePlayerOneDefaultName(AndroidGameRepository androidGameRepository) {
    String lobbyName = androidGameRepository.getLobbyClient().myPlayerName();
    if (!lobbyName.isEmpty()) {
      return lobbyName;
    }
    return application.getString(R.string.player_one_default_name);
  }

  @Provides
  @Named("PlayerTwoDefaultName")
  public String providePlayerTwoDefaultName() {
    return application.getString(R.string.player_two_default_name);
  }

  @Provides
  @Named("ApplicationName")
  public String provideApplicationName() {
    return application.getString(R.string.app_name);
  }

  @Provides
  @Singleton
  public SharedPreferences provideSharedPreferences(Application application) {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }

  @Provides
  @Singleton
  public Context provideContext(Application application) {
    return application;
  }

  @Provides
  @Singleton
  public FirebaseAnalytics provideFirebaseAnalytics(Application application) {
    return FirebaseAnalytics.getInstance(application);
  }

  @Provides
  @Singleton
  public EventLogger provideEventLogger(AnalyticsEventLogger analyticsEventLogger) {
    return analyticsEventLogger;
  }
}
