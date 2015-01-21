package com.cauchymop.goblob.model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.cauchymop.goblob.proto.PlayGameData.Color;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
 */
public class GoGame implements Serializable {

  public static final int NO_MOVE = -1;

  private int boardSize;
  private GoBoard board;
  private Color currentColor;
  private ArrayList<GoBoard> boardHistory = Lists.newArrayList();
  private ArrayList<Integer> moveHistory = Lists.newArrayList();
  // Instance pool management.
  private transient LinkedList<GoBoard> boardPool = Lists.newLinkedList();

  public GoGame(int boardSize) {
    this.boardSize = boardSize;
    currentColor = Color.BLACK;
    board = getNewBoard();
    boardHistory.add(board);
  }

  private GoBoard getNewBoard() {
    if (boardPool.isEmpty()) {
      return new GoBoard(boardSize);
    }
    GoBoard board = boardPool.remove(0);
    board.clear();
    return board;
  }

  private void recycleBoard(GoBoard board) {
    boardPool.add(board);
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
    currentColor = GoBoard.getOpponent(currentColor);
  }

  public void undo() {
    currentColor = GoBoard.getOpponent(currentColor);
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

  public Color getColor(int x, int y) {
    return board.getColor(x, y);
  }

  public Color getCurrentColor() {
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

  public List<Integer> getMoveHistory() {
    return moveHistory;
  }

  @Override
  public String toString() {
    return String.format("GoGame(size=%d, moves=%s)", getBoardSize(), getMoveHistory());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GoGame goGame = (GoGame) o;

    return Objects.equal(boardSize, goGame.boardSize)
        && Objects.equal(moveHistory, goGame.moveHistory);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(boardSize, moveHistory);
  }

  public GoBoard getBoard() {
    return board;
  }

  public Set<Integer> getNonEyeFillingMoves() {
    Set<Integer> moves = Sets.newHashSet();
    for (int pos = 0 ; pos < boardSize*boardSize ; pos++) {
      if (board.getColor(pos) == null && !board.isEyeFilling(pos, currentColor)) {
        moves.add(pos);
      }
    }
    return moves;
  }
}

