package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

public class ConfigurationViewModel {

  public enum ConfigurationMessage {
    INITIAL,
    ACCEPT_OR_CHANGE,
    WAITING_FOR_OPPONENT
  }

  private final double komi;
  private final int boardSize;
  private final int handicap;
  private final String blackPlayerName;
  private final String whitePlayerName;
  private final ConfigurationMessage configurationMessage;
  private final boolean interactionsEnabled;

  public ConfigurationViewModel(PlayGameData.GameConfiguration gameConfiguration,
      ConfigurationMessage configurationMessage, boolean interactionsEnabled) {
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

  public ConfigurationMessage getConfigurationMessage() {
    return configurationMessage;
  }

  public boolean isInteractionsEnabled() {
    return interactionsEnabled;
  }
}
