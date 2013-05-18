package com.cauchymop.goblob;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
 */
public class GoGame implements Game {

  private StoneColor currentColor;
  private int boardSize;
  private ArrayList<GoBoard> boardHistory = Lists.newArrayList();
  private ArrayList<Integer> moveHistory = Lists.newArrayList();
  private GoBoard[] boardPool = new GoBoard[10];
  private int boardPoolSize = 0;
  private GoBoard board;

  public GoGame(int boardSize) {
    this.boardSize = boardSize;
    currentColor = StoneColor.Black;
    board = getNewBoard();
    board.empty();
    boardHistory.add(board);
  }

  private GoBoard getNewBoard() {
    if (boardPoolSize == 0) {
      return new GoBoard5();
    }
    boardPoolSize--;
    return boardPool[boardPoolSize];
  }

  private void recycleBoard(GoBoard board) {
    boardPool[boardPoolSize] = board;
    boardPoolSize++;
  }

  public void pass() {
    GoBoard newBoard = getNewBoard();
    newBoard.copyFrom(board);
    boardHistory.add(newBoard);
    moveHistory.add(boardSize*boardSize);
    board = newBoard;
    currentColor = currentColor.getOpponent();
  }

  public boolean play(int x, int y) {
    GoBoard newBoard = getNewBoard();
    newBoard.copyFrom(board);
    if (newBoard.play(currentColor, x, y)) {
      boardHistory.add(newBoard);
      moveHistory.add(y*boardSize + x);
      board = newBoard;
      currentColor = currentColor.getOpponent();
      return true;
    }
    recycleBoard(newBoard);
    return false;
  }

  @Override
  public void undo() {
    currentColor = currentColor.getOpponent();
    recycleBoard(boardHistory.remove(boardHistory.size() - 1));
    moveHistory.remove(moveHistory.size() - 1);
    board = boardHistory.get(boardHistory.size() - 1);
  }

  @Override
  public int getPosCount() {
    return boardSize * boardSize + 1;
  }

  @Override
  public boolean play(int pos) {
    if (pos == boardSize * boardSize) {
      pass();
      return true;
    }
    return play(pos % boardSize, pos / boardSize);
  }

  @Override
  public boolean isGameEnd() {
    if (moveHistory.size() < 2) {
      return false;
    }
    int lastMove = moveHistory.get(moveHistory.size() - 1);
    int previousMove = moveHistory.get(moveHistory.size() - 2);
    int passMove = boardSize * boardSize;
    return lastMove == passMove && previousMove == passMove;
  }

  @Override
  public double getScore() {
    return board.getScore(currentColor);
  }

  public StoneColor getColor(int x, int y) {
    return board.getColor(x, y);
  }
}
