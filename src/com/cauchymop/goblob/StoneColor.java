package com.cauchymop.goblob;

/**
* Colors which can be used on a {@link Board}.
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
}
