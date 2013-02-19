package com.cauchymop.goblob;

import android.test.AndroidTestCase;

/**
 * Class to test {@link TextBoard}.
 */
public class TextBoardTest extends AndroidTestCase {

  public void testToString() {
    Board board = new Board(2);
    assertEquals("..\n..\n", TextBoard.toString(board));
  }

  public void testToString_white() {
    Board board = new Board(2);
    board.setColor(1, 1, Board.Color.White);
    assertEquals("..\n.○\n", TextBoard.toString(board));
  }

  public void testToString_black() {
    Board board = new Board(2);
    board.setColor(1, 1, Board.Color.Black);
    assertEquals("..\n.●\n", TextBoard.toString(board));
  }

  public void testToString_invalid() {
    Board board = new Board(2);
    for (Board.Color color : new Board.Color[]{Board.Color.BlackTerritory,
        Board.Color.WhiteTerritory, Board.Color.Border}) {
      board.setColor(0, 0, color);
      try {
        TextBoard.toString(board);
        fail("Encoding an invalid color should raise an exception");
      } catch (RuntimeException exception) {
        // Expected.
      }
    }
  }

  public void testFillBoard() {
    Board board = new Board(2);
    TextBoard.fillBoard(board, ".●\n○.\n");
    assertEquals(Board.Color.Empty, board.getColor(0, 0));
    assertEquals(Board.Color.Black, board.getColor(1, 0));
    assertEquals(Board.Color.Empty, board.getColor(1, 1));
    assertEquals(Board.Color.White, board.getColor(0, 1));
  }
}
