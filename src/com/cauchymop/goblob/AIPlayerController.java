package com.cauchymop.goblob;

/**
 * Class representing a {@link Player} playing {@link AI} moves.
 */
public class AIPlayerController extends PlayerController {
  private static final int DEPTH = 5;
  private Game game;

  public AIPlayerController(Game game) {
    this.game = game;
  }

  @Override
  public void startTurn() {
    game.play(AI.getBestMove(game, HeuristicProvider.getHeuristic(game), DEPTH));
  }
}
