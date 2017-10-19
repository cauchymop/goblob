package com.cauchymop.goblob.presenter;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.GoGameController;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GamePresenterTest {
  private static final GameDatas GAME_DATAS = new GameDatas();

  @Mock private Analytics analytics;
  @Mock private GameRepository gameRepository;
  @Mock private AchievementManager achievementManager;
  @Mock private GameView view;
  @Mock private GameViewUpdater gameViewUpdater;
  @Mock private ConfigurationViewEventProcessor configurationViewEventProcessor;
  @Mock private InGameViewEventProcessor inGameViewEventProcessor;
  @Mock private GoGameControllerFactory goGameControllerFactory;
  @Mock private GoGameController goGameController;

  private GamePresenter gamePresenter;


  @Before
  public void setUp() throws Exception {
    when(goGameControllerFactory.createGameController(eq(GAME_DATAS), any(), eq(analytics))).thenReturn(goGameController);
    gamePresenter = createGamePresenter();
    gamePresenter.setView(view);
    reset(view, gameViewUpdater, gameRepository);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(analytics, gameRepository, achievementManager, gameViewUpdater, view /*goGameControllerFactory, goGameController*/);
  }

  @Test
  public void initialisation_registersAsGameRepositoryListener() {
    GamePresenter presenter = createGamePresenter();

    verify(gameRepository).addGameRepositoryListener(presenter);
  }

  @Test
  public void setView_registersViewListenersAndUpdatesView() {
    gamePresenter.setView(view);

    verify(view).setConfigurationViewListener(configurationViewEventProcessor);
    verify(view).setInGameActionListener(inGameViewEventProcessor);
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
    gamePresenter.gameChanged(createGameData().setMatchId("pizza").build());

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").build());

    // Does nothing.
  }

  @Test
  public void gameChanged_withSameMatchId() throws Exception {
    setInitialGame();

    gamePresenter.gameChanged(createGameData().setMatchId("pizza").setPhase(INITIAL).build());

    verify(gameViewUpdater).update(any(), eq(view));
  }

  @Test
  public void gameSelected_updatesView() throws Exception {

    gamePresenter.gameSelected(createGameData().setMatchId("pipo").setPhase(INITIAL).build());

    verify(gameViewUpdater).update(goGameController, view);
  }

  @Test
  public void clear() throws Exception {
    gamePresenter.clear();

    verify(gameRepository).removeGameRepositoryListener(gamePresenter);
  }

  @Test
  public void onUndo() throws Exception {
    when(goGameController.undo()).thenReturn(true);
    setInitialGame();

    gamePresenter.onUndo();

    verify(goGameController).undo();
    verify(gameRepository).commitGameChanges(any());
    verify(gameViewUpdater).update(goGameController, view);
    verify(analytics).undo();
  }

  @Test
  public void onRedo() throws Exception {
    when(goGameController.redo()).thenReturn(true);
    setInitialGame();

    gamePresenter.onRedo();

    verify(goGameController).redo();
    verify(gameRepository).commitGameChanges(any());
    verify(gameViewUpdater).update(goGameController, view);
    verify(analytics).redo();
  }

  @Test
  public void onResign() throws Exception {
    setInitialGame();

    gamePresenter.onResign();

    verify(goGameController).resign();
    verify(gameRepository).commitGameChanges(any());
    verify(gameViewUpdater).update(goGameController, view);
    verify(analytics).resign();
  }

  private void setInitialGame() {
    when(goGameController.getMatchId()).thenReturn("pizza");
    gamePresenter.gameSelected(createGameData().setMatchId("pizza").build());
    reset(view, gameViewUpdater);
  }

  private GamePresenter createGamePresenter() {
    return new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, gameViewUpdater, configurationViewEventProcessor, inGameViewEventProcessor, goGameControllerFactory);
  }
}
