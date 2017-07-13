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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GamePresenterTest {
  private static final GameDatas GAME_DATAS = new GameDatas();

  @Mock private Analytics analytics;
  @Mock private GameRepository gameRepository;
  @Mock private AchievementManager achievementManager;
  @Mock private ConfigurationViewModels configurationViewModels;
  @Mock private InGameViewModels inGameViewModels;
  @Mock private GameView view;

  private GamePresenter gamePresenter;

  @Before
  public void setUp() throws Exception {
    gamePresenter = new GamePresenter(GAME_DATAS, analytics, gameRepository, achievementManager, configurationViewModels, inGameViewModels, view);
    Mockito.verify(gameRepository).addGameRepositoryListener(gamePresenter);
  }

  @After
  public void tearDown() throws Exception {
//    Mockito.verifyNoMoreInteractions(view, inGameViewModels, configurationViewModels, achievementManager, gamePresenter, analytics);
  }

  @Test
  public void gameListChanged() throws Exception {
    gamePresenter.gameListChanged();
  }

  @Test
  public void gameChanged() throws Exception {
  }

  @Test
  public void gameSelected() throws Exception {
  }

  @Test
  public void clear() throws Exception {
  }

  @Test
  public void onBlackPlayerNameChanged() throws Exception {
  }

  @Test
  public void onWhitePlayerNameChanged() throws Exception {
  }

  @Test
  public void onHandicapChanged() throws Exception {
  }

  @Test
  public void onKomiChanged() throws Exception {
  }

  @Test
  public void onBoardSizeChanged() throws Exception {
  }

  @Test
  public void onSwapEvent() throws Exception {
  }

  @Test
  public void onConfigurationValidationEvent() throws Exception {
  }

  @Test
  public void onIntersectionSelected() throws Exception {
  }

  @Test
  public void onPass() throws Exception {
  }

  @Test
  public void onDone() throws Exception {
  }

  @Test
  public void onUndo() throws Exception {
  }

  @Test
  public void onRedo() throws Exception {
  }

  @Test
  public void onResign() throws Exception {
  }

}