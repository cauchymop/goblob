package com.cauchymop.goblob.model;

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
    game.play(this, ai.getBestMove(game, DEPTH));
  }

  public AI getAi() {
    return ai;
  }
}
