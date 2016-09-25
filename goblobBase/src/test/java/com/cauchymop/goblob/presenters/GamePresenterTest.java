package com.cauchymop.goblob.presenters;

import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.views.GameView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
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

  private GamePresenter gamePresenter;

  @Before
  public void setUp() throws Exception {
    PlayGameData.GoPlayer black = GAME_DATAS.createGamePlayer("pipo", "player1");
    when(goGameController.getCurrentPlayer()).thenReturn(black);
    when(goGameController.getCurrentColor()).thenReturn(PlayGameData.Color.BLACK);
    when(goGameController.getPhase()).thenReturn(PlayGameData.GameData.Phase.IN_GAME);
    when(goGameController.getGame()).thenReturn(goGame);
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
  public void stopPresenting_cleansBoardView() throws Exception {
    gamePresenter.startPresenting(goGameController, gameView);

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