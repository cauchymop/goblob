package com.cauchymop.goblob.model;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {link GoGameSerializer}.
 */
public class GameSerializerTest {

  private GoGame goGame;
  private GameMoveSerializer<GoGame> serializer;

  @Before
  public void setupGoGame() {
    goGame = new GoGame(9);
  }

  @Before
  public void setupSerializer() {
    serializer = new GameMoveSerializer<GoGame>();
  }

  @Test
  public void test_serialize_empty() {
    assertThat(serializer.serialize(goGame)).isEqualTo("".getBytes());
  }

  @Test
  public void test_serialize() {
    goGame.play(null, 7);
    goGame.play(null, 12);
    assertThat(serializer.serialize(goGame)).isEqualTo("7 12".getBytes());
  }

  @Test
  public void test_deserialize() {
    serializer.deserializeTo("7 12".getBytes(), goGame);
    assertThat(goGame.getMoveHistory()).containsExactly(7, 12);
  }
}
