package com.cauchymop.goblob.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Class to test {@link TextBoard}.
 */
public class TextBoardTest {

  @Test
  public void testToString() {
    GoBoard board = new GoBoard(2);
    Assert.assertEquals("..\n..\n", TextBoard.toString(board));
  }

  @Test
  public void testToString_white() {
    GoBoard board = new GoBoard(2);
    board.play(StoneColor.White, board.getPos(1, 1));
    Assert.assertEquals("..\n.○\n", TextBoard.toString(board));
  }

  @Test
  public void testToString_black() {
    GoBoard board = new GoBoard(2);
    board.play(StoneColor.Black, board.getPos(1, 1));
    Assert.assertEquals("..\n.●\n", TextBoard.toString(board));
  }

  @Test
  public void testFillBoard() {
    GoBoard board = new GoBoard(2);
    TextBoard.fillBoard(board, ".●\n○.\n");
    Assert.assertEquals(StoneColor.Empty, board.getColor(0, 0));
    Assert.assertEquals(StoneColor.Black, board.getColor(1, 0));
    Assert.assertEquals(StoneColor.Empty, board.getColor(1, 1));
    Assert.assertEquals(StoneColor.White, board.getColor(0, 1));
  }

  @Test
  public void testFillBoard_comment() {
    GoBoard board = new GoBoard(2);
    TextBoard.fillBoard(board, "# .●\n○");
    Assert.assertEquals(StoneColor.White, board.getColor(0, 0));
    Assert.assertEquals(StoneColor.Empty, board.getColor(1, 0));
    Assert.assertEquals(StoneColor.Empty, board.getColor(1, 1));
    Assert.assertEquals(StoneColor.Empty, board.getColor(0, 1));
  }
}
