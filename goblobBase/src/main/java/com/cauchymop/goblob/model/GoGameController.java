package com.cauchymop.goblob.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;

/**
 * Class to handle interactions between the {@link GoPlayer}s and the {@link GoGame}.
 */
public class GoGameController implements Serializable {

  private Map<StoneColor, GoPlayer> players = Maps.newHashMap();
  private List<Integer> moves;
  private transient final GoGame goGame;
  private transient Set<Listener> listeners = Sets.newHashSet();
  private transient Thread thread;
  private transient PlayerController blackController;
  private transient PlayerController whiteController;

  public GoGameController(GameData gameData, int boardsize) {
    this.moves = Lists.newArrayList(gameData.getMoveList());
    this.goGame = createGoGame(boardsize);
  }

  private GoGame createGoGame(int boardsize) {
    GoGame goGame = new GoGame(boardsize);
    for (int move : moves) {
      goGame.play(move);
    }
    return goGame;
  }

  private PlayerController getCurrentController() {
    if (goGame.getCurrentColor() == StoneColor.Black) {
      return blackController;
    } else {
      return whiteController;
    }
  }

  public void runGame() {
    thread = new Thread("Game") {

      @Override
      public void run() {
        while (!goGame.isGameEnd()) {
          getCurrentController().startTurn();
        }
      }
    };
    thread.start();
  }

  public boolean pass(PlayerController controller) {
    return playMove(controller, goGame.getPassValue());
  }

  public boolean play(PlayerController controller, int x, int y) {
    return playMove(controller, goGame.getPos(x, y));
  }

  private boolean playMove(PlayerController controller, int move) {
    if (isCurrentController(controller) && playMove(move)) {
      moves.add(move);
      fireGameChanged();
      return true;
    }
    return false;
  }

  private boolean playMove(int move) {
    if (goGame.play(move)) {
      return true;
    }
    return false;
  }

  private boolean isCurrentController(PlayerController controller) {
    return controller == getCurrentController();
  }

  public void setBlackController(PlayerController blackController) {
    System.out.println("setBlackController: " + blackController);
    this.blackController = blackController;
  }

  public void setWhiteController(PlayerController whiteController) {
    System.out.println("setWhiteController: " + whiteController);
    this.whiteController = whiteController;
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

  public void pause() {
    System.out.println("pause - killing thread");
    if (thread != null) {
      thread.interrupt();
    }
  }

  public void resume() {
    System.out.println("resume - starting thread");
    runGame();
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
    return GameData.newBuilder().addAllMove(moves).build();
  }

  public interface Listener {
    public void gameChanged(GoGameController gameController);
  }
}
