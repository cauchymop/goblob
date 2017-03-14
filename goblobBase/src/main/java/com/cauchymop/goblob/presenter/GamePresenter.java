package com.cauchymop.goblob.presenter;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.BoardViewModel;
import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.GoBoard;
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.model.PlayerViewModel;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.view.GameView;
import com.cauchymop.goblob.view.GoBoardView;
import com.cauchymop.goblob.view.InGameView;

public class GamePresenter implements GoBoardView.BoardEventListener, ConfigurationEventListener, GameRepository.GameRepositoryListener, InGameView.InGameActionListener {

  private Analytics analytics;
  private GoGameController goGameController;
  private GameRepository gameRepository;
  private GameView view;
  private GameDatas gameDatas;

  public GamePresenter(GameDatas gameDatas, Analytics analytics,
      GameRepository gameRepository, final GameView view) {
    this.gameDatas = gameDatas;
    this.analytics = analytics;
    this.gameRepository = gameRepository;
    this.view = view;
    gameRepository.addGameRepositoryListener(this);
    updateGame();
  }

  private void updateGame() {
    PlayGameData.GameData currentGame = gameRepository.getCurrentGame();
    if (currentGame == null) {
      view.clear();
      return;
    }
    goGameController = new GoGameController(gameDatas, currentGame, analytics);
    if (isConfigured()) {
      view.setInGameViewModel(getInGameViewModel());
      view.setInGameActionListener(this);
    } else {
      view.setConfigurationViewModel(getConfigurationViewModel());
      view.setConfigurationViewListener(this);
    }
  }

  public InGameViewModel getInGameViewModel() {
    boolean passActionAvailable = goGameController.isLocalTurn() && goGameController.getPhase() == PlayGameData.GameData.Phase.IN_GAME;
    boolean doneActionAvailable = goGameController.isLocalTurn() && goGameController.getPhase() == PlayGameData.GameData.Phase.DEAD_STONE_MARKING;;
    return new InGameViewModel(getBoardViewModel(), getCurrentPlayerViewModel(), passActionAvailable, doneActionAvailable);
  }

  private PlayerViewModel getCurrentPlayerViewModel() {
    return new PlayerViewModel(goGameController.getCurrentPlayer().getName());
  }

  @Override
  public void gameListChanged() {
    // Nothing to do
  }

  @Override
  public void gameChanged(PlayGameData.GameData gameData) {
    if (gameData.getMatchId().equals(goGameController.getMatchId())) {
      updateGame();
    }
  }

  @Override
  public void gameSelected(PlayGameData.GameData gameData) {
    updateGame();
  }

  public void clear() {
    gameRepository.removeGameRepositoryListener(this);
  }

  private BoardViewModel getBoardViewModel() {
    GoGame game = goGameController.getGame();
    GoBoard board = game.getBoard();
    int boardSize = game.getBoardSize();
    int lastMoveX = -1;
    int lastMoveY = -1;
    PlayGameData.Color[][] stones = new PlayGameData.Color[boardSize][boardSize];
    for (int x = 0; x < boardSize; x++) {
      for (int y = 0; y < boardSize; y++) {
        stones[y][x] = board.getColor(x, y);
        int pos = game.getPos(x, y);
        if (pos == game.getLastMove()) {
          lastMoveX = x;
          lastMoveY = y;
        }
      }
    }

    PlayGameData.Color[][] territories = new PlayGameData.Color[boardSize][boardSize];
    for (PlayGameData.Position position : goGameController.getScore().getBlackTerritoryList()) {
      territories[position.getY()][position.getX()] = PlayGameData.Color.BLACK;
    }
    for (PlayGameData.Position position : goGameController.getScore().getWhiteTerritoryList()) {
      territories[position.getY()][position.getX()] = PlayGameData.Color.WHITE;
    }
    for (PlayGameData.Position position : goGameController.getDeadStones()) {
      int x = position.getX();
      int y = position.getY();
      territories[y][x] = gameDatas.getOppositeColor(stones[y][x]);
    }

    return new BoardViewModel(boardSize, stones, territories, lastMoveX, lastMoveY, goGameController.isLocalTurn());
  }

  private ConfigurationViewModel getConfigurationViewModel() {
    return new ConfigurationViewModel(goGameController.getGameConfiguration(), getConfigurationMessage(), goGameController.isLocalTurn());
  }

  private ConfigurationViewModel.ConfigurationMessage getConfigurationMessage() {
    if (goGameController.getPhase() == PlayGameData.GameData.Phase.INITIAL) {
      return ConfigurationViewModel.ConfigurationMessage.INITIAL;
    } else if (goGameController.isLocalTurn()) {
      return ConfigurationViewModel.ConfigurationMessage.ACCEPT_OR_CHANGE;
    } else {
      return ConfigurationViewModel.ConfigurationMessage.WAITING_FOR_OPPONENT;
    }
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

}
