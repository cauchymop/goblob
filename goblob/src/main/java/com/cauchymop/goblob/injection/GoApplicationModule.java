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
import com.cauchymop.goblob.presenter.AchievementManager;
import com.cauchymop.goblob.presenter.FeedbackSender;
import com.cauchymop.goblob.presenter.GameMessageGenerator;
import com.cauchymop.goblob.ui.AchievementManagerAndroid;
import com.cauchymop.goblob.ui.AndroidFeedbackSender;
import com.cauchymop.goblob.ui.AndroidGameRepository;
import com.cauchymop.goblob.ui.GameMessageGeneratorAndroid;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.TurnBasedMultiplayerClient;
import com.google.common.base.Preconditions;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Named;
import javax.inject.Singleton;

import androidx.annotation.Nullable;
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
  @Nullable
  public GoogleSignInAccount getSignedInAccount(Context context) {
    return GoogleSignIn.getLastSignedInAccount(context);
  }

  @Provides
  @Named("Not Null")
  public GoogleSignInAccount getNotNullableSignedInAccount(@Nullable GoogleSignInAccount googleSignInAccount) {
    return Preconditions.checkNotNull(googleSignInAccount);
  }

  @Provides
  public TurnBasedMultiplayerClient getTurnBasedMultiplayerClient(Context context, @Named("Not Null") GoogleSignInAccount account) {
    return Games.getTurnBasedMultiplayerClient(context, account);
  }

  @Provides
  public PlayersClient getPlayerClient(Context context, @Named("Not Null") GoogleSignInAccount account) {
    return Games.getPlayersClient(context, account);
  }

  @Provides
  public AchievementsClient getAchievementsClient(Context context, @Named("Not Null") GoogleSignInAccount account) {
    return Games.getAchievementsClient(context, account);
  }

  @Provides
  @Named("PlayerOneDefaultName")
  public String providePlayerOneDefaultName(GoogleAccountManager googleAccountManager,
      AvatarManager avatarManager) {
    if (googleAccountManager.getSignInComplete()) {
      Player currentPlayer = googleAccountManager.getCurrentPlayer();
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

  @Provides
  @Singleton
  public EventLogger getFEventLogger(FirebaseAnalytics firebaseAnalytics) {
    return new AnalyticsEventLogger(firebaseAnalytics);
  }

}
