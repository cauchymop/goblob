package com.cauchymop.goblob;

/**
* Colors which can be used in a {@link GoGame}.
*/
public enum StoneColor {
  Empty,
  Border,
  Black,
  White,
  BlackTerritory,
  WhiteTerritory;

  public StoneColor getOpponent() {
    switch(this) {
      case White:
        return Black;
      case Black:
        return White;
      default:
        throw new RuntimeException("Invalid color");
    }
  }

  public StoneColor getPlayerColor() {
    switch (this) {
      case BlackTerritory:
        return Black;
      case WhiteTerritory:
        return White;
      default:
        return this;
    }
  }

  public boolean isEmpty() {
    return this == Empty || this == BlackTerritory || this == WhiteTerritory;
  }
}
