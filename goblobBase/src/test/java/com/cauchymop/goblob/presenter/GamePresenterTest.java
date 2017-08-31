package com.cauchymop.goblob.presenter;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.view.GameView;
import com.cauchymop.goblob.viewmodel.ConfigurationViewModels;
import com.cauchymop.goblob.viewmodel.InGameViewModels;

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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GamePresenterTest {
  private static final GameDatas GAME_DATAS = new GameDatas();
  private static final String CONFIGURATION_INITIAL_MESSAGE = "configuration initial message";

  @Mock private Analytics analytics;
  @Mock private GameRepository gameRepository;
  @Mock private AchievementManager achievementManager;
  @Mock private GameMessageGenerator gameMessageGenerator;
  @Mock private GameView view;

  private GamePresenter gamePresenter;
  private ConfigurationViewModels configurationViewModels;
  private InGameViewModels inGameViewModels;

  @Before
  public void setUp() throws Exception {
    configurationViewModels = new ConfigurationViewModels(gameMessageGenerator);
    inGameViewModels = new InGameViewModels(GAME_DATAS, gameMessageGenerator);

    gamePresenter = new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, configurationViewModels, inGameViewModels, view);

    reset(gameRepository);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(analytics, gameRepository, achievementManager, gameMessageGenerator, view);
  }

  @Test
  public void initialisation_fetchInfoFromRepositoryAndUpdateView() {
    GamePresenter presenter = new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, configurationViewModels, inGameViewModels, view);
    verify(gameRepository).addGameRepositoryListener(presenter);
  }

  @Test
  public void gameListChanged_doesNothing() throws Exception {
    gamePresenter.gameListChanged();

    // Does nothing.
  }

  @Test
  public void gameChanged_withDifferentMatchId_doesNothing() throws Exception {
    gamePresenter.gameChanged(createGameData().setMatchId("pizza").build());
    reset(view, gameMessageGenerator);

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").build());

    // Does nothing.
  }

  @Test
  public void gameChanged_updateView() throws Exception {
    when(gameMessageGenerator.getConfigurationMessageInitial()).thenReturn(CONFIGURATION_INITIAL_MESSAGE);

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").setPhase(INITIAL).build());

    verify(view).setConfigurationViewListener(gamePresenter);
    verify(view).setConfigurationViewModel(any());
    verify(gameMessageGenerator).getConfigurationMessageInitial();

  }

  // TODO: All init cases here

  @Test
  public void gameChanged_withSameMatchIdAndConfigured() throws Exception {
    setInitialGame();
    when(gameMessageGenerator.getConfigurationMessageInitial()).thenReturn(CONFIGURATION_INITIAL_MESSAGE);

    gamePresenter.gameChanged(createGameData().setMatchId("pizza").setPhase(INITIAL).build());

    verify(view).setConfigurationViewListener(gamePresenter);
    verify(view).setConfigurationViewModel(any());
    verify(gameMessageGenerator).getConfigurationMessageInitial();
  }

//  @Test
//  public void gameSelected() throws Exception {
//  }
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

  private void setInitialGame() {
    when(gameMessageGenerator.getConfigurationMessageInitial()).thenReturn(CONFIGURATION_INITIAL_MESSAGE);
    gamePresenter.gameChanged(createGameData().setMatchId("pizza").build());
    reset(view, gameMessageGenerator);
  }

}