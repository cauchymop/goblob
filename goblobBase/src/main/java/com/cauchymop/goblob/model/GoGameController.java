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

  private final GoGame goGame;
  private GameData.Builder gameData;
  private GameData initialGameData;

  public GoGameController(GameDatas gameDatas, GameData gameData) {
    this.gameDatas = gameDatas;
    this.initialGameData = gameData;
    this.gameData = Preconditions.checkNotNull(gameData).toBuilder();
    GameConfiguration gameConfiguration = getGameConfiguration();
    goGame = new GoGame(gameConfiguration.getBoardSize(), gameConfiguration.getHandicap());
    for (Move move : this.gameData.getMoveList()) {
      goGame.play(getPos(move));
    }
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
      return true;
    }
    return false;
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
    return true;
  }

  public List<PlayGameData.Position> getDeadStones() {
    return getMatchEndStatus().getDeadStoneList();
  }

  public void markingTurnDone() {
    if (isLocalGame() || !getMatchEndStatus().getLastModifier().equals(getCurrentColor())) {
      gameData.setPhase(Phase.FINISHED);
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
  }

  public boolean isLocalPlayer(GoPlayer player) {
    return gameDatas.isLocalPlayer(gameData, player);
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

  public void updateGameConfiguration(int boardSize, int handicap, float komi,
      GoPlayer blackPlayer, GoPlayer whitePlayer) {
    gameData.setGameConfiguration(gameDatas.createGameConfiguration(boardSize, handicap, komi,
            getGameConfiguration().getGameType(), blackPlayer, whitePlayer));
    gameData.setPhase(isConfigurationAgreed(initialGameData, getGameConfiguration()) ? Phase.IN_GAME : Phase.CONFIGURATION);
    gameData.setTurn(computeConfigurationTurn());
  }

  private boolean isConfigurationAgreed(GameData initialGame,
      GameConfiguration newGameConfiguration) {
    return initialGame.getGameConfiguration().getGameType() == PlayGameData.GameType.LOCAL
        || initialGame.getPhase() == Phase.CONFIGURATION
        && initialGame.getGameConfiguration().equals(newGameConfiguration);
  }

  private PlayGameData.Color computeConfigurationTurn() {
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
}
