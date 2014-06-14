package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  private transient Set<Listener> listeners = Sets.newHashSet();
  private transient GameConfiguration gameConfiguration;
  private transient MatchEndStatus matchEndStatus;

  public GoGameController(GameData gameData) {
    gameConfiguration = gameData.getGameConfiguration();
    matchEndStatus = gameData.getMatchEndStatus();
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

  private boolean playMove(Move move) {
    switch (move.getType()) {
      case MOVE:
        PlayGameData.Position position = move.getPosition();
        int pos = goGame.getPos(position.getX(), position.getY());
        if (!goGame.play(pos)) {
          return false;
        }
        break;
      case PASS:
        goGame.play(goGame.getPassValue());
        break;
    }
    moves.add(move);
    fireGameChanged();
    return true;
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

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  protected void fireGameChanged() {
    if (listeners.isEmpty()) return;
    for (Listener listener : listeners) {
      listener.gameChanged(this);
    }
  }

  public GoGame getGame() {
    return goGame;
  }

  public GameData getGameData() {
    return GameData.newBuilder()
        .setGameConfiguration(gameConfiguration)
        .addAllMove(moves)
        .build();
  }

  public GameConfiguration getGameConfiguration() {
    return gameConfiguration;
  }

  public boolean isLocalTurn() {
    return getCurrentPlayer().getType() == GoPlayer.PlayerType.LOCAL && !getGame().isGameEnd();
  }

  public interface Listener {
    public void gameChanged(GoGameController gameController);
  }
}
