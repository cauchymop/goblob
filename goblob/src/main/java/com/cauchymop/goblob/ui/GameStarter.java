package com.cauchymop.goblob.ui;

import com.cauchymop.goblob.model.GoGameController;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

/**
 * Interface defining a class that can start a remote or local game from an existing game or create a new game.
 */
public interface GameStarter {
  public void startNewGame();
  public void startRemoteGame(String matchId);
  public void startLocalGame(GoGameController gameController);
}
