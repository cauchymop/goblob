package com.cauchymop.goblob.viewmodel;

import com.cauchymop.goblob.proto.PlayGameData;

public class ConfigurationViewModel {

  private final double komi;
  private final int boardSize;
  private final int handicap;
  private final String blackPlayerName;
  private final String whitePlayerName;
  private final String configurationMessage;
  private final boolean interactionsEnabled;

  public ConfigurationViewModel(PlayGameData.GameConfiguration gameConfiguration,
      String configurationMessage, boolean interactionsEnabled) {
    this.komi = gameConfiguration.getKomi();
    this.boardSize = gameConfiguration.getBoardSize();
    this.handicap = gameConfiguration.getHandicap();
    this.blackPlayerName = gameConfiguration.getBlack().getName();
    this.whitePlayerName = gameConfiguration.getWhite().getName();
    this.configurationMessage = configurationMessage;
    this.interactionsEnabled = interactionsEnabled;
  }

  public double getKomi() {
    return komi;
  }

  public int getBoardSize() {
    return boardSize;
  }

  public int getHandicap() {
    return handicap;
  }

  public String getBlackPlayerName() {
    return blackPlayerName;
  }

  public String getWhitePlayerName() {
    return whitePlayerName;
  }

  public String getConfigurationMessage() {
    return configurationMessage;
  }

  public boolean isInteractionsEnabled() {
    return interactionsEnabled;
  }
}
