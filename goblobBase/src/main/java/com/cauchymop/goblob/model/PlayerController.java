package com.cauchymop.goblob.model;

import java.io.Serializable;

/**
 * A class for interactions between a {@link GoPlayer} and a {@link GoGame}.
 */
public abstract class PlayerController implements Serializable {
  public abstract void startTurn();

  public void opponentPlayed(int move) {
  }
}
