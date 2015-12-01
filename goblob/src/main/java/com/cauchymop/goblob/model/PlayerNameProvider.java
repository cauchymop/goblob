package com.cauchymop.goblob.model;

import android.content.Context;

import com.cauchymop.goblob.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Player;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.android.gms.games.Games.Players;

public class PlayerNameProvider {

  @Inject @Named("PlayerTwoDefaultName") String playerTwoDefaultName;
  @Inject GoogleApiClient googleApiClient;
  @Inject AvatarManager avatarManager;
  @Inject Context context;

  String getPlayerOneDefaultName() {
    if (googleApiClient.isConnected()) {
      Player currentPlayer = Players.getCurrentPlayer(googleApiClient);
      avatarManager.setAvatarUri(currentPlayer.getDisplayName(), currentPlayer.getIconImageUri());
      return currentPlayer.getDisplayName();
    } else {
      return context.getString(R.string.player_one_default_name);
    }
  }

  String getPlayerTwoDefaultName() {
    return playerTwoDefaultName;
  }
}
