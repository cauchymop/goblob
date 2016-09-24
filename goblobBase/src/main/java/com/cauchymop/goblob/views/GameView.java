package com.cauchymop.goblob.views;

import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.presenters.GamePresenter;

import java.util.List;

public interface GameView {
  void updateMenu(boolean undoMenuItemAvailable, boolean redoMenuItemAvailable, boolean resignMenuItemAvailable);

  void initGoBoardView(GoGameController goGameController, GoBoardViewListener goBoardViewListener);

  void showCurrentPlayerInfo(String currentPlayerName, PlayGameData.Color currentPlayerColor);

  void showPlayerPassedMessage(String playerName);

  void showMarkingDeadStonesMessage();

  void showPlayerWonMessage(String winnerName, float wonBy);

  void showPlayerResignedMessage(String playerName);

  void clearMessage();

  void initActionButton(GamePresenter.GameActionButtonType gameActionButtonType);

  void hideActionButton();

  void unlockAchievements(List<GamePresenter.Achievement> achievements);

  void endTurn(PlayGameData.GameData gameData);

  void buzz();

  void cleanBoardView();
}
