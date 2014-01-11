package com.cauchymop.goblob.model;

/**
 * A {@link Player} for the Game of Go.
 */
public class GoPlayer extends Player {

  private StoneColor stoneColor = StoneColor.Black;

  public GoPlayer(PlayerType type, String name) {
    super(type, name);
  }

  public StoneColor getStoneColor() {
    return stoneColor;
  }

  public void setStoneColor(StoneColor stoneColor) {
    this.stoneColor = stoneColor;
  }

}
