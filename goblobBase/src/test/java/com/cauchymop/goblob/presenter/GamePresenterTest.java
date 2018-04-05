package com.cauchymop.goblob.presenter;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
  private GoGameController goGameController;
  @Mock
  private FeedbackSender feedbackSender;
  @Mock
  private SingleGamePresenter singleGamePresenter;

  private GamePresenter gamePresenter;

  @Before
  public void setUp() throws Exception {
    gamePresenter = createGamePresenter();
    gamePresenter.setView(view);
    reset(view, gameViewUpdater, gameRepository);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(analytics, gameRepository, achievementManager, gameViewUpdater, view, goGameController);
  }

  @Test
  public void setView_registersAsGameRepositoryListener() {
    gamePresenter.setView(view);

    verify(gameViewUpdater).setView(view);
    verify(gameRepository).addGameSelectionListener(gamePresenter);
  }


  @Test
  public void gameSelected_updatesView() throws Exception {
    PlayGameData.GameData gameData = createGameData().setMatchId("pipo").setPhase(INITIAL).build();

    gamePresenter.gameSelected(gameData);

    verify(gameViewUpdater).setGoGameController(any());
    verify(view).setConfigurationViewListener(any());
    verify(view).setInGameActionListener(any());
    verify(goGameController).setGameData(gameData);

    verify(gameRepository).addGameChangeListener(any());
    verify(achievementManager).updateAchievements(goGameController);
    verify(gameViewUpdater).update();
  }

  @Test
  public void gameSelected_withNullGameData_noPreviousGame_doesNothing() throws Exception {

    gamePresenter.gameSelected(null);

  }

  @Test
  public void gameSelected_withNullGameData_andPreviousGame_clearsPreviousGame() throws Exception {
    setInitialGame();

    gamePresenter.gameSelected(null);

    // From SingleGamePresenter.clear()
    verify(gameRepository).removeGameChangeListener(any());

  }

  @Test
  public void gameSelected_withPreviousGame_clearPreviousGame_andUpdatesView() throws Exception {
    setInitialGame();
    PlayGameData.GameData selectedGameData = createGameData().setMatchId("pipo").setPhase(INITIAL).build();

    gamePresenter.gameSelected(selectedGameData);

    verify(gameViewUpdater).setGoGameController(any());
    verify(view).setConfigurationViewListener(any());
    verify(view).setInGameActionListener(any());
    verify(goGameController).setGameData(selectedGameData);

    verify(gameRepository).addGameChangeListener(any());
    verify(achievementManager).updateAchievements(goGameController);
    verify(gameViewUpdater).update();
    // From SingleGamePresenter.clear()
    verify(gameRepository).removeGameChangeListener(any());
  }

  @Test
  public void clear_withNoGame() throws Exception {
    gamePresenter.clear();

    verify(gameRepository).removeGameSelectionListener(gamePresenter);
    verify(view).setConfigurationViewListener(null);
    verify(view).setInGameActionListener(null);
  }

  @Test
  public void clear_withGame() throws Exception {
    setInitialGame();

    gamePresenter.clear();

    verify(gameRepository).removeGameSelectionListener(gamePresenter);
    verify(view).setConfigurationViewListener(null);
    verify(view).setInGameActionListener(null);
    // From SingleGamePresenter.clear()
    verify(gameRepository).removeGameChangeListener(any());
  }

  public void setInitialGame() {
    gamePresenter.gameSelected(createGameData().build());
    reset(gameRepository, achievementManager, gameViewUpdater, view, goGameController);
  }

  private GamePresenter createGamePresenter() {
    return new GamePresenter(GAME_DATAS,
        analytics,
        gameRepository,
        gameViewUpdater,
        feedbackSender,
        goGameController,
        singleGamePresenter);
  }
}
