package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.cauchymop.goblob.proto.PlayGameData.MatchEndStatus;
import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.cauchymop.goblob.proto.PlayGameData.Move;

/**
 * Class to handle interactions between the {@link GoPlayer}s and the {@link GoGame}.
 */
public class GoGameController implements Serializable {

  private Map<StoneColor, GoPlayer> players = Maps.newHashMap();
  private List<Move> moves = Lists.newArrayList();
  private final GoGame goGame;
  private GameConfiguration gameConfiguration;
  private MatchEndStatus matchEndStatus;
  private PlayGameData.Score score;

  public GoGameController(GameData gameData) {
    gameConfiguration = gameData.getGameConfiguration();
    goGame = new GoGame(gameConfiguration.getBoardSize());
    matchEndStatus = gameData.hasMatchEndStatus() ? gameData.getMatchEndStatus() : null;
    moves = Lists.newArrayList(gameData.getMoveList());
    for (Move move : moves) {
      goGame.play(getPos(move));
    }
    updateScore();
  }

  public PlayGameData.Score getScore() {
    return score;
  }

  private void updateScore() {
    ScoreGenerator scoreGenerator = new ScoreGenerator(goGame.getBoard(),
        Sets.newHashSet(getDeadStones()), gameConfiguration.getKomi());
    score = scoreGenerator.getScore();
  }

  public boolean playMove(Move move) {
    if (getMode() == Mode.IN_GAME && goGame.play(getPos(move))) {
      moves.add(move);
      checkForMatchEnd();
      updateScore();
      return true;
    }
    return false;
  }

  private void checkForMatchEnd() {
    if (goGame.isGameEnd()) {
      PlayGameData.Color lastModifier = goGame.getCurrentColor().getOpponent().getGameDataColor();
      matchEndStatus = MatchEndStatus.newBuilder()
          .setLastModifier(lastModifier)
          .setTurn(lastModifier)
          .setScore(score)
          .build();
    }
  }

  private int getPos(Move move) {
    switch (move.getType()) {
      case MOVE:
        PlayGameData.Position position = move.getPosition();
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
    return getGoPlayer(getCurrentColor().getOpponent());
  }

  public StoneColor getCurrentColor() {
    if (getMode() == Mode.IN_GAME) {
      return goGame.getCurrentColor();
    }
    return StoneColor.getStoneColor(matchEndStatus.getTurn());
  }

  public GoPlayer getGoPlayer(StoneColor color) {
    return players.get(color);
  }

  public void setGoPlayer(StoneColor color, GoPlayer player) {
    players.put(color, player);
  }

  @Override
  public String toString() {
    return String.format("GoGameController(GoGame=%s, black=%s, white=%s, end=%s)",
        goGame, getGoPlayer(StoneColor.Black), getGoPlayer(StoneColor.White), matchEndStatus);
  }

  public GoGame getGame() {
    return goGame;
  }

  public GameData getGameData() {
    GameData.Builder builder = GameData.newBuilder()
        .setGameConfiguration(gameConfiguration)
        .addAllMove(moves);
    if (matchEndStatus != null) {
      builder.setMatchEndStatus(matchEndStatus);
    }
    return builder.build();
  }

  public GameConfiguration getGameConfiguration() {
    return gameConfiguration;
  }

  public boolean isLocalTurn() {
    return getCurrentPlayer().getType() == GoPlayer.PlayerType.LOCAL && !isGameFinished();
  }

  public boolean isGameFinished() {
    return matchEndStatus != null && matchEndStatus.getGameFinished();
  }

  public Mode getMode() {
    if (matchEndStatus != null) {
      return Mode.END_GAME_NEGOTIATION;
    }
    return Mode.IN_GAME;
  }

  public boolean toggleDeadStone(Move move) {
    PlayGameData.Position position = move.getPosition();
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
    updateScore();
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
        && matchEndStatus.getLastModifier().equals(getCurrentColor().getGameDataColor());
  }

  public void markingTurnDone() {
    if (!isEndGameStatusLastModifiedByCurrentPlayer()) {
      matchEndStatus = matchEndStatus.toBuilder()
          .setGameFinished(true)
          .build();
    }
    matchEndStatus = matchEndStatus.toBuilder()
        .setTurn(getCurrentColor().getOpponent().getGameDataColor())
        .build();
  }

  public enum Mode {
    START_GAME_NEGOTIATION,
    IN_GAME,
    END_GAME_NEGOTIATION
  }
}
