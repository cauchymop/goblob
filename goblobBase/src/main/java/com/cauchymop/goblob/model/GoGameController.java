package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
  private final transient GoGame goGame;
  private transient GameConfiguration gameConfiguration;
  private transient MatchEndStatus matchEndStatus;

  public GoGameController(GameData gameData) {
    gameConfiguration = gameData.getGameConfiguration();
    matchEndStatus = gameData.hasMatchEndStatus() ? gameData.getMatchEndStatus() : null;
    goGame = new GoGame(gameConfiguration.getBoardSize());
    for (Move move : gameData.getMoveList()) {
      playMove(move);
    }
  }

  public void pass() {
    playMove(GameDatas.createPassMove());
  }

  public boolean play(int x, int y) {
    return playMove(GameDatas.createMove(x, y));
  }

  public boolean playMove(Move move) {
    switch (move.getType()) {
      case MOVE:
        PlayGameData.Position position = move.getPosition();
        int pos = goGame.getPos(position.getX(), position.getY());
        if (!goGame.play(pos)) {
          return false;
        }
        // TODO: if endgame, update last change
        break;
      case PASS:
        MatchEndStatus.Color lastModifier = goGame.getCurrentColor().getGameDataColor();
        goGame.play(goGame.getPassValue());
        if (goGame.isGameEnd()) {
          matchEndStatus = MatchEndStatus.newBuilder()
              .setLastModifier(lastModifier)
              .setScore(calculateScore())
              .build();
        }
        break;
    }
    moves.add(move);
    return true;
  }

  private int calculateScore() {
    // TODO: better scoring.
    return 0;
  }

  public GoPlayer getCurrentPlayer() {
    return getGoPlayer(goGame.getCurrentColor());
  }

  public GoPlayer getOpponent() {
    return getGoPlayer(goGame.getCurrentColor().getOpponent());
  }

  public GoPlayer getGoPlayer(StoneColor color) {
    return players.get(color);
  }

  public void setGoPlayer(StoneColor color, GoPlayer player) {
    players.put(color, player);
  }

  @Override
  public String toString() {
    return String.format("GoGameController(GoGame=%s, black=%s, white=%s, moves=%s)", goGame,
        getGoPlayer(StoneColor.Black), getGoPlayer(StoneColor.White), moves);
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
    return getCurrentPlayer().getType() == GoPlayer.PlayerType.LOCAL && !getGame().isGameEnd();
  }

  public Mode getMode() {
    if (matchEndStatus != null) {
//      return Mode.END_GAME_NEGOTIATION;
    }
    return Mode.IN_GAME;
  }

  public boolean isEndGameStatusLastModifiedByCurrentPlayer() {
    return getMode() == Mode.END_GAME_NEGOTIATION
        && matchEndStatus.getLastModifier().equals(goGame.getCurrentColor().getGameDataColor());
  }

  public enum Mode {
    START_GAME_NEGOTIATION,
    IN_GAME,
    END_GAME_NEGOTIATION
  }
}
