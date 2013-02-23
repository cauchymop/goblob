package com.cauchymop.goblob;

import android.test.AndroidTestCase;

/**
 * Class to test {@link GoGame}.
 */
public class GoGameTest extends AndroidTestCase {

  private static final int TEST_BOARDSIZE = 5;

  public void testPlay() {
    GoGame goGame = new GoGame(TEST_BOARDSIZE);
    assertTrue(goGame.play(0, 0));
    assertTrue(goGame.play(1, 1));
    assertEquals(StoneColor.Black, goGame.getBoard().getColor(0, 0));
    assertEquals(StoneColor.White, goGame.getBoard().getColor(1, 1));
  }

  public void testPlay_nonEmptyCell() {
    GoGame goGame = new GoGame(TEST_BOARDSIZE);
    assertTrue(goGame.play(0, 0));
    assertFalse(goGame.play(0, 0));
    assertEquals(StoneColor.Black, goGame.getBoard().getColor(0, 0));
  }

  public void testPlay_capture() {
    GoGame goGame = new GoGame(TEST_BOARDSIZE);
    goGame.getBoard().setColor(0, 0, StoneColor.White);
    goGame.getBoard().setColor(1, 0, StoneColor.White);
    goGame.getBoard().setColor(0, 1, StoneColor.Black);
    goGame.getBoard().setColor(1, 1, StoneColor.Black);
    assertTrue(goGame.play(2, 0));
    assertEquals(StoneColor.Empty, goGame.getBoard().getColor(0,0));
    assertEquals(StoneColor.Empty, goGame.getBoard().getColor(1,0));
  }

  public void testPlay_noLiberties() {
    GoGame goGame = new GoGame(TEST_BOARDSIZE);
    goGame.getBoard().setColor(1, 0, StoneColor.White);
    goGame.getBoard().setColor(0, 1, StoneColor.White);
    assertFalse(goGame.play(0, 0));
  }

  public void testPlay_farLiberties() {
    GoGame goGame = new GoGame(TEST_BOARDSIZE);
    goGame.getBoard().setColor(1, 0, StoneColor.Black);
    goGame.getBoard().setColor(0, 1, StoneColor.Black);
    assertTrue(goGame.play(0, 0));
  }

  public void testPlay_noLibertiesFar() {
    GoGame goGame = new GoGame(TEST_BOARDSIZE);
    goGame.getBoard().setColor(1, 0, StoneColor.Black);
    goGame.getBoard().setColor(0, 1, StoneColor.Black);
    goGame.getBoard().setColor(2, 0, StoneColor.White);
    goGame.getBoard().setColor(1, 1, StoneColor.White);
    goGame.getBoard().setColor(0, 2, StoneColor.White);
    assertFalse(goGame.play(0, 0));
  }
}
