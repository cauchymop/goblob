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
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.view.GameView;

public class GamePresenter implements MovePlayedListener, GameRepository.GameRepositoryListener {

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
    goGameController = new GoGameController(gameDatas, gameRepository.getCurrentGame(), analytics);
    if (isConfigured()) {
      this.view.initInGameView(getInGameViewModel());
      this.view.setMovePlayedListener(GamePresenter.this);
    } else {
      this.view.initConfigurationView(getConfigurationViewModel());
      // TODO
//      view.setConfigurationViewListener(this);
    }
  }

  private ConfigurationViewModel getConfigurationViewModel() {
    return new ConfigurationViewModel(goGameController.getGameConfiguration().getKomi());
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

  public void played(int x, int y) {
    play(gameDatas.createMove(x, y));
  }

  private void play(PlayGameData.Move move) {
    boolean played = goGameController.playMoveOrToggleDeadStone(move);

    System.out.println(" ****************************************** PLAYED *********************************");
    if(played) {
      gameRepository.commitGameChanges(goGameController.buildGameData());
    } else {
      view.buzz();
      analytics.invalidMovePlayed(goGameController.getGameConfiguration());
    }
  }

  public InGameViewModel getInGameViewModel() {
    return new InGameViewModel(getBoardViewModel());
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
}
