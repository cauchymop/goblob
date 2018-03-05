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

  @Mock
  private Analytics analytics;
  @Mock
  private GameRepository gameRepository;
  @Mock
  private AchievementManager achievementManager;
  @Mock
  private GameView view;
  @Mock
  private GameViewUpdater gameViewUpdater;
  @Mock
  private ConfigurationViewEventProcessor configurationViewEventProcessor;
  @Mock
  private InGameViewEventProcessor inGameViewEventProcessor;
  @Mock
  private GoGameControllerFactory goGameControllerFactory;
  @Mock
  private GoGameController goGameController;
  @Mock
  private FeedbackSender feedbackSender;

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
  public void setView_registersAsGameRepositoryListener() {
    gamePresenter.setView(view);

    verify(gameViewUpdater).setView(view);
    verify(gameRepository).addGameSelectionListener(gamePresenter);
  }


  @Test
  public void gameSelected_updatesView() throws Exception {

    gamePresenter.gameSelected(createGameData().setMatchId("pipo").setPhase(INITIAL).build());

    verify(gameViewUpdater).setGoGameController(any());
    verify(view).setConfigurationViewListener(any());
    verify(view).setInGameActionListener(any());

    verify(gameRepository).addGameChangeListener(any());
    verify(achievementManager).updateAchievements(goGameController);
    verify(gameViewUpdater).update();
  }

  @Test
  public void clear() throws Exception {
    gamePresenter.clear();

    verify(gameRepository).removeGameSelectionListener(gamePresenter);
    verify(view).setConfigurationViewListener(null);
    verify(view).setInGameActionListener(null);
  }

  private void setInitialGame() {
    when(goGameController.getMatchId()).thenReturn("pizza");
    gamePresenter.gameSelected(createGameData().setMatchId("pizza").build());
    reset(view, gameViewUpdater, achievementManager);
  }

  private GamePresenter createGamePresenter() {
    return new GamePresenter(GAME_DATAS,
        analytics,
        gameRepository,
        achievementManager,
        gameViewUpdater,
        feedbackSender,
        goGameControllerFactory);
  }
}
