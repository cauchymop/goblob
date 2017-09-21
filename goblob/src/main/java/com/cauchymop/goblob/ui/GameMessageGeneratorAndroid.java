package com.cauchymop.goblob.ui;


import android.content.Context;
import android.content.res.Resources;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.presenter.GameMessageGenerator;

import javax.inject.Inject;

public class GameMessageGeneratorAndroid implements GameMessageGenerator {

  private final Resources resources;

  @Inject
  public GameMessageGeneratorAndroid(Context context) {
    resources = context.getResources();
  }

  @Override
  public String getGameResignedMessage(String winnerName) {
    return resources.getString(R.string.end_of_game_resigned_message, winnerName);
  }

  @Override
  public String getEndOfGameMessage(String winnerName, float wonBy) {
    return resources.getString(R.string.end_of_game_message, winnerName, wonBy);
  }

  @Override
  public String getStoneMarkingMessage() {
    return resources.getString(R.string.marking_message);
  }

  @Override
  public String getOpponentPassedMessage(String opponentName) {
    return resources.getString(R.string.opponent_passed_message, opponentName);
  }

  @Override
  public String getConfigurationMessageInitial() {
    return resources.getString(R.string.configuration_message_initial);
  }

  @Override
  public String getConfigurationMessageAcceptOrChange() {
    return resources.getString(R.string.configuration_message_accept_or_change);
  }

  @Override
  public String getConfigurationMessageWaitingForOpponent() {
    return resources.getString(R.string.configuration_message_waiting_for_opponent);
  }

}
