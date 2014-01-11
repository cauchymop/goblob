package com.cauchymop.goblob.model;

/**
 * Layer containing who owns what intersection.
 */
public class Territories {
  private StoneColor[] territories;

  public Territories(int size) {
    territories = new StoneColor[size];
  }

  public StoneColor getTerritory(int pos) {
    return territories[pos];
  }

  public void mark(int pos, StoneColor color) {
    // TODO: be smarter
    territories[pos] = color;
  }
}
