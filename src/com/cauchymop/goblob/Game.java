package com.cauchymop.goblob;

/**
* Interface for a generic game. Implement this to use {@link AI}.
*/
public interface Game {
  public abstract void undo();
  public abstract int getPosCount();
  public abstract boolean play(int pos);
  public abstract boolean isGameEnd();
  public abstract double getScore();
}
