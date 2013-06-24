package com.cauchymop.goblob;

import android.os.Build;

/**
 * Class representing a {@link Player} playing {@link AI} moves.
 */
public class AIPlayer implements Player {
  private static final int DEPTH = 5;

  @Override
  public String getName() {
    return Build.MODEL;
  }

  @Override
  public void startTurn(Game game) {
    game.play(AI.getBestMove(game, HeuristicProvider.getHeuristic(game), DEPTH));
  }
}
