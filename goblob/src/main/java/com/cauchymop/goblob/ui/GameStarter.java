package com.cauchymop.goblob.ui;

import com.cauchymop.goblob.proto.PlayGameData;

/**
 * Interface defining a class that can start a remote or local game from an existing game or create a new game.
 */
public interface GameStarter {
  void startNewGame();

  void selectGame(String matchId);

  void showUpdateScreen();
}
