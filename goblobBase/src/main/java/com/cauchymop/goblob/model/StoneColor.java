package com.cauchymop.goblob.model;

import java.io.Serializable;

/**
 * Colors which can be used in a {@link GoGame}.
 */
public enum StoneColor implements Serializable {
  Black,
  White;

  public StoneColor getOpponent() {
    switch (this) {
      case White:
        return Black;
      case Black:
        return White;
      default:
        throw new RuntimeException("Invalid color");
    }
  }
}
