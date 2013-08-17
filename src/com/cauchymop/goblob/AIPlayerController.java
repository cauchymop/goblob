package com.cauchymop.goblob;

/**
 * Class representing a {@link Player} playing {@link AlphaBeta} moves.
 */
public class AIPlayerController extends PlayerController {
  private static final int DEPTH = 5;
  private Game game;
  private AI ai;

  public AIPlayerController(Game game) {
    this.game = game;
    ai = new AlphaBeta();
  }

  @Override
  public void startTurn() {
    game.play(ai.getBestMove(game, HeuristicProvider.getHeuristic(game), DEPTH));
  }
}
