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
    Assert.assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    Assert.assertTrue(goBoard.play(StoneColor.White, 1, 1));
    Assert.assertEquals(StoneColor.Black, goBoard.getColor(0, 0));
    Assert.assertEquals(StoneColor.White, goBoard.getColor(1, 1));
  }

  @Test
  public void testPlay_nonEmptyCell() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    Assert.assertFalse(goBoard.play(StoneColor.White, 0, 0));
    Assert.assertEquals(StoneColor.Black, goBoard.getColor(0, 0));
  }

  @Test
  public void testPlay_capture() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    Assert.assertTrue(goBoard.play(StoneColor.White, 0, 1));
    Assert.assertTrue(goBoard.play(StoneColor.White, 1, 0));
    Assert.assertEquals(StoneColor.Empty, goBoard.getColor(0, 0));
  }

  @Test
  public void testPlay_capture2() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    Assert.assertTrue(goBoard.play(StoneColor.Black, 1, 0));
    Assert.assertTrue(goBoard.play(StoneColor.White, 0, 1));
    Assert.assertTrue(goBoard.play(StoneColor.White, 1, 1));
    Assert.assertTrue(goBoard.play(StoneColor.White, 2, 0));
    Assert.assertEquals(StoneColor.Empty, goBoard.getColor(0, 0));
    Assert.assertEquals(StoneColor.Empty, goBoard.getColor(1, 0));
  }

  @Test
  public void testPlay_suicide() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.White, 0, 1));
    Assert.assertTrue(goBoard.play(StoneColor.White, 1, 1));
    Assert.assertTrue(goBoard.play(StoneColor.White, 2, 0));
    Assert.assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    Assert.assertFalse(goBoard.play(StoneColor.Black, 1, 0));
  }

  @Test
  public void testPlay_connectAndCapture() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(StoneColor.White, 4, 4));
    Assert.assertTrue(goBoard.play(StoneColor.Black, 4, 3));
    Assert.assertTrue(goBoard.play(StoneColor.Black, 3, 3));
    Assert.assertTrue(goBoard.play(StoneColor.Black, 3, 4));  // Connect and capture.
    Assert.assertTrue(goBoard.play(StoneColor.White, 4, 2));
    Assert.assertTrue(goBoard.play(StoneColor.White, 3, 2));
    Assert.assertTrue(goBoard.play(StoneColor.White, 2, 3));
    Assert.assertTrue(goBoard.play(StoneColor.White, 2, 4));  // No capture.
    Assert.assertEquals(StoneColor.Black, goBoard.getColor(3, 3));
  }
}
