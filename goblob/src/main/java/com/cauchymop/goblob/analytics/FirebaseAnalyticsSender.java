package com.cauchymop.goblob.analytics;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.Score;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

public class FirebaseAnalyticsSender implements Analytics {
  private final FirebaseAnalytics firebaseAnalytics;

  @Inject
  public FirebaseAnalyticsSender(FirebaseAnalytics firebaseAnalytics) {
    this.firebaseAnalytics = firebaseAnalytics;
  }

  @Override
  public void gameCreated(GameData game) {
    Bundle bundle = getGameConfigurationBundle(game.getGameConfiguration());
    firebaseAnalytics.logEvent("game created", bundle);
  }

  @Override
  public void configurationChanged(GameData game) {
    PlayGameData.GameType gameType = game.getGameConfiguration().getGameType();
    if (gameType == PlayGameData.GameType.REMOTE) {
      Bundle bundle = getGameConfigurationBundle(game.getGameConfiguration());
      bundle.putBoolean("agreed", (game.getPhase() == Phase.IN_GAME));
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

  @Override
  public void movePlayed(GameConfiguration gameConfiguration, PlayGameData.Move move) {
    if (move.getType() == PlayGameData.Move.MoveType.PASS) {
      firebaseAnalytics.logEvent("passed", getGameConfigurationBundle(gameConfiguration));
    } else {
      firebaseAnalytics.logEvent("movePlayed", getGameConfigurationBundle(gameConfiguration));
    }
  }

  @Override
  public void deadStoneToggled(GameConfiguration gameConfiguration) {
    firebaseAnalytics.logEvent("deadStoneToggled", getGameConfigurationBundle(gameConfiguration));
  }

  @Override
  public void invalidMovePlayed(GameConfiguration gameConfiguration) {
    firebaseAnalytics.logEvent("invalidMovePlayed", getGameConfigurationBundle(gameConfiguration));
  }

  @Override
  public void gameFinished(GameConfiguration gameConfiguration, Score score) {
    Bundle gameConfigurationBundle = getGameConfigurationBundle(gameConfiguration);
    gameConfigurationBundle.putBoolean("resigned", score.getResigned());
    gameConfigurationBundle.putFloat("wonBy", score.getWonBy());
    gameConfigurationBundle.putString("winner", score.getWinner().toString());
    firebaseAnalytics.logEvent("gameFinished", gameConfigurationBundle);
  }

  @NonNull
  private Bundle getGameConfigurationBundle(GameConfiguration gameConfiguration) {
    Bundle bundle = new Bundle();
    bundle.putString("type", gameConfiguration.getGameType().name());
    bundle.putInt("size", gameConfiguration.getBoardSize());
    bundle.putInt("handicap", gameConfiguration.getHandicap());
    return bundle;
  }
}
