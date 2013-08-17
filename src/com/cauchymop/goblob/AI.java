package com.cauchymop.goblob;

/**
 * Interface for an Artificial Intelligence.
 */
public interface AI {
  int getBestMove(Game game, Heuristic heuristic, int depth);
}
