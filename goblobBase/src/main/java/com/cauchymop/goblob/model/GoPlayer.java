package com.cauchymop.goblob.model;

import java.io.Serializable;

/**
 * A Player for the Game of Go.
 */
public class GoPlayer implements Serializable {

  private PlayerType type;
  private String name;

  public GoPlayer(PlayerType type, String name) {
    this.type = type;
    this.name = name;
  }

  public PlayerType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return String.format("Player(type=%s, name=%s)", getType(), getName());
  }

  public enum PlayerType {
    LOCAL,
    REMOTE
  }
}
