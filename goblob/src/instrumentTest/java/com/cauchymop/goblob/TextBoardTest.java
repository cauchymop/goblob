package com.cauchymop.goblob;

import android.test.AndroidTestCase;

import com.cauchymop.goblob.model.GoBoard;
import com.cauchymop.goblob.model.StoneColor;
import com.cauchymop.goblob.model.TextBoard;

/**
 * Class to test {@link TextBoard}.
 */
public class TextBoardTest extends AndroidTestCase {

  public void testToString() {
    GoBoard board = new GoBoard(2);
    assertEquals("..\n..\n", TextBoard.toString(board));
  }

  public void testToString_white() {
    GoBoard board = new GoBoard(2);
    board.play(StoneColor.White, 1, 1);
    assertEquals("..\n.○\n", TextBoard.toString(board));
  }

  public void testToString_black() {
    GoBoard board = new GoBoard(2);
    board.play(StoneColor.Black, 1, 1);
    assertEquals("..\n.●\n", TextBoard.toString(board));
  }

  public void testFillBoard() {
    GoBoard board = new GoBoard(2);
    TextBoard.fillBoard(board, ".●\n○.\n");
    assertEquals(StoneColor.Empty, board.getColor(0, 0));
    assertEquals(StoneColor.Black, board.getColor(1, 0));
    assertEquals(StoneColor.Empty, board.getColor(1, 1));
    assertEquals(StoneColor.White, board.getColor(0, 1));
  }

  public void testFillBoard_comment() {
    GoBoard board = new GoBoard(2);
    TextBoard.fillBoard(board, "# .●\n○");
    assertEquals(StoneColor.White, board.getColor(0, 0));
    assertEquals(StoneColor.Empty, board.getColor(1, 0));
    assertEquals(StoneColor.Empty, board.getColor(1, 1));
    assertEquals(StoneColor.Empty, board.getColor(0, 1));
  }
}
