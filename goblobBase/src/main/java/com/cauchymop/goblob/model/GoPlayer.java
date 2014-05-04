package com.cauchymop.goblob.model;

import java.io.Serializable;

/**
 * A Player for the Game of Go.
 */
public class GoPlayer implements Serializable {

  private PlayerType type;
  private String id;
  private String name;

  public GoPlayer(PlayerType type, String id, String name) {
    this.type = type;
    this.id = id;
    this.name = name;
  }

  public PlayerType getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return String.format("Player(type=%s, id=%s, name=%s)", getType(), getId(), getName());
  }

  public enum PlayerType {
    LOCAL,
    REMOTE
  }
}
