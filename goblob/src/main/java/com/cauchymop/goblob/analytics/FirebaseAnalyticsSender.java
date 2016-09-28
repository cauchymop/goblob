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
}
