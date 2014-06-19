package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

import org.junit.Test;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static org.fest.assertions.Assertions.assertThat;


/**
 * Tests for {@link GoGameController}.
 */
public class GoGameControllerTest {

  private static final int TEST_HANDICAP = 0;
  private static final String TEST_BLACK_ID = "black";
  private static final String TEST_WHITE_ID = "white";

  @Test
  public void testNew_gameData() {
    GameData gameData = GameData.newBuilder()
        .setGameConfiguration(GameDatas.createGameConfiguration(9, TEST_HANDICAP, TEST_BLACK_ID, TEST_WHITE_ID))
        .addMove(GameDatas.createMove(2, 3))
        .addMove(GameDatas.createMove(4, 5))
        .build();

    GoGameController controller = new GoGameController(gameData);

    assertThat(controller.getGameData()).isEqualTo(gameData);
    GoGame goGame = controller.getGame();
    assertThat(goGame.getMoveHistory())
        .containsExactly(goGame.getPos(2, 3), goGame.getPos(4, 5));
  }

  @Test
  public void testGetGameData() {
    PlayGameData.GameConfiguration gameConfiguration = GameDatas.createGameConfiguration(9, TEST_HANDICAP, TEST_BLACK_ID, TEST_WHITE_ID);
    GoGameController controller = new GoGameController(GameData.newBuilder()
        .setGameConfiguration(gameConfiguration)
        .build());
    controller.play(0, 0);
    controller.play(1, 1);
    controller.pass();
    assertThat(controller.getGameData()).isEqualTo(GameData.newBuilder()
        .setGameConfiguration(gameConfiguration)
        .addMove(GameDatas.createMove(0, 0))
        .addMove(GameDatas.createMove(1, 1))
        .addMove(GameDatas.createPassMove())
        .build());
  }

  private GoGameController createGoGameController() {
    return new GoGameController(GameDatas.createGameData(9, TEST_HANDICAP, TEST_BLACK_ID, TEST_WHITE_ID));
  }

  @Test
  public void testToString() {
    GoGameController controller = createGoGameController();
    assertThat(controller.toString()).isNotNull();
  }
}
