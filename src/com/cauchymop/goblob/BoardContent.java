package com.cauchymop.goblob;

import com.cauchymop.goblob.GoBoard.Color;

/**
 * Class to represent the visible content of the board (stones and territories).
 */
public class BoardContent {

  private final int boardSize;
  private final GoBoard goBoard;

  private ContentColor[] board;

  public BoardContent(GoBoard goBoard) {
    this.goBoard = goBoard;
    this.boardSize = goBoard.getBoardSize();
    initBoard();
  }

  public void setContentColor(int x, int y, ContentColor currentPlayerColor) {
    board[getPos(x, y)] = currentPlayerColor;
  }

  private int getPos(int x, int y) {
    return y * boardSize + x;
  }

  public ContentColor getContentColor(int x, int y) {
    return board[getPos(x, y)];
  }

  private void initBoard() {
    board = new ContentColor[boardSize * boardSize];
    for (int y = 0; y < boardSize; y++) {
      for (int x = 0; x < boardSize; x++) {
        board[getPos(x, y)] = ContentColor.Empty;
      }
    }
  }

  public boolean play(ContentColor contentColor, int x, int y) {
    GoBoard.Color color = getColorFromContentColor(contentColor);
    if (!goBoard.play(color, x, y)) {
      return false;
    }
    updateFromGoBoard();
    updateTerritories();
    return true;
  }

  private void updateTerritories() {
    // TODO: scan board and update territories.
  }

  private Color getColorFromContentColor(ContentColor contentColor) {
    switch (contentColor) {
      case Black:
        return Color.Black;
      case White:
        return Color.White;
      default:
        throw new RuntimeException("Invalid ContentColor: only Black and White can play.");
    }
  }

  private void updateFromGoBoard() {
    for (int x = 0 ; x < boardSize ; x++) {
      for (int y = 0 ; y <boardSize ; y++) {
        switch (goBoard.getColor(x, y)) {
          case Empty:
            board[getPos(x, y)] = ContentColor.Empty;
            break;
          case Black:
            board[getPos(x, y)] = ContentColor.Black;
            break;
          case White:
            board[getPos(x, y)] = ContentColor.White;
            break;
          default:
            throw new RuntimeException("Invalid color");
        }
      }
    }
  }

  public static enum ContentColor {
    Empty, Black, White, BlackTerritory, WhiteTerritory
  }
}
