package com.cauchymop.goblob;

/**
 * Class to represent a board with colors on it.
 */
public class Board {

  private Color board[];
  private int boardSize;

  public Board(int boardSize) {
    this.boardSize = boardSize;
    board = new Color[(boardSize + 2) * (boardSize + 2)];
    initEmptyCells();
    initBorderCells();
  }

  private void initEmptyCells() {
    for (int x = 0 ; x < getBoardSize() ; x++) {
      for (int y = 0 ; y < getBoardSize() ; y++) {
        setColor(x, y, Color.Empty);
      }
    }
  }

  private void initBorderCells() {
    for (int col = -1 ; col < getBoardSize() + 1 ; col++) {
      setColor(-1, col, Color.Border);
      setColor(col, -1, Color.Border);
      setColor(getBoardSize(), col, Color.Border);
      setColor(col, getBoardSize(), Color.Border);
    }
  }

  protected int getBoardSize() {
    return boardSize;
  }

  protected int getPos(int x, int y) {
    return (y + 1) * (boardSize + 2) + (x + 1);
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
    return pos - (boardSize + 2);
  }

  public int getSouth(int pos) {
    return pos + (boardSize + 2);
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
