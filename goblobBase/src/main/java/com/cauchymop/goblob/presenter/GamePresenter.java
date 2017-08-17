package com.cauchymop.goblob.presenter;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.view.GameView;
import com.cauchymop.goblob.view.GoBoardView;
import com.cauchymop.goblob.view.InGameView;
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel;
import com.cauchymop.goblob.viewmodel.ConfigurationViewModels;
import com.cauchymop.goblob.viewmodel.InGameViewModel;
import com.cauchymop.goblob.viewmodel.InGameViewModels;

public class GamePresenter implements GoBoardView.BoardEventListener, ConfigurationEventListener, GameRepository.GameRepositoryListener, InGameView.InGameActionListener {

  private final Analytics analytics;
  private final GameRepository gameRepository;
  private final AchievementManager achievementManager;
  private final ConfigurationViewModels configurationViewModels;
  private final InGameViewModels inGameViewModels;
  private final GameView view;
  private final GameDatas gameDatas;

  private GoGameController goGameController;

  public GamePresenter(GameDatas gameDatas,
      Analytics analytics,
      GameRepository gameRepository,
      AchievementManager achievementManager,
      ConfigurationViewModels configurationViewModels,
      InGameViewModels inGameViewModels,
      final GameView view) {
    this.gameDatas = gameDatas;
    this.analytics = analytics;
    this.gameRepository = gameRepository;
    this.achievementManager = achievementManager;
    this.configurationViewModels = configurationViewModels;
    this.inGameViewModels = inGameViewModels;
    this.view = view;
    gameRepository.addGameRepositoryListener(this);
  }

  private void updateFromGame(PlayGameData.GameData gameData) {
    if (gameData == null) {
      view.setInGameActionListener(null);
      view.setConfigurationViewListener(null);
      // TODO: Introduce an empty state? self destroy?
      return;
    }
    goGameController = new GoGameController(gameDatas, gameData, analytics);
    if (isConfigured()) {
      view.setInGameViewModel(getInGameViewModel());
      view.setInGameActionListener(this);
    } else {
      view.setConfigurationViewModel(getConfigurationViewModel());
      view.setConfigurationViewListener(this);
    }

    updateAchievements();
  }

  private void updateAchievements() {
    if (!goGameController.isGameFinished()) {
      return;
    }
    switch (goGameController.getGame().getBoardSize()) {
      case 9:
        achievementManager.unlockAchievement9x9();
        break;
      case 13:
        achievementManager.unlockAchievement13x13();
        break;
      case 19:
        achievementManager.unlockAchievement19x19();
        break;
    }
    if (goGameController.isLocalGame()) {
      achievementManager.unlockAchievementLocal();
    } else {
      achievementManager.unlockAchievementRemote();
      if (goGameController.getWinner().getIsLocal()) {
        achievementManager.unlockAchievementWinner();
      }
    }
  }

  @Override
  public void gameListChanged() {
    // Nothing to do
  }

  @Override
  public void gameChanged(PlayGameData.GameData gameData) {
    if (goGameController == null || gameData.getMatchId().equals(goGameController.getMatchId())) {
      updateFromGame(gameData);
    }
  }

  @Override
  public void gameSelected(PlayGameData.GameData gameData) {
    updateFromGame(gameData);
  }

  public void clear() {
    gameRepository.removeGameRepositoryListener(this);
  }

  @Override
  public void onBlackPlayerNameChanged(String blackPlayerName) {
    if (!goGameController.getGameConfiguration().getBlack().getName().equals(blackPlayerName)) {
      goGameController.setBlackPlayerName(blackPlayerName);
    }
  }

  @Override
  public void onWhitePlayerNameChanged(String whitePlayerName) {
    if (!goGameController.getGameConfiguration().getWhite().getName().equals(whitePlayerName)) {
      goGameController.setWhitePlayerName(whitePlayerName);
    }
  }

  @Override
  public void onHandicapChanged(int handicap) {
    if (goGameController.getGameConfiguration().getHandicap() != handicap) {
      goGameController.setHandicap(handicap);
    }
  }

  @Override
  public void onKomiChanged(float komi) {
    if (goGameController.getGameConfiguration().getKomi() != komi) {
      goGameController.setKomi(komi);
    }
  }

  @Override
  public void onBoardSizeChanged(int boardSize) {
    if (goGameController.getGameConfiguration().getBoardSize() != boardSize) {
      goGameController.setBoardSize(boardSize);
    }
  }

  @Override
  public void onSwapEvent() {
    goGameController.swapPlayers();
    view.setConfigurationViewModel(getConfigurationViewModel());
  }

  @Override
  public void onConfigurationValidationEvent() {
    goGameController.validateConfiguration();
    commitGameChanges();
  }

  @Override
  public void onIntersectionSelected(int x, int y) {
    if (goGameController.isLocalTurn()) {
      boolean played = goGameController.playMoveOrToggleDeadStone(gameDatas.createMove(x, y));

      if (played) {
        commitGameChanges();
      } else {
        view.buzz();
        analytics.invalidMovePlayed(goGameController.getGameConfiguration());
      }
    }
  }

  @Override
  public void onPass() {
    goGameController.pass();
    commitGameChanges();
  }

  @Override
  public void onDone() {
    goGameController.markingTurnDone();
    commitGameChanges();
  }

  public void onUndo() {
    if (goGameController.undo()) {
      commitGameChanges();
      analytics.undo();
    }
  }

  public void onRedo() {
    if (goGameController.redo()) {
      analytics.redo();
      commitGameChanges();
    }
  }

  public void onResign() {
    goGameController.resign();
    commitGameChanges();
    analytics.resign();
  }

  private ConfigurationViewModel getConfigurationViewModel() {
    return configurationViewModels.from(goGameController);
  }

  private InGameViewModel getInGameViewModel() {
    return inGameViewModels.from(goGameController);
  }

  private boolean isConfigured() {
    switch (goGameController.getPhase()) {
      case INITIAL:
      case CONFIGURATION:
        return false;
      case IN_GAME:
      case DEAD_STONE_MARKING:
      case FINISHED:
        return true;
      default:
        throw new RuntimeException("Invalid phase for game: " + goGameController.getPhase());
    }
  }

  private void commitGameChanges() {
    PlayGameData.GameData gameData = goGameController.buildGameData();
    gameRepository.commitGameChanges(gameData);
    switch (goGameController.getPhase()) {
      case CONFIGURATION:
        view.setConfigurationViewModel(getConfigurationViewModel());
        break;
      case IN_GAME:
      case DEAD_STONE_MARKING:
      case FINISHED:
        view.setInGameViewModel(getInGameViewModel());
        break;
      default:
        throw new RuntimeException("Invalid Phase: " + goGameController.getPhase());
    }
  }

//  private void playMonteCarloMove() {
//    int bestMove = MonteCarlo.getBestMove(goGameController.getGame(), 1000);
//    int boardSize = goGameController.getGameConfiguration().getBoardSize();
//    int x = bestMove % boardSize;
//    int y = bestMove / boardSize;
//    goGameController.playMoveOrToggleDeadStone(gameDatas.createMove(x, y));
//  }

}
