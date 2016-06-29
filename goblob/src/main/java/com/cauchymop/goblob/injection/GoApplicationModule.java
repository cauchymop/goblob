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

import java.util.UUID;

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

  private static final String LOCAL_UNIQUE_ID = "LOCAL_UNIQUE_ID";
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
  @Named("LocalUniqueId")
  public String getLocalUniqueId(SharedPreferences preferences) {
    String uid = preferences.getString(LOCAL_UNIQUE_ID, null);
    if (uid == null) {
      uid = UUID.randomUUID().toString();
      preferences.edit().putString(LOCAL_UNIQUE_ID, uid).apply();
    }
    return uid;
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
