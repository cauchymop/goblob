package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;

import static com.cauchymop.goblob.proto.PlayGameData.Color;
import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import static com.cauchymop.goblob.proto.PlayGameData.MatchEndStatus;
import static com.cauchymop.goblob.proto.PlayGameData.Move;
import static com.cauchymop.goblob.proto.PlayGameData.Position;
import static com.cauchymop.goblob.proto.PlayGameData.Score;

/**
 * Class to handle interactions between the players and the {@link GoGame}.
 */
public class GoGameController implements Serializable {

  transient GameDatas gameDatas;

  private GoGame goGame;
  private GameData.Builder gameData;
  private GameData initialGameData;
  private Analytics analytics;

  public GoGameController(GameDatas gameDatas, GameData gameData, Analytics analytics) {
    this.gameDatas = gameDatas;
    this.initialGameData = gameData;
    this.gameData = Preconditions.checkNotNull(gameData).toBuilder();
    this.analytics = analytics;
    // The GoGame settings can change during the configuration, so we postpone its creation.
    if (gameData.getPhase() != Phase.CONFIGURATION) {
      createGoGame();
      for (Move move : this.gameData.getMoveList()) {
        goGame.play(getPos(move));
      }
    }
  }

  public void createGoGame() {
    GameConfiguration gameConfiguration = getGameConfiguration();
    goGame = new GoGame(gameConfiguration.getBoardSize(), gameConfiguration.getHandicap());
  }

  public Score getScore() {
    return getMatchEndStatus().getScore();
  }

  public boolean undo() {
    if (canUndo()) {
      gameData.addRedo(0, removeLastMove());
      goGame.undo();
      return true;
    }
    return false;
  }

  public boolean redo() {
    if (canRedo()) {
      playMove(gameData.getRedo(0));
      return true;
    }
    return false;
  }

  private boolean playMove(Move move) {
    if (gameData.getPhase() == Phase.IN_GAME && goGame.play(getPos(move))) {
      updateRedoForMove(move);
      gameData.addMove(move);
      gameData.setTurn(getOpponentColor());
      checkForMatchEnd();
      analytics.movePlayed(getGameConfiguration(), move);
      return true;
    } else {
      analytics.invalidMovePlayed(getGameConfiguration());
      return false;
    }
  }

  public GoPlayer getCurrentPlayer() {
    return gameDatas.getCurrentPlayer(gameData);
  }

  public GoPlayer getOpponent() {
    return gameDatas.getGoPlayer(gameData, getOpponentColor());
  }

  public Color getCurrentColor() {
    return gameDatas.getCurrentColor(gameData);
  }

  @Override
  public String toString() {
    return String.format("GoGameController(GoGame=%s, black=%s, white=%s, end=%s)",
        goGame, getBlackPlayer(), getWhitePlayer(), getMatchEndStatus());
  }

  public GoGame getGame() {
    return goGame;
  }

  public GameData buildGameData() {
    gameData.setSequenceNumber(gameData.getSequenceNumber() + 1);
    return gameData.build();
  }

  public GameConfiguration getGameConfiguration() {
    return gameData.getGameConfiguration();
  }

  private Move removeLastMove() {
    int lastIndex = gameData.getMoveCount() - 1;
    Move lastMove = gameData.getMove(lastIndex);
    gameData.removeMove(lastIndex);
    return lastMove;
  }

  public boolean isLocalTurn() {
    return gameDatas.isLocalTurn(gameData);
  }

  private boolean toggleDeadStone(Position position) {
    if (goGame.getColor(position.getX(), position.getY()) == null) {
      return false;
    }
    int index = getMatchEndStatus().getDeadStoneList().indexOf(position);
    MatchEndStatus.Builder matchEndStatus = gameData.getMatchEndStatusBuilder();
    if (index == -1) {
      matchEndStatus.addDeadStone(position);
    } else {
      matchEndStatus.removeDeadStone(index);
    }
    matchEndStatus.setLastModifier(gameData.getTurn());
    matchEndStatus.setScore(calculateScore());
    analytics.deadStoneToggled(getGameConfiguration());
    return true;
  }

  public List<PlayGameData.Position> getDeadStones() {
    return getMatchEndStatus().getDeadStoneList();
  }

  public void markingTurnDone() {
    if (isLocalGame() || !getMatchEndStatus().getLastModifier().equals(getCurrentColor())) {
      gameData.setPhase(Phase.FINISHED);
      analytics.gameFinished(getGameConfiguration(), getScore());
    }
    gameData.setTurn(getOpponentColor());
  }

  public boolean canUndo() {
    return isLocalGame() && gameData.getPhase() == Phase.IN_GAME && !gameData.getMoveList().isEmpty();
  }

  public boolean canRedo() {
    return isLocalGame() && gameData.getPhase() == Phase.IN_GAME && !gameData.getRedoList().isEmpty();
  }

  public void resign() {
    gameData.setPhase(Phase.FINISHED);
    Score.Builder score = gameData.getMatchEndStatusBuilder().getScoreBuilder();
    score.setWinner(getOpponentColor());
    score.setResigned(true);
    analytics.gameFinished(getGameConfiguration(), getScore());
  }

