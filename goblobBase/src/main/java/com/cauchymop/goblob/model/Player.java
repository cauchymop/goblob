package com.cauchymop.goblob.model;

import java.io.Serializable;

/**
 * Interface to represent a player.
 */
public class Player implements Serializable {

  private PlayerType type;
  private String name;

  public Player(PlayerType type, String name) {
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
    AI(false),
    HUMAN_LOCAL(false),
    HUMAN_REMOTE_FRIEND(true),
    HUMAN_REMOTE_RANDOM(true);

    private final boolean remote;

    PlayerType(boolean remote) {
      this.remote = remote;
    }

    public boolean isRemote() {
      return remote;
    }
  }
}
