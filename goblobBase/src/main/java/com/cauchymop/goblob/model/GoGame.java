package com.cauchymop.goblob.model;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
 */
public class GoGame extends Game implements Serializable {

  public static final int NO_MOVE = -1;

  private GoPlayer blackPlayer;
  private GoPlayer whitePlayer;
  private int boardSize;
  private GoBoard board;
  private transient PlayerController blackController;
  private transient PlayerController whiteController;
  private StoneColor currentColor;
  private ArrayList<GoBoard> boardHistory = Lists.newArrayList();
  private ArrayList<Integer> moveHistory = Lists.newArrayList();
  // Instance pool management.
  private GoBoard[] boardPool = new GoBoard[10];
  private int boardPoolSize = 0;

  private transient Thread thread;

  public GoGame(int boardSize) {
    this.boardSize = boardSize;
    currentColor = StoneColor.Black;
    board = getNewBoard();
    boardHistory.add(board);
  }

  public void runGame() {
    thread = new Thread("Game") {

      @Override
      public void run() {
        while (!isGameEnd()) {
          System.out.println(currentColor + ".startTurn()");
          getCurrentController().startTurn();
        }
      }
    };
    thread.start();
  }

  private PlayerController getCurrentController() {
    if (currentColor == StoneColor.Black) {
      return blackController;
    } else {
      return whiteController;
    }
  }

  private PlayerController getOpponentController() {
    if (currentColor == StoneColor.Black) {
      return whiteController;
    } else {
      return blackController;
    }
  }

  private GoBoard getNewBoard() {
    if (boardPoolSize == 0) {
      return new GoBoard(boardSize);
    }
    boardPoolSize--;
    GoBoard board = boardPool[boardPoolSize];
    board.clear();
    return board;
  }

  private void recycleBoard(GoBoard board) {
    boardPool[boardPoolSize] = board;
    boardPoolSize++;
  }

  public boolean pass(PlayerController controller) {
    return play(controller, getPassValue());
  }

  public boolean play(PlayerController controller, int x, int y) {
    return play(controller, getPos(x, y));
  }

  @Override
  public boolean play(PlayerController controller, int move) {
    if (controller != getCurrentController()) {
      return false;
    }

    GoBoard newBoard = getNewBoard();
    newBoard.copyFrom(board);

    if (move == getPassValue()) {
      applyMove(newBoard, move);
      return true;
    }

    if (newBoard.play(currentColor, move) && !boardHistory.contains(newBoard)) {
      applyMove(newBoard, move);
      return true;
    }

    recycleBoard(newBoard);
    return false;
  }

  private void applyMove(GoBoard newBoard, int move) {
    boardHistory.add(newBoard);
    moveHistory.add(move);
    board = newBoard;
    currentColor = currentColor.getOpponent();
    if (getOpponentController() != null) {
      getOpponentController().opponentPlayed(move);
    }
    fireGameChanged();
  }

  @Override
  public void undo() {
    currentColor = currentColor.getOpponent();
    recycleBoard(boardHistory.remove(boardHistory.size() - 1));
    moveHistory.remove(moveHistory.size() - 1);
    board = boardHistory.get(boardHistory.size() - 1);
  }

  @Override
  public Game copy() {
    GoGame copy = new GoGame(boardSize);
    for (Integer move : moveHistory) {
      copy.play(copy.getCurrentController(), move);
    }
    return copy;
  }

  @Override
  public int getPosCount() {
    return boardSize * boardSize + 1;
  }

  public int getPos(int x, int y) {
    return y * getBoardSize() + x;
  }

  @Override
  public boolean isGameEnd() {
    if (moveHistory.size() < 2) {
      return false;
    }
    int lastMove = moveHistory.get(moveHistory.size() - 1);
    int previousMove = moveHistory.get(moveHistory.size() - 2);
    int passMove = getPassValue();
    return lastMove == passMove && previousMove == passMove;
  }

  @Override
  public double getScore() {
    return board.getScore(currentColor);
  }

  public StoneColor getColor(int x, int y) {
    return board.getColor(x, y);
  }

  public void setBlackController(PlayerController blackController) {
    System.out.println("setBlackController: " + blackController);
    this.blackController = blackController;
  }

  public void setWhiteController(PlayerController whiteController) {
    System.out.println("setWhiteController: " + whiteController);
    this.whiteController = whiteController;
  }

  public GoPlayer getBlackPlayer() {
    return blackPlayer;
  }

  public GoPlayer getWhitePlayer() {
    return whitePlayer;
  }

  public StoneColor getCurrentColor() {
    return currentColor;
  }

  public int getBoardSize() {
    return boardSize;
  }

  public double[] getScores() {
    PlayerController lastController = whiteController;
    if (!(lastController instanceof AIPlayerController)) {
      lastController = blackController;
      if (!(lastController instanceof AIPlayerController)) {
        return null;
      }
    }
    AIPlayerController aiController = (AIPlayerController) lastController;
    return aiController.getAi().getScores();
  }

  public GoPlayer getCurrentPlayer() {
    return getGoPlayer(getCurrentColor());
  }

  public GoPlayer getOpponent() {
    return getGoPlayer(getCurrentColor().getOpponent());
  }

  private GoPlayer getGoPlayer(StoneColor color) {
    switch (color) {
      case Black:
        return getBlackPlayer();
      case White:
        return getWhitePlayer();
      default:
        throw new IllegalStateException("Invalid Player Color: " + getCurrentColor());
    }
  }

  public boolean isLastMovePass() {
    return getLastMove() == getPassValue();
  }

  public int getPassValue() {
    return boardSize * boardSize;
  }

  public int getLastMove() {
    return (moveHistory.isEmpty()) ? NO_MOVE : moveHistory.get(moveHistory.size() - 1);
  }

  public StoneColor getTerritory(int pos) {
    return board.getTerritory(pos);
  }

  public void mark(int pos, StoneColor color) {
    board.mark(pos, color);
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

  @Override
  public List<Integer> getMoveHistory() {
    return moveHistory;
  }

  public void setBlackPlayer(GoPlayer blackPlayer) {
    this.blackPlayer = blackPlayer;
  }

  public void setWhitePlayer(GoPlayer whitePlayer) {
    this.whitePlayer = whitePlayer;
  }
}