  public boolean isLocalGame() {
    return gameDatas.isLocalGame(gameData);
  }

  private Color getOpponentColor() {
    return GoBoard.getOpponent(getCurrentColor());
  }

  private Score calculateScore() {
    ScoreGenerator scoreGenerator = new ScoreGenerator(goGame.getBoard(),
        Sets.newHashSet(getDeadStones()), getGameConfiguration().getKomi());
    return scoreGenerator.getScore();
  }

  private void updateRedoForMove(Move move) {
    if (gameData.getRedoCount() == 0) {
      return;
    }
    if (move.equals(gameData.getRedo(0))) {
      gameData.removeRedo(0);
    } else {
      gameData.clearRedo();
    }
  }

  private void checkForMatchEnd() {
    if (goGame.isGameEnd()) {
      gameData.setPhase(Phase.DEAD_STONE_MARKING);
      Color lastModifier = GoBoard.getOpponent(goGame.getCurrentColor());
      gameData.getMatchEndStatusBuilder()
          .setLastModifier(lastModifier)
          .setScore(calculateScore());
    }
  }

  private int getPos(Move move) {
    switch (move.getType()) {
      case MOVE:
        Position position = move.getPosition();
        return goGame.getPos(position.getX(), position.getY());
      case PASS:
        return goGame.getPassValue();
      default:
        throw new RuntimeException("Invalid Move");
    }
  }

  private MatchEndStatus getMatchEndStatus() {
    return gameData.getMatchEndStatus();
  }

  private GoPlayer getWhitePlayer() {
    return getGameConfiguration().getWhite();
  }

  private GoPlayer getBlackPlayer() {
    return getGameConfiguration().getBlack();
  }

  public boolean isGameFinished() {
    return gameData.getPhase() == Phase.FINISHED;
  }

  public Phase getPhase() {
    return gameData.getPhase();
  }

  public boolean playMoveOrToggleDeadStone(Move move) {
    switch(getPhase()) {
      case IN_GAME:
        return playMove(move);
      case DEAD_STONE_MARKING:
        return toggleDeadStone(move.getPosition());
      default:
        throw new RuntimeException("Invalid mode");
    }
  }

  public GoPlayer getWinner() {
    return gameDatas.getGoPlayer(gameData, gameData.getMatchEndStatus().getScore().getWinner());
  }

  public void validateConfiguration() {

    if (isConfigurationAgreed(initialGameData, getGameConfiguration())) {
      gameData.setPhase(Phase.IN_GAME);
      createGoGame();
    } else {
      gameData.setPhase(Phase.CONFIGURATION);
    }
    gameData.setTurn(computeConfigurationNextTurn());
    analytics.configurationChanged(gameData);
  }

  private boolean isConfigurationAgreed(GameData initialGame,
      GameConfiguration newGameConfiguration) {
    return initialGame.getGameConfiguration().getGameType() == PlayGameData.GameType.LOCAL
        || (initialGame.getPhase() == Phase.CONFIGURATION
          && initialGame.getGameConfiguration().equals(newGameConfiguration));
  }

  private PlayGameData.Color computeConfigurationNextTurn() {
    if (getPhase() == Phase.CONFIGURATION) {
      return gameDatas.getOpponentColor(getGameConfiguration());
    } else if (getPhase() == Phase.IN_GAME) {
      return gameDatas.computeInGameTurn(getGameConfiguration(), 0);
    } else {
      throw new IllegalArgumentException("Invalid phase: " + getPhase());
    }
  }

  public GoPlayer getPlayerForColor(Color player) {
    return gameDatas.getGoPlayer(gameData, player);
  }

  public String getMatchId() {
    return gameData.getMatchId();
  }

  public void setBlackPlayerName(String blackPlayerName) {
    gameData.getGameConfigurationBuilder().getBlackBuilder().setName(blackPlayerName);
  }

  public void setWhitePlayerName(String whitePlayerName) {
    gameData.getGameConfigurationBuilder().getWhiteBuilder().setName(whitePlayerName);
  }

  public void setBoardSize(int boardSize) {
    gameData.getGameConfigurationBuilder().setBoardSize(boardSize);
  }

  public void setKomi(float komi) {
    gameData.getGameConfigurationBuilder().setKomi(komi);
  }

  public void setHandicap(int handicap) {
    gameData.getGameConfigurationBuilder().setHandicap(handicap);
  }

  public void swapPlayers() {
    GameConfiguration gameConfiguration = getGameConfiguration();
    PlayGameData.GoPlayer black = gameConfiguration.getBlack();
    PlayGameData.GoPlayer white = gameConfiguration.getWhite();
    gameData.getGameConfigurationBuilder().setBlack(white);
    gameData.getGameConfigurationBuilder().setWhite(black);
    gameData.setTurn(getOpponentColor());
  }

  public boolean pass() {
    return playMove(gameDatas.createPassMove());
  }
}
