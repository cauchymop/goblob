package com.cauchymop.goblob;

import android.util.SparseArray;

/**
 * Class to store and use a heuristic for a {@link GoGame}.
 */
public class GoHeuristic implements Heuristic {
  private static final int MAX_DEPTH = 5;
  private SparseArray[] scores = new SparseArray[MAX_DEPTH];

  private int getPattern(GoBoard board, StoneColor color, int pos) {
    int posy = pos / board.getSize();
    int posx = pos % board.getSize();
    int pattern = 0;
    for (int y = posy -1 ; y <= posy + 1 ; y++) {
      for (int x = posx -1 ; x <= posx + 1 ; x++) {
          // TODO: implement this.
      }
    }
    return pattern;
  }

    @Override
    public void record(Game game, int depth, double reference, int pos, double score) {
        // TODO: implement this.
    }
}
