package com.cauchymop.goblob.model;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link GoGame}.
 */
public class GoGameTest {

  private GoGame goGame;

  @Before
  public void setupGoGame() {
    GoPlayer blackPlayer = new GoPlayer(Player.PlayerType.HUMAN_LOCAL, "blackPlayer");
    GoPlayer whitePlayer = new GoPlayer(Player.PlayerType.HUMAN_LOCAL, "whitePlayer");
    goGame = new GoGame(9, blackPlayer, whitePlayer);
  }

  @Test
  public void test_Serializable() {
    GoGame goGameAfterSerialization = reserialize(goGame);

    assertThat(goGameAfterSerialization.getBoardSize()).isEqualTo(9);
  }

  @Test
  public void test_GetMoveHistory_noMoves() {
    assertThat(goGame.getMoveHistory()).isEmpty();
  }

  @Test
  public void test_GetMoveHistory() {
    goGame.play(null, 11);
    goGame.play(null, 17);
    assertThat(goGame.getMoveHistory()).containsExactly(11, 17);
  }

  /**
   * Serializes and deserializes the specified object.
   *
   * @return the re-serialized object
   * @throws RuntimeException if the specified object was not successfully
   *                          serialized or deserialized
   */
  @SuppressWarnings("unchecked")
  public static <T> T reserialize(T object) {
    assertNotNull(object);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(object);
      ObjectInputStream in = new ObjectInputStream(
          new ByteArrayInputStream(bytes.toByteArray()));
      return (T) in.readObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
