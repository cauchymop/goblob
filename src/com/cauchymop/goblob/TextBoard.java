package com.cauchymop.goblob;

import java.io.IOException;
import java.io.StringReader;

import com.cauchymop.goblob.InvalidTextBoardException.ERROR_CODE;

/**
 * Class to convert between a {@link Board} and its text representation.
 */
public class TextBoard {

  public static String toString(Board board) {
    StringBuilder buf = new StringBuilder();
    for (int y = 0; y < board.getBoardSize(); y++) {
      for (int x = 0; x < board.getBoardSize(); x++) {
        buf.append(getChar(board.getColor(x, y)));
      }
      buf.append('\n');
    }
    return buf.toString();
  }

  private static char getChar(StoneColor color) {
    switch (color) {
      case Empty:
      case BlackTerritory:
      case WhiteTerritory:
        return '.';
      case Black:
        return '●';
      case White:
        return '○';
    }
    throw new RuntimeException("Invalid color: " + color);
  }

  public static Board fromString(String textBoard) throws InvalidTextBoardException {
    Board board = new Board(getTextBoardSize(textBoard));
    fillBoard(board, textBoard);
    return board;
  }

  public static int getTextBoardSize(String textBoard) throws InvalidTextBoardException {
    int sizeX = -1;
    int sizeY = -1;
    int x = 0;
    int y = 0;
    StringReader reader = new StringReader(textBoard);
    while (true) {
      try {
        switch (reader.read()) {
          case -1:
            if (sizeX != sizeY) {
              throw new InvalidTextBoardException(ERROR_CODE.InvalidSize);
            }
            return sizeX;
          case '#':
            skipLine(reader);
            break;
          case '\n':
            y++;
            sizeY = y;
            if (sizeX == -1) {
              sizeX = x;
            } else if ( x != sizeX) {
              throw new InvalidTextBoardException(ERROR_CODE.InvalidSize);
            }
            x = 0;
            break;
          case '.':
          case '●':
          case 'X':
          case '○':
          case 'O':
            x++;
            break;
        }
      } catch (IOException e) {
        // A StringReader can't throw an IOException.
      }
    }
  }

  // TODO: Error checking in fillBoard as in getTextBoardSize
  public static void fillBoard(Board board, String text) {
    int x = 0;
    int y = 0;
    StringReader reader = new StringReader(text);
    while (true) {
      try {
        switch (reader.read()) {
          case -1:
            return;
          case '#':
            skipLine(reader);
            break;
          case '\n':
            y++;
            x = 0;
            break;
          case '.':
            x++;
            break;
          case '●':
          case 'X':
            board.setColor(x, y, StoneColor.Black);
            x++;
            break;
          case '○':
          case 'O':
            board.setColor(x, y, StoneColor.White);
            x++;
            break;
        }
      } catch (IOException e) {
        // A StringReader can't throw an IOException.
      }
    }
  }

  private static void skipLine(StringReader reader) throws IOException {
    int c;
    do {
      c = reader.read();
      if (c == -1) {
        return;
      }
    } while (c != '\n');
  }
}
