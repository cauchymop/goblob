package com.cauchymop.goblob;

/**
 * Class to represent a board with colors on it.
 */
public class Board {

  private Color board[];
  private int boardSize;

  public Board(int boardSize) {
    this.boardSize = boardSize;
    board = new Color[getInternalBoardSize() * getInternalBoardSize()];
  }

  protected int getInternalBoardSize() {
    return boardSize;
  }

  protected int getBoardSize() {
    return boardSize;
  }

  protected int getPos(int x, int y) {
    return y * getInternalBoardSize() + x;
  }

  public Color getColor(int position) {
    return board[position];
  }

  public Color getColor(int x, int y) {
    return board[getPos(x, y)];
  }

  public Color setColor(int position, Color color) {
    return board[position] = color;
  }

  public Color setColor(int x, int y, Color color) {
    return board[getPos(x, y)] = color;
  }

  public int getNorth(int pos) {
    return pos - getInternalBoardSize();
  }

  public int getSouth(int pos) {
    return pos + getInternalBoardSize();
  }

  public int getWest(int pos) {
    return pos - 1;
  }

  public int getEast(int pos) {
    return pos + 1;
  }

  public static enum Color {
    Empty,
    Border,
    Black,
    White,
    BlackTerritory,
    WhiteTerritory,
  }
}
