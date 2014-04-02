package com.cauchymop.goblob.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Class to test {@link com.cauchymop.goblob.model.GoBoard}.
 */
public class GoBoardTest {

  @Test
  public void testPlay() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(0, 0)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(1, 1)));
    Assert.assertEquals(StoneColor.Black, goBoard.getColor(0, 0));
    Assert.assertEquals(StoneColor.White, goBoard.getColor(1, 1));
  }

  @Test
  public void testPlay_nonEmptyCell() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(0, 0)));
    Assert.assertFalse(goBoard.play(StoneColor.White, goBoard.getPos(0, 0)));
    Assert.assertEquals(StoneColor.Black, goBoard.getColor(0, 0));
  }

  @Test
  public void testPlay_capture() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(0, 0)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(0, 1)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(1, 0)));
    Assert.assertNull(goBoard.getColor(0, 0));
  }

  @Test
  public void testPlay_capture2() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(0, 0)));
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(1, 0)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(0, 1)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(1, 1)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(2, 0)));
    Assert.assertNull(goBoard.getColor(0, 0));
    Assert.assertNull(goBoard.getColor(1, 0));
  }

  @Test
  public void testPlay_suicide() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(0, 1)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(1, 1)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(2, 0)));
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(0, 0)));
    Assert.assertFalse(goBoard.play(StoneColor.Black, goBoard.getPos(1, 0)));
  }

  @Test
  public void testPlay_connectAndCapture() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(4, 4)));
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(4, 3)));
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(3, 3)));
    Assert.assertTrue(goBoard.play(StoneColor.Black, goBoard.getPos(3, 4)));  // Connect and capture.
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(4, 2)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(3, 2)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(2, 3)));
    Assert.assertTrue(goBoard.play(StoneColor.White, goBoard.getPos(2, 4)));  // No capture.
    Assert.assertEquals(StoneColor.Black, goBoard.getColor(3, 3));
  }
}
