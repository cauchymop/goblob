package com.cauchymop.goblob;

import android.test.AndroidTestCase;

/**
 * Class to test {@link GoBoard}..
 */
public class GoBoardTest extends AndroidTestCase {

  private static final int TEST_BOARDSIZE = 5;

  public void testPlay() {
    GoBoard goBoard = new GoBoard(TEST_BOARDSIZE);
    assertTrue(goBoard.play(GoBoard.Color.Black, 0, 0));
    assertEquals(GoBoard.Color.Black, goBoard.getColor(0, 0));
  }

  public void testPlay_nonEmptyCell() {
    GoBoard goBoard = new GoBoard(TEST_BOARDSIZE);
    assertTrue(goBoard.play(GoBoard.Color.Black, 0, 0));
    assertFalse(goBoard.play(GoBoard.Color.Black, 0, 0));
  }

  public void testPlay_capture() {
    GoBoard goBoard = new GoBoard(TEST_BOARDSIZE);
    assertTrue(goBoard.play(GoBoard.Color.Black, 0, 0));
    assertTrue(goBoard.play(GoBoard.Color.White, 1, 0));
    assertTrue(goBoard.play(GoBoard.Color.White, 0, 1));
    assertEquals(GoBoard.Color.Empty, goBoard.getColor(0,0));
  }

  public void testPlay_noLiberties() {
    GoBoard goBoard = new GoBoard(TEST_BOARDSIZE);
    assertTrue(goBoard.play(GoBoard.Color.White, 1, 0));
    assertTrue(goBoard.play(GoBoard.Color.White, 0, 1));
    assertFalse(goBoard.play(GoBoard.Color.Black, 0, 0));
  }

  public void testPlay_farLiberties() {
    GoBoard goBoard = new GoBoard(TEST_BOARDSIZE);
    assertTrue(goBoard.play(GoBoard.Color.White, 1, 0));
    assertTrue(goBoard.play(GoBoard.Color.White, 0, 1));
    assertTrue(goBoard.play(GoBoard.Color.White, 0, 0));
  }

  public void testPlay_noLibertiesFar() {
    GoBoard goBoard = new GoBoard(TEST_BOARDSIZE);
    assertTrue(goBoard.play(GoBoard.Color.White, 2, 0));
    assertTrue(goBoard.play(GoBoard.Color.White, 1, 1));
    assertTrue(goBoard.play(GoBoard.Color.White, 0, 2));
    assertTrue(goBoard.play(GoBoard.Color.Black, 1, 0));
    assertTrue(goBoard.play(GoBoard.Color.Black, 0, 1));
    assertFalse(goBoard.play(GoBoard.Color.Black, 0, 0));
  }
}
