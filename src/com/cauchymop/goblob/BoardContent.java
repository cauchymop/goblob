package com.cauchymop.goblob;

/**
 * Class to represent the visible content of the board (stones and territories).
 */
public class BoardContent {

  private final int boardSize;
  private final GoBoard goBoard;

  private ContentColor[][] board;

  public BoardContent(GoBoard goBoard) {
    this.goBoard = goBoard;
    this.boardSize = goBoard.getBoardSize();
    initBoard();
  }

  public void setContentColor(int x, int y, ContentColor currentPlayerColor) {
    board[y][x] = currentPlayerColor;
  }

  public ContentColor getContentColor(int x, int y) {
    return board[y][x];
  }

  private void initBoard() {
    board = new ContentColor[boardSize][];
    for (int row = 0; row < boardSize; row++) {
      board[row] = new ContentColor[boardSize];
      for (int col = 0; col < boardSize; col++) {
        board[row][col] = ContentColor.Empty;
      }
    }
  }

  public boolean play(GoBoard.Color color, int x, int y) {
    if (!goBoard.play(GoBoard.Color.Black, x, y)) {
      return false;
    }
    updateFromGoBoard();
    return true;
  }

  private void updateFromGoBoard() {
    for (int x = 0 ; x < boardSize ; x++) {
      for (int y = 0 ; y <boardSize ; y++) {
        switch (goBoard.getColor(x, y)) {
          case Empty:
            board[y][x] = ContentColor.Empty;
            break;
          case Black:
            board[y][x] = ContentColor.Black;
            break;
          case White:
            board[y][x] = ContentColor.White;
            break;
          default:
            throw new RuntimeException("Invalid color");
        }
      }
    }
  }

  public static enum ContentColor {
    Empty, Black, White, BlackTerritory, WhiteTerritory;
  }
}
