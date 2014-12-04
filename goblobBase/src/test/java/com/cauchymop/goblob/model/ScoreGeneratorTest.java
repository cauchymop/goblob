package com.cauchymop.goblob.model;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.Position;
import static org.fest.assertions.Assertions.assertThat;

public class ScoreGeneratorTest {

  public static final ImmutableList<Position> EMPTY_LIST = ImmutableList.of();
  private GoBoard board;

  @Before
  public void setUp() throws Exception {
    board = new GoBoard(5);
  }

  @Test
  public void testGetTerritories_empty() throws Exception {
    ScoreGenerator territories = new ScoreGenerator(board, EMPTY_LIST);
    assertThat(territories.getTerritories(StoneColor.Black)).isEmpty();
    assertThat(territories.getTerritories(StoneColor.White)).isEmpty();
  }

  @Test
  public void testGetTerritories_oneStone() throws Exception {
    board.play(StoneColor.Black, 0);
    ScoreGenerator territories = new ScoreGenerator(board, EMPTY_LIST);
    assertThat(territories.getTerritories(StoneColor.Black)).hasSize(24);
    assertThat(territories.getTerritories(StoneColor.White)).isEmpty();
  }

  @Test
  public void testGetTerritories_mixed() throws Exception {
    board.play(StoneColor.Black, 0);
    board.play(StoneColor.White, 1);
    ScoreGenerator territories = new ScoreGenerator(board, EMPTY_LIST);
    assertThat(territories.getTerritories(StoneColor.Black)).isEmpty();
    assertThat(territories.getTerritories(StoneColor.White)).isEmpty();
  }

  @Test
  public void testGetTerritories_mixed_prisoner() throws Exception {
    board.play(StoneColor.Black, 0);
    board.play(StoneColor.White, 1);
    ScoreGenerator territories = new ScoreGenerator(board, ImmutableList.of(getPosition(0, 0)));
    assertThat(territories.getTerritories(StoneColor.Black)).isEmpty();
    assertThat(territories.getTerritories(StoneColor.White)).hasSize(24);
  }

  @Test
  public void testGetTerritories_enclosed() throws Exception {
    board.play(StoneColor.Black, board.getPos(0, 1));
    board.play(StoneColor.Black, board.getPos(1, 1));
    board.play(StoneColor.Black, board.getPos(2, 1));
    board.play(StoneColor.Black, board.getPos(2, 0));
    board.play(StoneColor.White, board.getPos(3, 0));  // Make outside neutral.
    ScoreGenerator territories = new ScoreGenerator(board, EMPTY_LIST);
    assertThat(territories.getTerritories(StoneColor.Black)).hasSize(2);
    assertThat(territories.getTerritories(StoneColor.White)).isEmpty();
  }

  @Test
  public void testGetTerritories_enclosed_prisoner() throws Exception {
    board.play(StoneColor.Black, board.getPos(0, 1));
    board.play(StoneColor.Black, board.getPos(1, 1));
    board.play(StoneColor.Black, board.getPos(2, 1));
    board.play(StoneColor.Black, board.getPos(2, 0));
    board.play(StoneColor.White, board.getPos(0, 0));  // Prisoner.
    board.play(StoneColor.White, board.getPos(3, 0));  // Make outside neutral.
    ScoreGenerator territories = new ScoreGenerator(board, ImmutableList.of(getPosition(0, 0)));
    assertThat(territories.getTerritories(StoneColor.Black)).hasSize(2);
    assertThat(territories.getTerritories(StoneColor.White)).isEmpty();
  }

  private Position getPosition(int x, int y) {
    return Position.newBuilder().setX(x).setY(y).build();
  }
}
