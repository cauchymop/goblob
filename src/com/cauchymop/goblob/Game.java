package com.cauchymop.goblob;

import com.google.common.collect.Sets;

import java.util.Set;

/**
* Interface for a generic game. Implement this to use {@link AlphaBeta}.
*/
public abstract class Game {

  private Set<Listener> listeners = Sets.newHashSet();

  public abstract void undo();
  public abstract Game copy();
  public abstract int getPosCount();
  public abstract boolean play(int pos);
  public abstract boolean isGameEnd();
  public abstract double getScore();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  protected void fireGameChanged() {
    for (Listener listener : listeners) {
      listener.gameChanged(this);
    }
  }

  public interface Listener {
    public void gameChanged(Game game);
  }
}
