package com.cauchymop.goblob.model;

import java.io.IOException;
import java.io.StringReader;

import static com.cauchymop.goblob.proto.PlayGameData.Color;

/**
 * Class to convert between a {@link GoBoard} and its text representation.
 */
public class TextBoard {

  public static String toString(GoBoard board) {
    StringBuilder buf = new StringBuilder();
    for (int y = 0; y < board.getSize(); y++) {
      for (int x = 0; x < board.getSize(); x++) {
        buf.append(getChar(board.getColor(x, y)));
      }
      buf.append('\n');
    }
    return buf.toString();
  }

  private static char getChar(Color color) {
    if (color == null) {
      return '.';
    }
    switch (color) {
      case BLACK:
        return '●';
      case WHITE:
        return '○';
    }
    throw new RuntimeException("Invalid color");
  }

  public static void fillBoard(GoBoard board, String text) {
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
            board.play(Color.BLACK, board.getPos(x, y));
            x++;
            break;
          case '○':
          case 'O':
            board.play(Color.WHITE, board.getPos(x, y));
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
