package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static org.fest.assertions.Assertions.assertThat;


/**
 * Tests for {@link GoGameController}.
 */
public class GoGameControllerTest {

  @Test
  public void testGetGameData() {
    GoGameController controller = new GoGameController(GameData.getDefaultInstance(), 9);
    DummyPlayerController whiteController = new DummyPlayerController();
    controller.setWhiteController(whiteController);
    DummyPlayerController blackController = new DummyPlayerController();
    controller.setBlackController(blackController);
    controller.play(blackController, 0, 0);
    controller.play(whiteController, 1, 1);
    controller.pass(blackController);
    assertThat(controller.getGameData()).isEqualTo(GameData.newBuilder()
        .addMove(GameDatas.createMove(0, 0))
        .addMove(GameDatas.createMove(1, 1))
        .addMove(GameDatas.createPassMove())
        .build());
  }

  private static class DummyPlayerController extends PlayerController {
    @Override
    public void startTurn() {
    }
  }
}
