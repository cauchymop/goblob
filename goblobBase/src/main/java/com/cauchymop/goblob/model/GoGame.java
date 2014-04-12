package com.cauchymop.goblob.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
 */
public class GoGame implements Serializable {

  public static final int NO_MOVE = -1;

  private int boardSize;
  private GoBoard board;
  private StoneColor currentColor;
  private ArrayList<GoBoard> boardHistory = Lists.newArrayList();
  private ArrayList<Integer> moveHistory = Lists.newArrayList();
  // Instance pool management.
  private GoBoard[] boardPool = new GoBoard[10];
  private int boardPoolSize = 0;

  public GoGame(int boardSize) {
    this.boardSize = boardSize;
    currentColor = StoneColor.Black;
    board = getNewBoard();
    boardHistory.add(board);
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

  public boolean play(int move) {
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
  }

  public void undo() {
    currentColor = currentColor.getOpponent();
    recycleBoard(boardHistory.remove(boardHistory.size() - 1));
    moveHistory.remove(moveHistory.size() - 1);
    board = boardHistory.get(boardHistory.size() - 1);
  }

  public GoGame copy() {
    GoGame copy = new GoGame(boardSize);
    for (Integer move : moveHistory) {
      copy.play(move);
    }
    return copy;
  }

  public int getPos(int x, int y) {
    return y * getBoardSize() + x;
  }

  public boolean isGameEnd() {
    if (moveHistory.size() < 2) {
      return false;
    }
    int lastMove = moveHistory.get(moveHistory.size() - 1);
    int previousMove = moveHistory.get(moveHistory.size() - 2);
    int passMove = getPassValue();
    return lastMove == passMove && previousMove == passMove;
  }

  public double getScore() {
    return board.getScore(currentColor);
  }

  public StoneColor getColor(int x, int y) {
    return board.getColor(x, y);
  }

  public StoneColor getCurrentColor() {
    return currentColor;
  }

  public int getBoardSize() {
    return boardSize;
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

  public List<Integer> getMoveHistory() {
    return moveHistory;
  }

  @Override
  public String toString() {
    return String.format("GoGame(size=%d, moves=%s)", getBoardSize(), getMoveHistory());
  }
}
