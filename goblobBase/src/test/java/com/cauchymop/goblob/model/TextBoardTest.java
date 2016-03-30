package com.cauchymop.goblob.model;

import org.junit.Assert;
import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.Color;

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
    board.play(Color.WHITE, board.getPos(1, 1));
    Assert.assertEquals("..\n.○\n", TextBoard.toString(board));
  }

  @Test
  public void testToString_black() {
    GoBoard board = new GoBoard(2);
    board.play(Color.BLACK, board.getPos(1, 1));
    Assert.assertEquals("..\n.●\n", TextBoard.toString(board));
  }

  @Test
  public void testFillBoard() {
    GoBoard board = new GoBoard(2);
    TextBoard.fillBoard(board, ".●\n○.\n");
    Assert.assertNull(board.getColor(0, 0));
    Assert.assertNull(board.getColor(1, 1));
    Assert.assertEquals(Color.BLACK, board.getColor(1, 0));
    Assert.assertEquals(Color.WHITE, board.getColor(0, 1));
  }

  @Test
  public void testFillBoard_comment() {
    GoBoard board = new GoBoard(2);
    TextBoard.fillBoard(board, "# .●\n○");
    Assert.assertEquals(Color.WHITE, board.getColor(0, 0));
    Assert.assertNull(board.getColor(1, 0));
    Assert.assertNull(board.getColor(1, 1));
    Assert.assertNull(board.getColor(0, 1));
  }

  @Test
  public void testFillBoard_comment_eof() {
    GoBoard board = new GoBoard(2);
    TextBoard.fillBoard(board, "..\n..\n#");
    Assert.assertNull(board.getColor(0, 0));
    Assert.assertNull(board.getColor(1, 0));
    Assert.assertNull(board.getColor(1, 1));
    Assert.assertNull(board.getColor(0, 1));
  }
}
