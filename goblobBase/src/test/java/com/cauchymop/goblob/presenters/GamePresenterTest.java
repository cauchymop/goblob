package com.cauchymop.goblob.presenters;

import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.views.GameView;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GamePresenterTest {

  private static final GameDatas GAME_DATAS = new GameDatas("12345");

  @Mock
  GameView gameView;

  private GameData gameData;

  private GamePresenter gamePresenter;

  @Before
  public void setUp() throws Exception {
    PlayGameData.GoPlayer black = GAME_DATAS.createGamePlayer("pipo", "player1");
    PlayGameData.GoPlayer white = GAME_DATAS.createGamePlayer("bimbo", "player2");
    gameData = GAME_DATAS.createNewGameData("pizza", PlayGameData.GameType.LOCAL, black, white).toBuilder().setPhase(GameData.Phase.IN_GAME).build();
    gamePresenter = new GamePresenter(GAME_DATAS);
  }

  @Test
  public void startPresenting_initMenu_canUndo_canRedo_canResign() throws Exception {
    PlayGameData.Move move1 = GAME_DATAS.createMove(2, 3);
    PlayGameData.Move move2 = GAME_DATAS.createMove(4, 5);
    PlayGameData.Move move3 = GAME_DATAS.createMove(6, 7);
    gameData = gameData.toBuilder().addAllMove(ImmutableList.of(move1, move2)).addRedo(move3).build();
    gamePresenter = new GamePresenter(GAME_DATAS);

    gamePresenter.startPresenting(gameData, gameView);

    verify(gameView).updateMenu(true, true, true);
  }

//  This case is not possible, test all false instead
//  @Test
//  public void startPresenting_initMenu_cannotUndo_canRedo_canResign() throws Exception {
//    PlayGameData.Move move1 = GAME_DATAS.createMove(2, 3);
//    PlayGameData.Move move2 = GAME_DATAS.createMove(4, 5);
//    PlayGameData.Move move3 = GAME_DATAS.createMove(6, 7);
//    gameData = gameData.toBuilder().addAllMove(ImmutableList.of(move1, move2)).addRedo(move3).build();
//
//    gamePresenter.startPresenting(gameData, gameView);
//
//    verify(gameView).updateMenu(false, true, true);
//  }

  @Test
  public void startPresenting_initMenu_canUndo_cannotRedo_canResign() throws Exception {
    gameData = gameData.toBuilder().addAllMove(ImmutableList.of(GAME_DATAS.createMove(2, 3), GAME_DATAS.createMove(4, 5))).build();
    gamePresenter.startPresenting(gameData, gameView);

    verify(gameView).updateMenu(true, false, true);
  }

  @Test @Ignore
  public void startPresenting_initMenu_canUndo_canRedo_cannotResign() throws Exception {
    // FIXME
    PlayGameData.GameConfiguration gameConfiguration = gameData.toBuilder().getGameConfigurationBuilder().setGameType(PlayGameData.GameType.REMOTE).build();
    PlayGameData.Move move1 = GAME_DATAS.createMove(2, 3);
    PlayGameData.Move move2 = GAME_DATAS.createMove(4, 5);
    PlayGameData.Move move3 = GAME_DATAS.createMove(6, 7);
    gameData = gameData.toBuilder().setGameConfiguration(gameConfiguration).addAllMove(ImmutableList.of(move1, move2)).addRedo(move3).build();
    System.out.println(" ===> gameData = " + gameData);
    gamePresenter.startPresenting(gameData, gameView);

    verify(gameView).updateMenu(true, true, false);
  }

  @Test
  public void startPresenting_initsGoboardView() throws Exception {
    gamePresenter.startPresenting(gameData, gameView);

    verify(gameView).initGoBoardView(any(GoGameController.class), eq(gamePresenter));
  }

  @Test
  public void stopPresenting_cleansBoardView() throws Exception {
    gamePresenter.startPresenting(gameData, gameView);

    gamePresenter.stopPresenting();

    verify(gameView).cleanBoardView();
  }

  @Test @Ignore
  public void onUndoSelected() throws Exception {
    // TODO
  }

  @Test @Ignore
  public void onRedoSelected() throws Exception {
    // TODO
  }

  @Test @Ignore
  public void onResignSelected() throws Exception {
    // TODO
  }

  @Test @Ignore
  public void played() throws Exception {
    // TODO
  }

  @Test @Ignore
  public void onActionButtonClicked() throws Exception {
    // TODO
  }

}