package com.cauchymop.goblob.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Class to handle interactions between the {@link GoPlayer}s and the {@link GoGame}.
 */
public class GoGameController implements Serializable {

  private final GoGame goGame;
  private Map<StoneColor, GoPlayer> players = Maps.newHashMap();
  private transient Set<Listener> listeners = Sets.newHashSet();
  private transient Thread thread;
  private transient PlayerController blackController;
  private transient PlayerController whiteController;

  public GoGameController(GoGame goGame) {
    this.goGame = goGame;
  }

  private PlayerController getCurrentController() {
    if (goGame.getCurrentColor() == StoneColor.Black) {
      return blackController;
    } else {
      return whiteController;
    }
  }

  private PlayerController getOpponentController() {
    if (goGame.getCurrentColor() == StoneColor.Black) {
      return whiteController;
    } else {
      return blackController;
    }
  }

  public void runGame() {
    thread = new Thread("Game") {

      @Override
      public void run() {
        while (!goGame.isGameEnd()) {
          System.out.println(goGame.getCurrentColor() + ".startTurn()");
          getCurrentController().startTurn();
        }
      }
    };
    thread.start();
  }

  public boolean pass(PlayerController controller) {
    return isCurrentController(controller) && playMove(goGame.getPassValue());
  }

  public boolean play(PlayerController controller, int x, int y) {
    return isCurrentController(controller) && playMove(goGame.getPos(x, y));
  }

  private boolean playMove(int passValue) {
    if (goGame.play(passValue)) {
      fireGameChanged();
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
    return String.format("GoGameController(GoGame=%s, black=%s, white=%s)", goGame,
        getGoPlayer(StoneColor.Black), getGoPlayer(StoneColor.White));
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

  public interface Listener {
    public void gameChanged(GoGameController gameController);
  }
}
