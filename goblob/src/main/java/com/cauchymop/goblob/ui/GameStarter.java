package com.cauchymop.goblob.ui;

import com.cauchymop.goblob.model.GoGame;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

/**
 * Created by olivierbonal on 05/05/14.
 */
public interface GameStarter {
  public void startNewGame();
  public void startRemoteGame(TurnBasedMatch match);
  public void startLocalGame(GoGame game);
}
