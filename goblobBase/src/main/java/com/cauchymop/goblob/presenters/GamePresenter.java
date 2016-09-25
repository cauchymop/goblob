package com.cauchymop.goblob.presenters;

import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.views.GameView;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GamePresenter {

  private GameDatas gameDatas;
  private GoGameController goGameController;
  private GameView gameView;

  public enum Achievement {
    ACHIEVEMENTS_13X13,
    ACHIEVEMENTS_19X19,
    ACHIEVEMENTS_LOCAL,
    ACHIEVEMENTS_REMOTE,
    ACHIEVEMENTS_WINNER,
    ACHIEVEMENTS_9X9
  }

  public enum GameActionButtonType {
    PASS,
    DONE
  }

  @Inject
  public GamePresenter(GameDatas gameDatas) {
    this.gameDatas = gameDatas;
  }

  public void startPresenting(GoGameController goGameController, GameView gameView) {
    this.goGameController = goGameController;
    this.gameView = gameView;
    gameView.updateMenu(this.goGameController.canUndo(), this.goGameController.canRedo(), this.goGameController.isLocalTurn());
    gameView.initGoBoardView(this.goGameController);
    gameView.showCurrentPlayerInfo(this.goGameController.getCurrentPlayer().getName(), this.goGameController.getCurrentColor());
    gameView.unlockAchievements(getAchievements());
    initActionButton();
    initMessageArea();
  }

  public void stopPresenting() {
    gameView.cleanBoardView();
  }

  public void onUndoSelected() {
    if (goGameController.undo()) {
      gameView.endTurn(goGameController.buildGameData());
    }
  }

  public void onRedoSelected() {
    if (goGameController.redo()) {
      gameView.endTurn(goGameController.buildGameData());
    }
  }

  public void onResignSelected() {
    goGameController.resign();
    gameView.endTurn(goGameController.buildGameData());
  }

  public void onMovePlayed(int x, int y) {
    play(gameDatas.createMove(x, y));
  }

  public void onActionButtonClicked() {
    switch(goGameController.getPhase()) {
      case IN_GAME:
        play(gameDatas.createPassMove());
        break;
      case DEAD_STONE_MARKING:
        goGameController.markingTurnDone();
        gameView.endTurn(goGameController.buildGameData());
        break;
      default:
        break;
    }
  }

  private List<Achievement> getAchievements() {
    ArrayList<Achievement> achievements = Lists.newArrayList();
    if (!goGameController.isGameFinished()) {
      return achievements;
    }
    switch (goGameController.getGame().getBoardSize()) {
      case 9:
        achievements.add(Achievement.ACHIEVEMENTS_9X9);
        break;
      case 13:
        achievements.add(Achievement.ACHIEVEMENTS_13X13);
        break;
      case 19:
        achievements.add(Achievement.ACHIEVEMENTS_19X19);
        break;
    }
    if (goGameController.isLocalGame()) {
      achievements.add(Achievement.ACHIEVEMENTS_LOCAL);
    } else {
      achievements.add(Achievement.ACHIEVEMENTS_REMOTE);
      if (goGameController.isLocalPlayer(goGameController.getWinner())) {
        achievements.add(Achievement.ACHIEVEMENTS_WINNER);
      }
    }
    return achievements;
  }

  private void initMessageArea() {
    if (goGameController.isGameFinished()) {
      PlayGameData.Score score = goGameController.getScore();
      if (score.getResigned()) {
        gameView.showPlayerResignedMessage(goGameController.getPlayerForColor(score.getWinner()).getName());
      } else {
        gameView.showPlayerWonMessage(goGameController.getPlayerForColor(score.getWinner()).getName(), score.getWonBy());
      }
    } else {
      PlayGameData.GameData.Phase phase = goGameController.getPhase();
      if (phase == PlayGameData.GameData.Phase.DEAD_STONE_MARKING) {
        gameView.showMarkingDeadStonesMessage();
      } else if (phase == PlayGameData.GameData.Phase.IN_GAME && goGameController.getGame().isLastMovePass()) {
        gameView.showPlayerPassedMessage(goGameController.getOpponent().getName());
      } else {
        gameView.clearMessage();
      }
    }
  }

  private void initActionButton() {
    switch(goGameController.getPhase()) {
      case IN_GAME:
        gameView.initActionButton(GameActionButtonType.PASS);
        break;
      case DEAD_STONE_MARKING:
        gameView.initActionButton(GameActionButtonType.DONE);
        break;
      default:
        gameView.hideActionButton();
        break;
    }
  }

  private void play(PlayGameData.Move move) {
    boolean played = goGameController.playMoveOrToggleDeadStone(move);
    if (played) {
      gameView.endTurn(goGameController.buildGameData());
    } else {
      gameView.buzz();
    }
  }

}
