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
    GoPlayer blackPlayer = new GoPlayer(Player.PlayerType.HUMAN_LOCAL, "blackPlayer");
    GoPlayer whitePlayer = new GoPlayer(Player.PlayerType.HUMAN_LOCAL, "whitePlayer");
    goGame = new GoGame(9, blackPlayer, whitePlayer);
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
