package com.cauchymop.goblob;

import android.os.Parcelable;

/**
 * Class to represent the state of a Go board, and apply the logic of playing a move.
 */
public interface GoBoard extends Parcelable {

  public void clear();

  public boolean play(StoneColor color, int x, int y);

  public double getScore(StoneColor color);

  public StoneColor getColor(int x, int y);

  public int getSize();

  public void copyFrom(GoBoard board);
}
