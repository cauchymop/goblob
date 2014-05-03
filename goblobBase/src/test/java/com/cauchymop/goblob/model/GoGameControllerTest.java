package com.cauchymop.goblob.model;

import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


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

  @Test
  public void testToString() {
    GoGameController controller = new GoGameController(GameData.getDefaultInstance(), 9);
    assertThat(controller.toString()).isNotNull();
  }

  @Test
  public void testFireGameChanged() {
    GoGameController controller = new GoGameController(GameData.getDefaultInstance(), 9);
    GoGameController.Listener mockListener1 = mock(GoGameController.Listener.class);
    GoGameController.Listener mockListener2 = mock(GoGameController.Listener.class);
    controller.addListener(mockListener1);
    controller.addListener(mockListener2);
    controller.fireGameChanged();

    verify(mockListener1).gameChanged(controller);
    verify(mockListener2).gameChanged(controller);
  }

  private static class DummyPlayerController extends PlayerController {
    @Override
    public void startTurn() {
    }
  }
}
