package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Tests for {@link GoGameController}.
 */
public class GoGameControllerTest {

  @Test
  public void testGetGameData() {
    GoGameController controller = new GoGameController(PlayGameData.GameData.getDefaultInstance(), 9);
    DummyPlayerController whiteController = new DummyPlayerController();
    controller.setWhiteController(whiteController);
    DummyPlayerController blackController = new DummyPlayerController();
    controller.setBlackController(blackController);
    controller.play(blackController, 0, 0);
    controller.play(whiteController, 1, 1);
    assertThat(controller.getGameData()).isEqualTo(PlayGameData.GameData.newBuilder().addMove(0).addMove(10).build());
  }

  private static class DummyPlayerController extends PlayerController {
    @Override
    public void startTurn() {
    }
  }
}
