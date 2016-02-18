package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayDeque;
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

  private ArrayDeque<Move> redoMoves = Queues.newArrayDeque();
  private final GoGame goGame;
  private GameData gameData;

  public GoGameController(GameDatas gameDatas, GameData gameData) {
    this.gameDatas = gameDatas;
    this.gameData = Preconditions.checkNotNull(gameData);
    GameConfiguration gameConfiguration = getGameConfiguration();
    goGame = new GoGame(gameConfiguration.getBoardSize(), gameConfiguration.getHandicap());
    for (Move move : getMoves()) {
      goGame.play(getPos(move));
    }
  }

  public Score getScore() {
    return getMatchEndStatus().getScore();
  }

  public boolean undo() {
    if (canUndo()) {
      redoMoves.addFirst(removeLastMove());
      goGame.undo();
      return true;
    }
    return false;
  }

  public boolean redo() {
    if (canRedo()) {
      playMove(redoMoves.peekFirst());
      return true;
    }
    return false;
  }

  public boolean playMove(Move move) {
    if (getMode() == GameDatas.Mode.IN_GAME && goGame.play(getPos(move))) {
      updateRedoForMove(move);
      addMove(move);
      setTurn(getOpponentColor());
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

  public GameData getGameData() {
    return gameData;
  }

  public GameConfiguration getGameConfiguration() {
    return gameData.getGameConfiguration();
  }

  private Move removeLastMove() {
    ArrayDeque<Move> moves = Queues.newArrayDeque(getMoves());
    Move lastMove = moves.removeLast();
    gameData = gameData.toBuilder()
        .clearMove()
        .addAllMove(moves)
        .build();
    return lastMove;
  }

  private void addMove(Move move) {
    gameData = gameData.toBuilder()
        .addMove(move)
        .build();
  }

  private boolean isLocalTurn() {
    return gameDatas.isLocalTurn(gameData);
  }

  public boolean toggleDeadStone(Move move) {
    Position position = move.getPosition();
    if (goGame.getColor(position.getX(), position.getY()) == null) {
      return false;
    }
    int index = getMatchEndStatus().getDeadStoneList().indexOf(move.getPosition());
    MatchEndStatus.Builder matchEndStatusBuilder = getMatchEndStatus().toBuilder();
    if (index == -1) {
      matchEndStatusBuilder.addDeadStone(move.getPosition());
    } else {
      matchEndStatusBuilder.removeDeadStone(index);
    }
    setMatchEndStatus(matchEndStatusBuilder
        .setLastModifier(gameData.getTurn())
        .setScore(calculateScore()));
    return true;
  }

  public List<PlayGameData.Position> getDeadStones() {
    return getMatchEndStatus().getDeadStoneList();
  }

  public void markingTurnDone() {
    MatchEndStatus.Builder matchEndStatusBuilder = getMatchEndStatus().toBuilder();
    if (isLocalGame() || !isEndGameStatusLastModifiedByCurrentPlayer()) {
      matchEndStatusBuilder.setGameFinished(true);
    }
    setMatchEndStatus(matchEndStatusBuilder);

    setTurn(getOpponentColor());
  }

  private void setTurn(Color color) {
    gameData = gameData.toBuilder().setTurn(color).build();
  }

  public boolean canUndo() {
    return isLocalTurn() && getMode() == GameDatas.Mode.IN_GAME && !getMoves().isEmpty();
  }

  private List<Move> getMoves() {
    return gameData.getMoveList();
  }

  public boolean canRedo() {
    return isLocalGame() && getMode() == GameDatas.Mode.IN_GAME && !redoMoves.isEmpty();
  }

  public void resign() {
    setMatchEndStatus(MatchEndStatus.newBuilder()
        .setGameFinished(true)
        .setScore(Score.newBuilder()
            .setWinner(getOpponentColor())
            .setResigned(true)));
  }

  public boolean isLocalPlayer(GoPlayer player) {
    return gameDatas.isLocalPlayer(gameData, player);
  }

  public boolean isLocalGame() {
    return gameDatas.isLocalGame(gameData);
  }

  public GameDatas.Mode getMode() {
    return gameDatas.getMode(gameData);
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
    if (redoMoves.isEmpty()) {
      return;
    }
    if (move.equals(redoMoves.peekFirst())) {
      redoMoves.removeFirst();
    } else {
      redoMoves.clear();
    }
  }

  private void checkForMatchEnd() {
    if (goGame.isGameEnd()) {
      Color lastModifier = GoBoard.getOpponent(goGame.getCurrentColor());
      setMatchEndStatus(MatchEndStatus.newBuilder()
          .setLastModifier(lastModifier)
          .setScore(calculateScore()));
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

  private void setMatchEndStatus(MatchEndStatus.Builder matchEndStatus) {
    gameData = gameData.toBuilder().setMatchEndStatus(matchEndStatus).build();
  }

  private boolean isEndGameStatusLastModifiedByCurrentPlayer() {
    return getMode() == GameDatas.Mode.END_GAME_NEGOTIATION
        && getMatchEndStatus().getLastModifier().equals(getCurrentColor());
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
}
