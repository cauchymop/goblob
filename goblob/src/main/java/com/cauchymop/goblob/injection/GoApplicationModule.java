package com.cauchymop.goblob.injection;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.analytics.FirebaseAnalyticsSender;
import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.GoogleApiClientManager;
import com.cauchymop.goblob.presenter.AchievementManager;
import com.cauchymop.goblob.presenter.FeedbackSender;
import com.cauchymop.goblob.presenter.GameMessageGenerator;
import com.cauchymop.goblob.ui.AchievementManagerAndroid;
import com.cauchymop.goblob.ui.AndroidFeedbackSender;
import com.cauchymop.goblob.ui.AndroidGameRepository;
import com.cauchymop.goblob.ui.GameMessageGeneratorAndroid;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Player;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import static com.google.android.gms.games.Games.Players;

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
    AchievementManager getAchievementManager(AchievementManagerAndroid achievementManagerAndroid);

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
  public GoogleApiClient provideGoogleApiClient(GoogleApiClientManager googleApiClientManager) {
    return googleApiClientManager.get();
  }

  @Provides
  @Named("PlayerOneDefaultName")
  public String providePlayerOneDefaultName(GoogleApiClient googleApiClient,
      AvatarManager avatarManager) {
    if (googleApiClient.isConnected()) {
      Player currentPlayer = Players.getCurrentPlayer(googleApiClient);
      avatarManager.setAvatarUri(currentPlayer.getDisplayName(), currentPlayer.getIconImageUri());
      return currentPlayer.getDisplayName();
    } else {
      return application.getString(R.string.player_one_default_name);
    }
  }

  @Provides
  @Named("PlayerTwoDefaultName")
  public String providePlayerTwoDefaultName() {
    return application.getString(R.string.player_two_default_name);
  }

  @Provides
  @Singleton
  public Context getApplicationContext() {
    return application;
  }

  @Provides
  @Singleton
  public SharedPreferences getSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Provides
  @Singleton
  public FirebaseAnalytics getFireBaFirebaseAnalytics(Context context) {
    return FirebaseAnalytics.getInstance(context);
  }

}
