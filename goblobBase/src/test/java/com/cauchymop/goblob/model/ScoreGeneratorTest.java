package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.Color;
import static com.cauchymop.goblob.proto.PlayGameData.Position;
import static com.google.common.truth.Truth.assertThat;

public class ScoreGeneratorTest {

  public static final ImmutableSet<Position> EMPTY_SET = ImmutableSet.of();
  private static final float TEST_KOMI = 7.5f;
  private GoBoard board;

  @Before
  public void setUp() throws Exception {
    board = new GoBoard(5);
  }

  @Test
  public void testGetTerritories_empty() throws Exception {
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_SET, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).isEmpty();
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_oneStone() throws Exception {
    board.play(Color.BLACK, 0);
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_SET, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).hasSize(24);
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_mixed() throws Exception {
    board.play(Color.BLACK, 0);
    board.play(Color.WHITE, 1);
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_SET, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).isEmpty();
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_mixed_prisoner() throws Exception {
    board.play(Color.BLACK, 0);
    board.play(Color.WHITE, 1);
    PlayGameData.Score score = new ScoreGenerator(board, ImmutableSet.of(getPosition(0, 0)), TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).isEmpty();
    assertThat(score.getWhiteTerritoryList()).hasSize(24);
  }

  @Test
  public void testGetTerritories_enclosed() throws Exception {
    board.play(Color.BLACK, board.getPos(0, 1));
    board.play(Color.BLACK, board.getPos(1, 1));
    board.play(Color.BLACK, board.getPos(2, 1));
    board.play(Color.BLACK, board.getPos(2, 0));
    board.play(Color.WHITE, board.getPos(3, 0));  // Make outside neutral.
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_SET, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).hasSize(2);
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_enclosed_prisoner() throws Exception {
    board.play(Color.BLACK, board.getPos(0, 1));
    board.play(Color.BLACK, board.getPos(1, 1));
    board.play(Color.BLACK, board.getPos(2, 1));
    board.play(Color.BLACK, board.getPos(2, 0));
    board.play(Color.WHITE, board.getPos(0, 0));  // Prisoner.
    board.play(Color.WHITE, board.getPos(3, 0));  // Make outside neutral.
    PlayGameData.Score score = new ScoreGenerator(board, ImmutableSet.of(getPosition(0, 0)), TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).hasSize(2);
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_enclosed_ownPrisoner() throws Exception {
    board.play(Color.BLACK, board.getPos(0, 1));
    board.play(Color.BLACK, board.getPos(1, 1));
    board.play(Color.BLACK, board.getPos(2, 1));
    board.play(Color.BLACK, board.getPos(2, 0));
    board.play(Color.BLACK, board.getPos(0, 0));  // Prisoner of the same color.
    board.play(Color.WHITE, board.getPos(3, 0));  // Make outside neutral.
    PlayGameData.Score score = new ScoreGenerator(board, ImmutableSet.of(getPosition(0, 0)), TEST_KOMI).getScore();
    // Article 3 of the Geneva convention: [prisonners] shall in all circumstances be treated [...]
    // without any adverse distinction founded on [...] colour"
    assertThat(score.getBlackTerritoryList()).hasSize(2);
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetScore() {
    // 10 points of territory+stones for black.
    board.play(Color.BLACK, board.getPos(1,0));
    board.play(Color.BLACK, board.getPos(1,1));
    board.play(Color.BLACK, board.getPos(1,2));
    board.play(Color.BLACK, board.getPos(1,3));
    board.play(Color.BLACK, board.getPos(1,4));

    // 15 points of territory+stones for white.
    board.play(Color.WHITE, board.getPos(2,0));
    board.play(Color.WHITE, board.getPos(2,1));
    board.play(Color.WHITE, board.getPos(2,2));
    board.play(Color.WHITE, board.getPos(2,3));
    board.play(Color.WHITE, board.getPos(2,4));

    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_SET, TEST_KOMI).getScore();
    assertThat(score.getWinner()).isEqualTo(Color.WHITE);
    assertThat(score.getWonBy()).isEqualTo(12.5f);
  }

  @Test
  public void testGetScore_aliveStones() {
    // 10 points of territory+stones for black.
    board.play(Color.BLACK, board.getPos(1,0));
    board.play(Color.BLACK, board.getPos(1,1));
    board.play(Color.BLACK, board.getPos(1,2));
    board.play(Color.BLACK, board.getPos(1,3));
    board.play(Color.BLACK, board.getPos(1,4));

    // 15 points of territory+stones for white.
    board.play(Color.WHITE, board.getPos(2,0));
    board.play(Color.WHITE, board.getPos(2,1));
    board.play(Color.WHITE, board.getPos(2,2));
    board.play(Color.WHITE, board.getPos(2,3));
    board.play(Color.WHITE, board.getPos(2,4));

    // Extra stones.
    board.play(Color.WHITE, board.getPos(3,2));
    board.play(Color.WHITE, board.getPos(3,3));

    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_SET, TEST_KOMI).getScore();
    assertThat(score.getWinner()).isEqualTo(Color.WHITE);
    assertThat(score.getWonBy()).isEqualTo(12.5f);
  }

  @Test
  public void testGetScore_deadStones() {
    // 10 points of territory+stones for black.
    board.play(Color.BLACK, board.getPos(1,0));
    board.play(Color.BLACK, board.getPos(1,1));
    board.play(Color.BLACK, board.getPos(1,2));
    board.play(Color.BLACK, board.getPos(1,3));
    board.play(Color.BLACK, board.getPos(1,4));

    // 15 points of territory+stones for white.
    board.play(Color.WHITE, board.getPos(2,0));
    board.play(Color.WHITE, board.getPos(2,1));
    board.play(Color.WHITE, board.getPos(2,2));
    board.play(Color.WHITE, board.getPos(2,3));
    board.play(Color.WHITE, board.getPos(2,4));

    // Extra dead stones.
    board.play(Color.BLACK, board.getPos(3,2));
    board.play(Color.BLACK, board.getPos(3,3));

    PlayGameData.Score score = new ScoreGenerator(board, ImmutableSet.of(getPosition(3, 2), getPosition(3, 3)), TEST_KOMI).getScore();
    assertThat(score.getWinner()).isEqualTo(Color.WHITE);
    assertThat(score.getWonBy()).isEqualTo(12.5f);
  }

  private Position getPosition(int x, int y) {
    return Position.newBuilder().setX(x).setY(y).build();
  }
}
