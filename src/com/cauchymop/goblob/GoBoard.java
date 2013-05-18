package com.cauchymop.goblob;

/**
 * Class to represent the state of a Go board.
 */
public interface GoBoard {

  public void empty();

  public boolean play(StoneColor color, int x, int y);

  public double getScore(StoneColor color);

  public StoneColor getColor(int x, int y);

  public int getSize();

  public void copyFrom(GoBoard board);
}
