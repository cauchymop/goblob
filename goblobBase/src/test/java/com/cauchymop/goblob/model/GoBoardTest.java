package com.cauchymop.goblob.model;

import org.junit.Assert;
import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.Color;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Class to test {@link GoBoard}.
 */
public class GoBoardTest {

  @Test
  public void testPlay() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(0, 0)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(1, 1)));
    Assert.assertEquals(Color.BLACK, goBoard.getColor(0, 0));
    Assert.assertEquals(Color.WHITE, goBoard.getColor(1, 1));
  }

  @Test
  public void testPlay_nonEmptyCell() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(0, 0)));
    Assert.assertFalse(goBoard.play(Color.WHITE, goBoard.getPos(0, 0)));
    Assert.assertEquals(Color.BLACK, goBoard.getColor(0, 0));
  }

  @Test
  public void testPlay_capture() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(0, 0)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(0, 1)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(1, 0)));
    Assert.assertNull(goBoard.getColor(0, 0));
  }

  @Test
  public void testPlay_capture2() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(0, 0)));
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(1, 0)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(0, 1)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(1, 1)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(2, 0)));
    Assert.assertNull(goBoard.getColor(0, 0));
    Assert.assertNull(goBoard.getColor(1, 0));
  }

  @Test
  public void testPlay_suicide() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(0, 1)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(1, 1)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(2, 0)));
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(0, 0)));
    Assert.assertFalse(goBoard.play(Color.BLACK, goBoard.getPos(1, 0)));
  }

  @Test
  public void testPlay_connectAndCapture() {
    GoBoard goBoard = new GoBoard(5);
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(4, 4)));
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(4, 3)));
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(3, 3)));
    Assert.assertTrue(goBoard.play(Color.BLACK, goBoard.getPos(3, 4)));  // Connect and capture.
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(4, 2)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(3, 2)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(2, 3)));
    Assert.assertTrue(goBoard.play(Color.WHITE, goBoard.getPos(2, 4)));  // No capture.
    Assert.assertEquals(Color.BLACK, goBoard.getColor(3, 3));
  }

  @Test
  public void testClear() {
    GoBoard goBoard = new GoBoard(5);
    goBoard.play(Color.BLACK, goBoard.getPos(0, 0));
    goBoard.play(Color.WHITE, goBoard.getPos(1, 1));
    goBoard.clear();
    assertThat(goBoard).isEqualTo(new GoBoard(5));
  }
}
