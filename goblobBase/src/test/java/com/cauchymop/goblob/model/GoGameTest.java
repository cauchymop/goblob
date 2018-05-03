package com.cauchymop.goblob.model;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.SerializableTester;

import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.Color;
import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link GoGame}.
 */
public class GoGameTest {

  @Test
  public void test_Serializable() {
    GoGame goGame = new GoGame(9, 0);
    goGame.play(7);
    GoGame goGameAfterSerialization = SerializableTester.reserialize(goGame);

    assertThat(goGameAfterSerialization).isEqualTo(goGame);
  }

  @Test
  public void test_Serializable_empty() {
    GoGame goGame = new GoGame(9, 0);
    GoGame goGameAfterSerialization = SerializableTester.reserialize(goGame);

    assertThat(goGameAfterSerialization).isEqualTo(goGame);
  }

  @Test
  public void testGetMoveHistory_noMoves() {
    GoGame goGame = new GoGame(9, 0);
    assertThat(goGame.getMoveHistory()).isEmpty();
  }

  @Test
  public void testGetMoveHistory() {
    GoGame goGame = new GoGame(9, 0);
    goGame.play(11);
    goGame.play(17);
    assertThat(goGame.getMoveHistory()).containsExactly(11, 17);
  }

  @Test
  public void testIsGameEnd() {
    GoGame goGame = new GoGame(9, 0);
    assertThat(goGame.isGameEnd()).isFalse();

    goGame.play(2);
    assertThat(goGame.isGameEnd()).isFalse();

    goGame.play(7);
    assertThat(goGame.isGameEnd()).isFalse();

    goGame.play(goGame.getPassValue());
    assertThat(goGame.isGameEnd()).isFalse();

    goGame.play(goGame.getPassValue());
    assertThat(goGame.isGameEnd()).isTrue();
  }

  @Test
  public void testUndo() {
    GoGame goGame = new GoGame(9, 0);
    goGame.play(2);
    goGame.undo();

    assertThat(goGame).isEqualTo(new GoGame(9, 0));
  }

  @Test
  public void testEquals() {
    GoGame goGame9A = new GoGame(9, 0);
    GoGame goGame9B = new GoGame(9, 0);
    GoGame goGame9C = goGame9A.copy();

    GoGame goGame13A = new GoGame(13, 0);
    GoGame goGame13B = new GoGame(13, 0);
    GoGame goGame13C = goGame13A.copy();

    GoGame goGame19A = new GoGame(19, 0);
    GoGame goGame19B = new GoGame(19, 0);
    GoGame goGame19C = goGame19A.copy();

    GoGame goGameMoveA = new GoGame(19, 0);
    goGameMoveA.play(7);
    GoGame goGameMoveB = new GoGame(19, 0);
    goGameMoveB.play(7);
    GoGame goGameMoveC = goGameMoveA.copy();

    GoGame goGameOtherMoveA = new GoGame(19, 0);
    goGameOtherMoveA.play(11);
    GoGame goGameOtherMoveB = new GoGame(19, 0);
    goGameOtherMoveB.play(11);
    GoGame goGameOtherMoveC = goGameOtherMoveA.copy();

    new EqualsTester()
        .addEqualityGroup(goGame9A, goGame9B, goGame9C)
        .addEqualityGroup(goGame13A, goGame13B, goGame13C)
        .addEqualityGroup(goGame19A, goGame19B, goGame19C)
        .addEqualityGroup(goGameMoveA, goGameMoveB, goGameMoveC)
        .addEqualityGroup(goGameOtherMoveA, goGameOtherMoveB, goGameOtherMoveC)
        .testEquals();
  }

  @Test
  public void testPlay_move() {
    GoGame goGame = new GoGame(9, 0);
    assertThat(goGame.play(3)).isTrue();
    assertThat(goGame.getMoveHistory()).containsExactly(3);
    assertThat(goGame.getColor(3, 0)).isEqualTo(Color.BLACK);
    assertThat(goGame.getCurrentColor()).isEqualTo(Color.WHITE);
    assertThat(goGame.isLastMovePass()).isFalse();
  }

  @Test
  public void testPlay_ko() {
    GoGame goGame = new GoGame(9, 0);
    goGame.play(3);
    assertThat(goGame.getMoveHistory()).containsExactly(3);
    assertThat(goGame.getColor(3, 0)).isEqualTo(Color.BLACK);
    assertThat(goGame.getCurrentColor()).isEqualTo(Color.WHITE);
    assertThat(goGame.isLastMovePass()).isFalse();
  }

  @Test
  public void testPlay_pass() {
    GoGame goGame = new GoGame(9, 0);
    assertThat(goGame.play(goGame.getPassValue())).isTrue();
    assertThat(goGame.getMoveHistory()).containsExactly(goGame.getPassValue());
    assertThat(goGame.getCurrentColor()).isEqualTo(Color.WHITE);
    assertThat(goGame.isLastMovePass()).isTrue();
  }

  @Test
  public void testPlay_invalid() {
    GoGame goGame = new GoGame(9, 0);
    goGame.play(3);
    assertThat(goGame.play(3)).isFalse();
  }

  @Test
  public void testHandicap9() {
    GoGame goGame = new GoGame(9, 9);
    assertThat(TextBoard.toString(goGame.getBoard())).isEqualTo(
        ".........\n" +
        ".........\n" +
        "..●.●.●..\n" +
        ".........\n" +
        "..●.●.●..\n" +
        ".........\n" +
        "..●.●.●..\n" +
        ".........\n" +
        ".........\n"
    );
  }
}
