package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.Position;
import static org.fest.assertions.Assertions.assertThat;

public class ScoreGeneratorTest {

  public static final ImmutableList<Position> EMPTY_LIST = ImmutableList.of();
  private static final float TEST_KOMI = 7.5f;
  private GoBoard board;

  @Before
  public void setUp() throws Exception {
    board = new GoBoard(5);
  }

  @Test
  public void testGetTerritories_empty() throws Exception {
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_LIST, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).isEmpty();
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_oneStone() throws Exception {
    board.play(StoneColor.Black, 0);
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_LIST, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).hasSize(24);
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_mixed() throws Exception {
    board.play(StoneColor.Black, 0);
    board.play(StoneColor.White, 1);
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_LIST, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).isEmpty();
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_mixed_prisoner() throws Exception {
    board.play(StoneColor.Black, 0);
    board.play(StoneColor.White, 1);
    PlayGameData.Score score = new ScoreGenerator(board, ImmutableList.of(getPosition(0, 0)), TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).isEmpty();
    assertThat(score.getWhiteTerritoryList()).hasSize(24);
  }

  @Test
  public void testGetTerritories_enclosed() throws Exception {
    board.play(StoneColor.Black, board.getPos(0, 1));
    board.play(StoneColor.Black, board.getPos(1, 1));
    board.play(StoneColor.Black, board.getPos(2, 1));
    board.play(StoneColor.Black, board.getPos(2, 0));
    board.play(StoneColor.White, board.getPos(3, 0));  // Make outside neutral.
    PlayGameData.Score score = new ScoreGenerator(board, EMPTY_LIST, TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).hasSize(2);
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  @Test
  public void testGetTerritories_enclosed_prisoner() throws Exception {
    board.play(StoneColor.Black, board.getPos(0, 1));
    board.play(StoneColor.Black, board.getPos(1, 1));
    board.play(StoneColor.Black, board.getPos(2, 1));
    board.play(StoneColor.Black, board.getPos(2, 0));
    board.play(StoneColor.White, board.getPos(0, 0));  // Prisoner.
    board.play(StoneColor.White, board.getPos(3, 0));  // Make outside neutral.
    PlayGameData.Score score = new ScoreGenerator(board, ImmutableList.of(getPosition(0, 0)), TEST_KOMI).getScore();
    assertThat(score.getBlackTerritoryList()).hasSize(2);
    assertThat(score.getWhiteTerritoryList()).isEmpty();
  }

  private Position getPosition(int x, int y) {
    return Position.newBuilder().setX(x).setY(y).build();
  }
}