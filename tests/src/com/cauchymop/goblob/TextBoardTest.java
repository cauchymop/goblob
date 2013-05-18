package com.cauchymop.goblob;

import android.test.AndroidTestCase;

/**
 * Class to test {@link TextBoard}.
 */
public class TextBoardTest extends AndroidTestCase {

  public void testToString() {
    GoBoard board = new SimpleGoBoard(2);
    assertEquals("..\n..\n", TextBoard.toString(board));
  }

  public void testToString_white() {
    GoBoard board = new SimpleGoBoard(2);
    board.play(StoneColor.White, 1, 1);
    assertEquals("..\n.○\n", TextBoard.toString(board));
  }

  public void testToString_black() {
    GoBoard board = new SimpleGoBoard(2);
    board.play(StoneColor.Black, 1, 1);
    assertEquals("..\n.●\n", TextBoard.toString(board));
  }

  public void testToString_invalid() {
    GoBoard board = new SimpleGoBoard(2);
    for (StoneColor color : new StoneColor[]{StoneColor.BlackTerritory,
        StoneColor.WhiteTerritory, StoneColor.Border}) {
      board.play(color, 0, 0);
      try {
        TextBoard.toString(board);
        fail("Encoding an invalid color should raise an exception");
      } catch (RuntimeException exception) {
        // Expected.
      }
    }
  }

  public void testFillBoard() {
    GoBoard board = new SimpleGoBoard(2);
    TextBoard.fillBoard(board, ".●\n○.\n");
    assertEquals(StoneColor.Empty, board.getColor(0, 0));
    assertEquals(StoneColor.Black, board.getColor(1, 0));
    assertEquals(StoneColor.Empty, board.getColor(1, 1));
    assertEquals(StoneColor.White, board.getColor(0, 1));
  }

  public void testFillBoard_comment() {
    GoBoard board = new SimpleGoBoard(2);
    TextBoard.fillBoard(board, "# .●\n○");
    assertEquals(StoneColor.White, board.getColor(0, 0));
    assertEquals(StoneColor.Empty, board.getColor(1, 0));
    assertEquals(StoneColor.Empty, board.getColor(1, 1));
    assertEquals(StoneColor.Empty, board.getColor(0, 1));
  }
}
