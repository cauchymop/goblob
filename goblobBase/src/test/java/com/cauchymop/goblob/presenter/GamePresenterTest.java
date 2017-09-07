package com.cauchymop.goblob.presenter;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.view.GameView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.cauchymop.goblob.model.TestDataHelperKt.createGameData;
import static com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.INITIAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class GamePresenterTest {
  private static final GameDatas GAME_DATAS = new GameDatas();

  @Mock private Analytics analytics;
  @Mock private GameRepository gameRepository;
  @Mock private AchievementManager achievementManager;

  @Mock private GameView view;
  @Mock private GameViewUpdater gameViewUpdater;

  private GamePresenter gamePresenter;

  @Before
  public void setUp() throws Exception {
    gamePresenter = new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, gameViewUpdater, view);

    reset(gameRepository, view);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(analytics, gameRepository, achievementManager, view, gameViewUpdater);
  }

  @Test
  public void initialisation_registersListeners() {
    GamePresenter presenter = new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, gameViewUpdater, view);
    verify(gameRepository).addGameRepositoryListener(presenter);
    verify(view).setConfigurationViewListener(presenter);
    verify(view).setInGameActionListener(presenter);
  }

  @Test
  public void gameListChanged_doesNothing() throws Exception {
    gamePresenter.gameListChanged();

    // Does nothing.
  }

  @Test
  public void gameChanged_withNoGameSelected_DoesNothing() throws Exception {

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").setPhase(INITIAL).build());

    // Does Nothing.
  }

  @Test
  public void gameChanged_withDifferentMatchId_doesNothing() throws Exception {
    gamePresenter.gameChanged(createGameData().setMatchId("pizza").build());
    reset(view);

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").build());

    // Does nothing.
  }

  @Test
  public void gameChanged_withSameMatchId() throws Exception {
    setInitialGame("pizza");

    gamePresenter.gameChanged(createGameData().setMatchId("pizza").setPhase(INITIAL).build());

    verify(gameViewUpdater).update(any(), eq(view));
  }

  @Test
  public void gameSelected_updatesView() throws Exception {

    gamePresenter.gameSelected(createGameData().setMatchId("pipo").setPhase(INITIAL).build());

    verify(gameViewUpdater).update(any(), eq(view));
  }

//
//  @Test
//  public void clear() throws Exception {
//  }
//
//  @Test
//  public void onBlackPlayerNameChanged() throws Exception {
//  }
//
//  @Test
//  public void onWhitePlayerNameChanged() throws Exception {
//  }
//
//  @Test
//  public void onHandicapChanged() throws Exception {
//  }
//
//  @Test
//  public void onKomiChanged() throws Exception {
//  }
//
//  @Test
//  public void onBoardSizeChanged() throws Exception {
//  }
//
//  @Test
//  public void onSwapEvent() throws Exception {
//  }
//
//  @Test
//  public void onConfigurationValidationEvent() throws Exception {
//  }
//
//  @Test
//  public void onIntersectionSelected() throws Exception {
//  }
//
//  @Test
//  public void onPass() throws Exception {
//  }
//
//  @Test
//  public void onDone() throws Exception {
//  }
//
//  @Test
//  public void onUndo() throws Exception {
//  }
//
//  @Test
//  public void onRedo() throws Exception {
//  }
//
//  @Test
//  public void onResign() throws Exception {
//  }

  private void setInitialGame(String matchId) {
    gamePresenter.gameSelected(createGameData().setMatchId(matchId).build());
    reset(view, gameViewUpdater);
  }

}