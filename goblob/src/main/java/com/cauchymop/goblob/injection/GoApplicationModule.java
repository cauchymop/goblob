package com.cauchymop.goblob.injection;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GoogleApiClientManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Player;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.google.android.gms.games.Games.Players;

/**
 * Module to configure dependency injection.
 */
@Module
public class GoApplicationModule {

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
  @Singleton
  @Named("LocalGoogleIdentity")
  public String getLocalGoogleId(GoogleApiClient googleApiClient) {
    if (!googleApiClient.isConnected()) {
      return null;
    }
    return Players.getCurrentPlayerId(googleApiClient);
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
}
