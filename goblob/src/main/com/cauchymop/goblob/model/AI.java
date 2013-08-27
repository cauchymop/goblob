package com.cauchymop.goblob.model;

/**
 * Interface for an Artificial Intelligence.
 */
public interface AI {
  int getBestMove(Game game, int depth);

  public double[] getScores();
}
