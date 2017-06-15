package com.cauchymop.goblob.viewmodel;

import com.cauchymop.goblob.proto.PlayGameData;

public class PlayerViewModel {
  private final String playerName;
  private final PlayGameData.Color playerColor;

  public PlayerViewModel(final String playerName, PlayGameData.Color playerColor) {
    this.playerName = playerName;
    this.playerColor = playerColor;
  }

  public String getPlayerName() {
    return playerName;
  }


  public PlayGameData.Color getPlayerColor() {
    return playerColor;
  }
}
