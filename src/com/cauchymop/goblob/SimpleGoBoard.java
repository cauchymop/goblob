package com.cauchymop.goblob;

import java.security.InvalidParameterException;

/**
 * Class to implement {@link GoBoard} in the most simple way.
 */
public class SimpleGoBoard implements GoBoard {

  private int size;
  private StoneColor[] board;

  public SimpleGoBoard(int size) {
    this.size = size;
    board = new StoneColor[size*size];
    empty();
  }

  @Override
  public void empty() {
    for (int pos = 0 ; pos < size*size ; pos++) {
      board[pos] = StoneColor.Empty;
    }
  }

  @Override
  public boolean play(StoneColor color, int x, int y) {
    if (board[y*size + x] != StoneColor.Empty) {
      return false;
    }
    board[y*size + x] = color;
    return true;
  }

  @Override
  public double getScore(StoneColor color) {
    return 0;
  }

  @Override
  public StoneColor getColor(int x, int y) {
    return board[y*size + x];
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public void copyFrom(GoBoard board) {
    if (!(board instanceof SimpleGoBoard)) {
      throw new InvalidParameterException();
    }
    SimpleGoBoard other = (SimpleGoBoard) board;
    System.arraycopy(other.board, 0, board, 0, board.getSize());
  }
}
