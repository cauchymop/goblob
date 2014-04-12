package com.cauchymop.goblob.model;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {link GoGameSerializer}.
 */
public class GameSerializerTest {

  private GoGame goGame;
  private GameMoveSerializer serializer;

  @Before
  public void setupGoGame() {
    goGame = new GoGame(9);
  }

  @Before
  public void setupSerializer() {
    serializer = new GameMoveSerializer();
  }

  @Test
  public void test_serialize_empty() {
    assertThat(serializer.getDataFromGame(goGame)).isEqualTo("".getBytes());
  }

  @Test
  public void test_serialize() {
    goGame.play(null, 7);
    goGame.play(null, 12);
    assertThat(serializer.getDataFromGame(goGame)).isEqualTo("7 12".getBytes());
  }

  @Test
  public void test_deserialize() {
    serializer.getGameFromData("7 12".getBytes(), goGame);
    assertThat(goGame.getMoveHistory()).containsExactly(7, 12);
  }
}
