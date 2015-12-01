package com.cauchymop.goblob.ui;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Player;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.google.android.gms.games.Games.Players;

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

  private final GoogleApiClientProviderProvider googleApiClientProviderProvider;
  private Application application;

  public GoApplicationModule(Application application) {
    this.application = application;
    googleApiClientProviderProvider = new GoogleApiClientProviderProvider();
    application.registerActivityLifecycleCallbacks(googleApiClientProviderProvider);
  }

  @Provides
  GoogleApiClientProvider provideGoogleApiClientProvider() {
    return googleApiClientProviderProvider.get();
  }

  @Provides
  GoogleApiClient provideGoogleApiClient(GoogleApiClientProvider googleApiClientProvider) {
    return googleApiClientProvider.get();
  }

  @Provides
  @Singleton
  AvatarManager provideAvatarManager(Context context) {
    return new AvatarManager(context);
  }

  @Provides
  @Named("PlayerOneDefaultName")
  String providePlayerOneDefaultName(GoogleApiClient googleApiClient, AvatarManager avatarManager) {
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
  String providePlayerTwoDefaultName() {
    return application.getString(R.string.player_two_default_name);
  }

  @Provides
  @Singleton
  Context getApplicationContext() {
    return application;
  }

  @Provides
  @Singleton
  SharedPreferences getSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  public interface GoogleApiClientProvider extends Provider<GoogleApiClient> {
  }

}
