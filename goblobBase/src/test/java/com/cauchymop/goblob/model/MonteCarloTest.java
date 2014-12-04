package com.cauchymop.goblob.model;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

/**
 * Tests for {@link MonteCarlo}.
 */
public class MonteCarloTest {

  @Test
  public void testGetBestMove_endGame() {
    GoGame game = new GoGame(6);
    TextBoard.fillBoard(game.getBoard(),
        ". ● ● ○ ○ .\n" +
        "● ● ● ● ○ ○\n" +
        ". ● ● ● ○ .\n" +
        ". ● ● ○ ○ .\n" +
        "● ● ● ○ ○ ○\n" +
        "● ● . . . .\n");
    assertThat(MonteCarlo.getBestMove(game, 100)).isEqualTo(33);
  }

}
