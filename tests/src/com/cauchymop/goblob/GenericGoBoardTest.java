package com.cauchymop.goblob;

import android.test.AndroidTestCase;

/**
 * Class to test {@link GenericGoBoard}.
 */
public class GenericGoBoardTest extends AndroidTestCase {

  public void testPlay() {
    GoBoard goBoard = new GenericGoBoard(5);
    assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    assertTrue(goBoard.play(StoneColor.White, 1, 1));
    assertEquals(StoneColor.Black, goBoard.getColor(0, 0));
    assertEquals(StoneColor.White, goBoard.getColor(1, 1));
  }

  public void testPlay_nonEmptyCell() {
    GoBoard goBoard = new GenericGoBoard(5);
    assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    assertFalse(goBoard.play(StoneColor.White, 0, 0));
    assertEquals(StoneColor.Black, goBoard.getColor(0, 0));
  }

  public void testPlay_capture() {
    GoBoard goBoard = new GenericGoBoard(5);
    assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    assertTrue(goBoard.play(StoneColor.White, 0, 1));
    assertTrue(goBoard.play(StoneColor.White, 1, 0));
    assertEquals(StoneColor.Empty, goBoard.getColor(0,0));
  }

  public void testPlay_capture2() {
    GoBoard goBoard = new GenericGoBoard(5);
    assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    assertTrue(goBoard.play(StoneColor.Black, 1, 0));
    assertTrue(goBoard.play(StoneColor.White, 0, 1));
    assertTrue(goBoard.play(StoneColor.White, 1, 1));
    assertTrue(goBoard.play(StoneColor.White, 2, 0));
    assertEquals(StoneColor.Empty, goBoard.getColor(0,0));
    assertEquals(StoneColor.Empty, goBoard.getColor(1,0));
  }

  public void testPlay_suicide() {
    GoBoard goBoard = new GenericGoBoard(5);
    assertTrue(goBoard.play(StoneColor.White, 0, 1));
    assertTrue(goBoard.play(StoneColor.White, 1, 1));
    assertTrue(goBoard.play(StoneColor.White, 2, 0));
    assertTrue(goBoard.play(StoneColor.Black, 0, 0));
    assertFalse(goBoard.play(StoneColor.Black, 1, 0));
  }

  public void testPlay_connectAndCapture() {
    GoBoard goBoard = new GenericGoBoard(5);
    assertTrue(goBoard.play(StoneColor.White, 4, 4));
    assertTrue(goBoard.play(StoneColor.Black, 4, 3));
    assertTrue(goBoard.play(StoneColor.Black, 3, 3));
    assertTrue(goBoard.play(StoneColor.Black, 3, 4));  // Connect and capture.
    assertTrue(goBoard.play(StoneColor.White, 4, 2));
    assertTrue(goBoard.play(StoneColor.White, 3, 2));
    assertTrue(goBoard.play(StoneColor.White, 2, 3));
    assertTrue(goBoard.play(StoneColor.White, 2, 4));  // No capture.
    assertEquals(StoneColor.Black, goBoard.getColor(3, 3));
  }
}
