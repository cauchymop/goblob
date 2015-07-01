package com.cauchymop.goblob.ui;

import com.cauchymop.goblob.model.GoGameController;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

/**
 * Interface defining a class that can start a remote or local game from an existing game or create a new game.
 */
public interface GameStarter {
  void startNewGame();
  void startRemoteGame(String matchId);
  void startLocalGame(GoGameController gameController);
  void showUpdateScreen();
}
