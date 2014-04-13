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
    GoGame after = serializer.getGameFromData(serializer.getDataFromGame(this.goGame), 9);
    assertThat(after).isEqualTo(goGame);
  }

  @Test
  public void test_serialize() {
    goGame.play(7);
    goGame.play(12);
    GoGame after = serializer.getGameFromData(serializer.getDataFromGame(this.goGame), 9);
    assertThat(after).isEqualTo(goGame);
  }
}
