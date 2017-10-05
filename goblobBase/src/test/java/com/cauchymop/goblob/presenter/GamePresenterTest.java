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
  @Mock private FeedbackSender feedbackSender;

  private GamePresenter gamePresenter;

  @Before
  public void setUp() throws Exception {
    gamePresenter = new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, feedbackSender, gameViewUpdater);
    reset(gameRepository);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(analytics, gameRepository, achievementManager, feedbackSender, gameViewUpdater, view);
  }

  @Test
  public void initialisation_registersAsGameRepositoryListener() {
    GamePresenter presenter = new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, feedbackSender, gameViewUpdater);

    verify(gameRepository).addGameRepositoryListener(presenter);
  }

  @Test
  public void setView_registersViewListenersAndUpdatesView() {
    gamePresenter.setView(view);

    verify(view).setConfigurationViewListener(gamePresenter);
    verify(view).setInGameActionListener(gamePresenter);
    verify(gameViewUpdater).update(null, view);
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
    gamePresenter.setView(view);
    reset(view, gameViewUpdater);
    gamePresenter.gameChanged(createGameData().setMatchId("pizza").build());

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").build());

    // Does nothing.
  }

  @Test
  public void gameChanged_withSameMatchId() throws Exception {
    gamePresenter.setView(view);
    setInitialGame("pizza");

    gamePresenter.gameChanged(createGameData().setMatchId("pizza").setPhase(INITIAL).build());

    verify(gameViewUpdater).update(any(), eq(view));
  }

  @Test
  public void gameSelected_updatesView() throws Exception {
    gamePresenter.setView(view);
    reset(view, gameViewUpdater);

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