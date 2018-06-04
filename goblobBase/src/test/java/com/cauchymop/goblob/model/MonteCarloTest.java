package com.cauchymop.goblob.model;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link MonteCarlo}.
 */
public class MonteCarloTest {

  @Test @Ignore
  public void testGetBestMove_endGame() {
    GoGame game = new GoGame(6, 0);
    TextBoard.INSTANCE.fillBoard(game.getBoard(),
        ". ● ● ○ ○ .\n" +
        "● ● ● ● ○ ○\n" +
        ". ● ● ● ○ .\n" +
        ". ● ● ○ ○ .\n" +
        "● ● ● ○ ○ ○\n" +
        "● ● . . . .\n");
    assertThat(MonteCarlo.getBestMove(game, 100)).isEqualTo(33);
  }

}
