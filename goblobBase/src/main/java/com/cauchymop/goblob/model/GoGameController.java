package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.List;

import static com.cauchymop.goblob.proto.PlayGameData.Color;
import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.cauchymop.goblob.proto.PlayGameData.GameType;
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

  private ArrayDeque<Move> moves = Queues.newArrayDeque();
  private ArrayDeque<Move> redoMoves = Queues.newArrayDeque();
  private final GoGame goGame;
  private GameConfiguration gameConfiguration;
  private MatchEndStatus matchEndStatus;
  private final GoPlayer blackPlayer;
  private final GoPlayer whitePlayer;
  private final String localGoogleIdentity;
  private final String matchId;

  public GoGameController(GameDatas gameDatas, GameData gameData, String localGoogleIdentity) {
    this.gameDatas = gameDatas;
    this.localGoogleIdentity = localGoogleIdentity;
    matchId = gameData.getMatchId();
    gameConfiguration = gameData.getGameConfiguration();
    blackPlayer = gameConfiguration.getBlack();
    whitePlayer = gameConfiguration.getWhite();
    goGame = new GoGame(gameConfiguration.getBoardSize(), gameConfiguration.getHandicap());
    matchEndStatus = gameData.hasMatchEndStatus() ? gameData.getMatchEndStatus() : null;
    moves = Queues.newArrayDeque(gameData.getMoveList());
    for (Move move : moves) {
      goGame.play(getPos(move));
    }
  }

  public Score getScore() {
    return matchEndStatus.getScore();
  }

  public boolean undo() {
    if (canUndo()) {
      redoMoves.addFirst(moves.removeLast());
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

  private Score calculateScore() {
    ScoreGenerator scoreGenerator = new ScoreGenerator(goGame.getBoard(),
        Sets.newHashSet(getDeadStones()), gameConfiguration.getKomi());
    return scoreGenerator.getScore();
  }

  public boolean playMove(Move move) {
    if (getMode() == Mode.IN_GAME && goGame.play(getPos(move))) {
      updateRedoForMove(move);
      moves.add(move);
      checkForMatchEnd();
      return true;
    }
    return false;
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
      matchEndStatus = MatchEndStatus.newBuilder()
          .setLastModifier(lastModifier)
          .setTurn(lastModifier)
          .setScore(calculateScore())
          .build();
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

  public GoPlayer getCurrentPlayer() {
    return getGoPlayer(getCurrentColor());
  }

  public GoPlayer getOpponent() {
    return getGoPlayer(getOpponentColor());
  }

  private Color getOpponentColor() {
    return GoBoard.getOpponent(getCurrentColor());
  }

  public Color getCurrentColor() {
    if (getMode() == Mode.IN_GAME) {
      return goGame.getCurrentColor();
    }
    return matchEndStatus.getTurn();
  }

  public GoPlayer getGoPlayer(Color color) {
    return color == Color.BLACK ? blackPlayer : whitePlayer;
  }

  @Override
  public String toString() {
    return String.format("GoGameController(GoGame=%s, black=%s, white=%s, end=%s)",
        goGame, blackPlayer, whitePlayer, matchEndStatus);
  }

  public GoGame getGame() {
    return goGame;
  }

  public GameData getGameData() {
    return gameDatas.createGameData(matchId, gameConfiguration, moves, matchEndStatus);
  }

  public GameConfiguration getGameConfiguration() {
    return gameConfiguration;
  }

  public boolean isLocalTurn() {
    return isLocalPlayer(getCurrentPlayer()) && !isGameFinished();
  }

  public boolean isGameFinished() {
    return matchEndStatus != null && matchEndStatus.getGameFinished();
  }

  public Mode getMode() {
    if (!gameConfiguration.getAccepted()) {
      return Mode.START_GAME_NEGOTIATION;
    }
    if (matchEndStatus != null) {
      return Mode.END_GAME_NEGOTIATION;
    }
    return Mode.IN_GAME;
  }

  public boolean toggleDeadStone(Move move) {
    Position position = move.getPosition();
    if (goGame.getColor(position.getX(), position.getY()) == null) {
      return false;
    }
    int index = matchEndStatus.getDeadStoneList().indexOf(move.getPosition());
    if (index == -1) {
      matchEndStatus = matchEndStatus.toBuilder()
          .addDeadStone(move.getPosition())
          .setLastModifier(matchEndStatus.getTurn())
          .build();
    } else {
      matchEndStatus = matchEndStatus.toBuilder()
          .removeDeadStone(index)
          .setLastModifier(matchEndStatus.getTurn())
          .build();
    }
    matchEndStatus = matchEndStatus.toBuilder()
        .setScore(calculateScore())
        .build();
    return true;
  }

  public List<PlayGameData.Position> getDeadStones() {
    if (matchEndStatus == null) {
      return Lists.newArrayList();
    }
    return matchEndStatus.getDeadStoneList();
  }

  private boolean isEndGameStatusLastModifiedByCurrentPlayer() {
    return getMode() == Mode.END_GAME_NEGOTIATION
        && matchEndStatus.getLastModifier().equals(getCurrentColor());
  }

  public void markingTurnDone() {
    if (isLocalGame() || !isEndGameStatusLastModifiedByCurrentPlayer()) {
      matchEndStatus = matchEndStatus.toBuilder()
          .setGameFinished(true)
          .build();
    }
    matchEndStatus = matchEndStatus.toBuilder()
        .setTurn(getOpponentColor())
        .build();
  }

  public boolean isLocalGame() {
    return getGameConfiguration().getGameType() == GameType.LOCAL;
  }

  public boolean canUndo() {
    return isLocalTurn() && getMode() == Mode.IN_GAME && !moves.isEmpty();
  }

  public boolean canRedo() {
    return isLocalGame() && getMode() == Mode.IN_GAME && !redoMoves.isEmpty();
  }

  public void resign() {
    matchEndStatus = MatchEndStatus.newBuilder()
        .setGameFinished(true)
        .setScore(Score.newBuilder()
            .setWinner(getOpponentColor())
            .setResigned(true))
        .build();
  }

  public GoPlayer getWinner() {
    return getGoPlayer(matchEndStatus.getScore().getWinner());
  }

  public boolean isLocalPlayer(GoPlayer player) {
    return isLocalGame() || player.getGoogleId().equals(localGoogleIdentity);
  }

  public String getLocalPlayerId() {
    return isLocalPlayer(blackPlayer) ? blackPlayer.getId() : whitePlayer.getId();
  }

  public String getRemotePlayerId() {
    return isLocalPlayer(blackPlayer) ? whitePlayer.getId() : blackPlayer.getId();
  }

  public String getMatchId() {
    return matchId;
  }

  public enum Mode {
    START_GAME_NEGOTIATION,
    IN_GAME,
    END_GAME_NEGOTIATION
  }
}
