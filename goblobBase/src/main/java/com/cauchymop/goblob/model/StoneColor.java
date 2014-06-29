package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

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

  public PlayGameData.MatchEndStatus.Color getGameDataColor() {
    switch (this) {
      case White:
        return PlayGameData.MatchEndStatus.Color.WHITE;
      case Black:
        return PlayGameData.MatchEndStatus.Color.BLACK;
      default:
        throw new RuntimeException("Invalid color");
    }
  }
}
