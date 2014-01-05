package com.cauchymop.goblob.model;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link GoGame}.
 */
public class GoGameTest {
  @Test
  public void test_Serializable() {
    GoPlayer blackPlayer = new GoPlayer(Player.PlayerType.HUMAN_LOCAL, "blackPlayer");
    GoPlayer whitePlayer = new GoPlayer(Player.PlayerType.HUMAN_LOCAL, "whitePlayer");
    GoGame goGame = new GoGame(9, blackPlayer, whitePlayer);

    GoGame goGameAfterSerialization = reserialize(goGame);

    assertEquals(9, goGame.getBoardSize());
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
