package com.cauchymop.goblob.presenters;

import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.Move;
import com.cauchymop.goblob.proto.PlayGameData.Score;
import com.cauchymop.goblob.views.GameView;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GamePresenterTest {

  private static final GameDatas GAME_DATAS = new GameDatas("12345");

  @Mock
  GameView gameView;

  @Mock
  GoGameController goGameController;

  @Mock
  GoGame goGame;

  private PlayGameData.GameData gameData;
  private GamePresenter gamePresenter;
  private PlayGameData.GoPlayer blackPlayer;
  private PlayGameData.GoPlayer whitePlayer;

  @Before
  public void setUp() throws Exception {
    blackPlayer = GAME_DATAS.createGamePlayer("pipo", "player1");
    whitePlayer = GAME_DATAS.createGamePlayer("bimbo", "player2");
    gameData = GAME_DATAS.createNewGameData("pizza", PlayGameData.GameType.LOCAL, blackPlayer, whitePlayer).toBuilder().setPhase(Phase.IN_GAME).build();

    when(goGameController.getCurrentPlayer()).thenReturn(blackPlayer);
    when(goGameController.getCurrentColor()).thenReturn(PlayGameData.Color.BLACK);
    when(goGameController.getPhase()).thenReturn(Phase.IN_GAME);
    when(goGameController.getGame()).thenReturn(goGame);
    when(goGameController.getPlayerForColor(PlayGameData.Color.BLACK)).thenReturn(blackPlayer);
    when(goGameController.getPlayerForColor(PlayGameData.Color.WHITE)).thenReturn(whitePlayer);

    gamePresenter = new GamePresenter(GAME_DATAS);
  }

  @Test
  public void startPresenting_initMenu_canUndo_canRedo_canResign() throws Exception {
    when(goGameController.canUndo()).thenReturn(true);
    when(goGameController.canRedo()).thenReturn(true);
    when(goGameController.isLocalTurn()).thenReturn(true);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).updateMenu(true, true, true);
  }

  @Test
  public void startPresenting_initMenu_cannotUndo_cannotRedo_cannotResign() throws Exception {
    when(goGameController.canUndo()).thenReturn(false);
    when(goGameController.canRedo()).thenReturn(false);
    when(goGameController.isLocalTurn()).thenReturn(false);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).updateMenu(false, false, false);
  }

  @Test
  public void startPresenting_initsGoboardView() throws Exception {
    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).initGoBoardView(goGameController);
  }

  @Test
  public void startPresenting_displaysPlayerInfo() throws Exception {
    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).showCurrentPlayerInfo(goGameController.getCurrentPlayer().getName(), goGameController.getCurrentColor());
  }

  @Test
  public void startPresenting_doesNotUnlocksAchievement_ifGameNotFinised() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(false);
    ArrayList<GamePresenter.Achievement> expectedAchievementsList = Lists.newArrayList();

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).unlockAchievements(eq(expectedAchievementsList));
  }

  @Test
  public void startPresenting_unlocks_9x9_achievement_if_achieved() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getScore()).thenReturn(Score.getDefaultInstance());
    when(goGameController.getGame().getBoardSize()).thenReturn(9);
    when(goGameController.isLocalGame()).thenReturn(true);
    ArrayList<GamePresenter.Achievement> expectedAchievementsList = Lists.newArrayList();
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_9X9);
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_LOCAL);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).unlockAchievements(eq(expectedAchievementsList));
  }

  @Test
  public void startPresenting_unlocks_13x13_achievement_if_achieved() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getScore()).thenReturn(Score.getDefaultInstance());
    when(goGameController.getGame().getBoardSize()).thenReturn(13);
    when(goGameController.isLocalGame()).thenReturn(true);
    ArrayList<GamePresenter.Achievement> expectedAchievementsList = Lists.newArrayList();
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_13X13);
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_LOCAL);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).unlockAchievements(eq(expectedAchievementsList));
  }

  @Test
  public void startPresenting_unlocks_19x19_achievement_if_achieved() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getScore()).thenReturn(Score.getDefaultInstance());
    when(goGameController.getGame().getBoardSize()).thenReturn(19);
    when(goGameController.isLocalGame()).thenReturn(true);
    ArrayList<GamePresenter.Achievement> expectedAchievementsList = Lists.newArrayList();
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_19X19);
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_LOCAL);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).unlockAchievements(eq(expectedAchievementsList));
  }

  @Test
  public void startPresenting_unlocks_remote_achievement_if_achieved() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getScore()).thenReturn(Score.getDefaultInstance());
    when(goGameController.getGame().getBoardSize()).thenReturn(0);
    when(goGameController.isLocalGame()).thenReturn(true);
    ArrayList<GamePresenter.Achievement> expectedAchievementsList = Lists.newArrayList();
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_LOCAL);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).unlockAchievements(eq(expectedAchievementsList));
  }

  @Test
  public void startPresenting_unlocks_local_achievement_if_achieved() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getScore()).thenReturn(Score.getDefaultInstance());
    when(goGameController.getGame().getBoardSize()).thenReturn(0);
    when(goGameController.isLocalGame()).thenReturn(false);
    ArrayList<GamePresenter.Achievement> expectedAchievementsList = Lists.newArrayList();
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_REMOTE);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).unlockAchievements(eq(expectedAchievementsList));
  }

  @Test
  public void startPresenting_unlocks_winner_achievement_if_achieved() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getScore()).thenReturn(Score.getDefaultInstance());
    when(goGameController.getGame().getBoardSize()).thenReturn(19);
    when(goGameController.isLocalGame()).thenReturn(false);
    when(goGameController.getWinner()).thenReturn(blackPlayer);
    when(goGameController.isLocalPlayer(blackPlayer)).thenReturn(true);
    ArrayList<GamePresenter.Achievement> expectedAchievementsList = Lists.newArrayList();
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_19X19);
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_REMOTE);
    expectedAchievementsList.add(GamePresenter.Achievement.ACHIEVEMENTS_WINNER);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).unlockAchievements(eq(expectedAchievementsList));
  }

  @Test
  public void startPresenting_initActionsButton_toPass_whenInGame() throws Exception {
    when(goGameController.getPhase()).thenReturn(Phase.IN_GAME);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).initActionButton(GamePresenter.GameActionButtonType.PASS);
  }

  @Test
  public void startPresenting_initActionsButton_toDone_whenMarking() throws Exception {
    when(goGameController.getPhase()).thenReturn(Phase.DEAD_STONE_MARKING);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).initActionButton(GamePresenter.GameActionButtonType.DONE);
  }

  @Test
  public void startPresenting_hidesActionsButton_forPhase_UNKNOWN() throws Exception {
    verifyActionButtonIsHiddenForPhase(Phase.UNKNOWN, goGameController, gameView);
  }

  @Test
  public void startPresenting_hidesActionsButton_forPhase_INITIAL() throws Exception {
    verifyActionButtonIsHiddenForPhase(Phase.INITIAL, goGameController, gameView);
  }

  @Test
  public void startPresenting_hidesActionsButton_forPhase_CONFIGURATION() throws Exception {
    verifyActionButtonIsHiddenForPhase(Phase.CONFIGURATION, goGameController, gameView);
  }

  @Test
  public void startPresenting_hidesActionsButton_forPhase_FINISHED() throws Exception {
    verifyActionButtonIsHiddenForPhase(Phase.FINISHED, goGameController, gameView);
  }

  @Test
  public void startPresenting_displaysWinner_whenGameFinished() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getPhase()).thenReturn(Phase.FINISHED);
    Score winningScore = Score.getDefaultInstance().toBuilder().setWinner(PlayGameData.Color.BLACK).setWonBy(3.5f).build();
    when(goGameController.getScore()).thenReturn(winningScore);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).showPlayerWonMessage(blackPlayer.getName(), winningScore.getWonBy());
  }

  @Test
  public void startPresenting_displaysWinnerAndResignedMessage_whenGameFinishedByResign() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(true);
    when(goGameController.getPhase()).thenReturn(Phase.FINISHED);
    Score resignedScore = Score.getDefaultInstance().toBuilder().setResigned(true).setWinner(PlayGameData.Color.WHITE).setWonBy(0).build();
    when(goGameController.getScore()).thenReturn(resignedScore);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).showPlayerResignedMessage(whitePlayer.getName());
  }

  @Test
  public void startPresenting_displaysMarkingStoneMessage_whenMarkingDeadStones() throws Exception {
    when(goGameController.getPhase()).thenReturn(Phase.DEAD_STONE_MARKING);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).showMarkingDeadStonesMessage();
  }

  @Test
  public void startPresenting_displaysPassedMessage_whenPlayerPassed() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(false);
    when(goGameController.getPhase()).thenReturn(Phase.IN_GAME);
    when(goGame.isLastMovePass()).thenReturn(true);
    when(goGameController.getOpponent()).thenReturn(whitePlayer);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).showPlayerPassedMessage(whitePlayer.getName());
  }

  @Test
  public void startPresenting_displaysNoMessage_whenInGameAndNormalMovePlayed() throws Exception {
    when(goGameController.isGameFinished()).thenReturn(false);
    when(goGameController.getPhase()).thenReturn(Phase.IN_GAME);
    when(goGame.isLastMovePass()).thenReturn(false);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).clearMessage();
  }

  @Test
  public void onUndoSelected_canUndo_endTurn() throws Exception {
    when(goGameController.undo()).thenReturn(true);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onUndoSelected();

    verify(goGameController).undo();
    verify(goGameController).buildGameData();
    verify(gameView).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onUndoSelected_cannotUndo_doesNothing() throws Exception {
    when(goGameController.undo()).thenReturn(false);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onUndoSelected();

    verify(goGameController).undo();
    verify(goGameController, never()).buildGameData();
    verify(gameView, never()).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onRedoSelected_canRedo_endTurn() throws Exception {
    when(goGameController.redo()).thenReturn(true);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onRedoSelected();

    verify(goGameController).redo();
    verify(goGameController).buildGameData();
    verify(gameView).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onRedoSelected_cannotRedo_doesNothing() throws Exception {
    when(goGameController.redo()).thenReturn(false);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onRedoSelected();

    verify(goGameController).redo();
    verify(goGameController, never()).buildGameData();
    verify(gameView, never()).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onResignSelected_resignsAndEndTurn() throws Exception {
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onResignSelected();

    verify(goGameController).buildGameData();
    verify(goGameController).resign();
    verify(gameView).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onMovePlayed_playsGivenMove_whenValid() throws Exception {
    Move expectedMove = GAME_DATAS.createMove(1, 1);
    when(goGameController.playMoveOrToggleDeadStone(expectedMove)).thenReturn(true);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onMovePlayed(1, 1);

    verify(goGameController).playMoveOrToggleDeadStone(expectedMove);
    verify(gameView, never()).buzz();
    verify(goGameController).buildGameData();
    verify(gameView).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onMovePlayed_doesNotPlaysGivenMoveAndBuzz_whenInvalid() throws Exception {
    Move move = GAME_DATAS.createMove(1, 1);
    when(goGameController.playMoveOrToggleDeadStone(move)).thenReturn(false);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onMovePlayed(1, 1);

    verify(goGameController).playMoveOrToggleDeadStone(move);
    verify(gameView).buzz();

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onActionButtonClicked_inGame_passes() throws Exception {
    Move passMove = GAME_DATAS.createPassMove();
    when(goGameController.getPhase()).thenReturn(Phase.IN_GAME);
    when(goGameController.playMoveOrToggleDeadStone(any(Move.class))).thenReturn(true);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.onActionButtonClicked();

    // For some reason, my verify times(2) in startPresenterAndVerifyInteractions
    // is forgotten when verifying one extra, so needs to verify 2 + 1 here.
    verify(goGameController, times(3)).getPhase();
    verify(goGameController).playMoveOrToggleDeadStone(passMove);
    verify(goGameController).buildGameData();
    verify(gameView).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onActionButtonClicked_whenMarkingStones_validatesMarkingAndEndTurn() throws Exception {
    when(goGameController.getPhase()).thenReturn(Phase.DEAD_STONE_MARKING);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(Phase.DEAD_STONE_MARKING);

    gamePresenter.onActionButtonClicked();

    // For some reason, my verify times(2) in startPresenterAndVerifyInteractions
    // is forgotten when verifying one extra, so needs to verify 2 + 1 here.
    verify(goGameController, times(3)).getPhase();
    verify(goGameController).markingTurnDone();
    verify(goGameController).buildGameData();
    verify(gameView).endTurn(gameData);

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  @Test
  public void onActionButtonClicked_doesNothing_forPhase_UNKNOWN() throws Exception {
    verifyActionButtonDoesNothingForPhase(Phase.UNKNOWN);
  }

  @Test
  public void onActionButtonClicked_doesNothing_forPhase_INITIAL() throws Exception {
    verifyActionButtonDoesNothingForPhase(Phase.INITIAL);
  }

  @Test
  public void onActionButtonClicked_doesNothing_forPhase_CONFIGURATION() throws Exception {
    verifyActionButtonDoesNothingForPhase(Phase.CONFIGURATION);
  }

  @Test
  public void onActionButtonClicked_doesNothing_forPhase_FINISHED() throws Exception {
    verifyActionButtonDoesNothingForPhase(Phase.FINISHED);
  }

  @Test
  public void stopPresenting_cleansBoardView() throws Exception {
    startPresenterAndVerifyInteractions(Phase.IN_GAME);

    gamePresenter.stopPresenting();

    verify(gameView).cleanBoardView();
  }

  private void startPresenterAndVerifyInteractions(Phase phase) {
    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).updateMenu(anyBoolean(), anyBoolean(), anyBoolean());
    verify(gameView).initGoBoardView(goGameController);
    verify(gameView).showCurrentPlayerInfo(anyString(), any(PlayGameData.Color.class));
    verify(gameView).unlockAchievements(anyList());

    switch (phase) {
      case IN_GAME:
        verify(gameView).initActionButton(any(GamePresenter.GameActionButtonType.class));
        verify(gameView).clearMessage();
        break;
      case DEAD_STONE_MARKING:
        verify(gameView).initActionButton(any(GamePresenter.GameActionButtonType.class));
        verify(gameView).showMarkingDeadStonesMessage();
        break;
      default:
        verify(gameView).hideActionButton();
        verify(gameView).clearMessage();
        break;
    }

    verify(goGameController).canUndo();
    verify(goGameController).canRedo();
    verify(goGameController).isLocalTurn();
    verify(goGameController).getCurrentPlayer();
    verify(goGameController).getCurrentColor();
    verify(goGameController, times(2)).isGameFinished();
    verify(goGameController, times(2)).getPhase();
    switch (phase) {
      case IN_GAME:
        verify(goGameController).getGame();
        verify(goGame).isLastMovePass();
      default:
        break;
    }

    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }

  private void verifyActionButtonIsHiddenForPhase(Phase phase, GoGameController goGameController, GameView gameView) {
    when(goGameController.getPhase()).thenReturn(phase);

    gamePresenter.startPresenting(goGameController, gameView);

    verify(gameView).hideActionButton();
  }

  private void verifyActionButtonDoesNothingForPhase(Phase phase) {
    when(goGameController.getPhase()).thenReturn(phase);
    when(goGameController.buildGameData()).thenReturn(gameData);
    startPresenterAndVerifyInteractions(phase);

    gamePresenter.onActionButtonClicked();

    // For some reason, my verify times(2) in startPresenterAndVerifyInteractions
    // is forgotten when verifying one extra, so needs to verify 2 + 1 here.
    verify(goGameController, times(3)).getPhase();
    verifyNoMoreInteractions(gameView, goGameController, goGame);
  }
}