package com.cauchymop.goblob.presenter;


public interface GameMessageGenerator {
  String getGameResignedMessage(String winnerName);

  String getEndOfGameMessage(String winnerName, float wonBy);

  String getStoneMarkingMessage();

  String getOpponentPassedMessage(String opponentName);

  String getConfigurationMessageInitial();

  String getConfigurationMessageAcceptOrChange();

  String getConfigurationMessageWaitingForOpponent();
}
