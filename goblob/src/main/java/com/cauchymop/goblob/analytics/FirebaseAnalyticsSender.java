package com.cauchymop.goblob.analytics;

import android.os.Bundle;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.proto.PlayGameData;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

public class FirebaseAnalyticsSender implements Analytics {
  private final FirebaseAnalytics firebaseAnalytics;

  @Inject
  public FirebaseAnalyticsSender(FirebaseAnalytics firebaseAnalytics) {
    this.firebaseAnalytics = firebaseAnalytics;
  }

  @Override
  public void gameCreated(PlayGameData.GameData game) {
    Bundle bundle = new Bundle();
    bundle.putString("type", game.getGameConfiguration().getGameType().name());
    firebaseAnalytics.logEvent("game created", bundle);
  }

  @Override
  public void configurationChanged(PlayGameData.GameData game) {
    Bundle bundle = new Bundle();
    PlayGameData.GameType gameType = game.getGameConfiguration().getGameType();
    if (gameType == PlayGameData.GameType.REMOTE) {
      bundle.putBoolean("agreed", (game.getPhase() == PlayGameData.GameData.Phase.IN_GAME));
      firebaseAnalytics.logEvent("configuration changed", bundle);
    }
  }

  @Override
  public void undo() {
    firebaseAnalytics.logEvent("undo", Bundle.EMPTY);
  }

  @Override
  public void redo() {
    firebaseAnalytics.logEvent("redo", Bundle.EMPTY);
  }

  @Override
  public void resign() {
    firebaseAnalytics.logEvent("resign", Bundle.EMPTY);
  }
}
