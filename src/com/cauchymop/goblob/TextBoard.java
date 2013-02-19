package com.cauchymop.goblob;

/**
 * Class to convert between a {@link Board} and its text representation.
 */
public class TextBoard {

  public static String toString(Board board) {
    StringBuilder buf = new StringBuilder();
    for (int y = 0 ; y < board.getBoardSize() ; y++) {
      for (int x = 0 ; x < board.getBoardSize() ; x++) {
        buf.append(getChar(board.getColor(x, y)));
      }
      buf.append('\n');
    }
    return buf.toString();
  }

  private static char getChar(Board.Color color) {
    switch (color) {
      case Empty:
        return '.';
      case Black:
        return '●';
      case White:
        return '○';
    }
    throw new RuntimeException("Invalid color");
  }

  public static void fillBoard(Board board, String text) {
    int x = 0;
    int y = 0;
    for (char ch : text.toCharArray()) {
      switch(ch) {
        case '\n':
          y++;
          x = 0;
          break;
        case '.':
          x++;
          break;
        case '●':
        case 'X':
          board.setColor(x, y, Board.Color.Black);
          x++;
          break;
        case '○':
        case 'O':
          board.setColor(x, y, Board.Color.White);
          x++;
          break;
      }
    }
  }
}
