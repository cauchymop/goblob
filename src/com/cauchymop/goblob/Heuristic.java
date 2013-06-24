package com.cauchymop.goblob;

/**
 * Interface to manage game heuristics.
 */
public interface Heuristic {
  public void record(Game game, int depth, double reference, int pos, double score);
}
