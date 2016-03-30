package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import dagger.Lazy;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link GoGameController}.
 */
public class GoGameControllerTest {

  private static final int TEST_HANDICAP = 0;
  private static final float TEST_KOMI = 7.5f;
  private static final GameDatas GAME_DATAS = new GameDatas(new Lazy<String>() {
    @Override
    public String get() {
      return "Pipo";
    }
  }, "Bimbo", null);
  private static final GoPlayer TEST_BLACK_PLAYER = GAME_DATAS.createGamePlayer("blackid", "black");
  private static final GoPlayer TEST_WHITE_PLAYER = GAME_DATAS.createGamePlayer("whiteid", "white");

  @Test
  public void testNew_gameData() {
    PlayGameData.GameConfiguration gameConfiguration = GAME_DATAS.createGameConfiguration(9, TEST_HANDICAP, TEST_KOMI, PlayGameData.GameType.LOCAL, TEST_BLACK_PLAYER, TEST_WHITE_PLAYER);
    GameData gameData = GAME_DATAS.createGameData(GameDatas.LOCAL_MATCH_ID, Phase.IN_GAME, PlayGameData.Color.BLACK, gameConfiguration, ImmutableList.of(GAME_DATAS.createMove(2, 3), GAME_DATAS.createMove(4, 5)), null);
    GoGameController controller = new GoGameController(GAME_DATAS, gameData);

    assertThat(controller.getGameData()).isEqualTo(gameData);
    GoGame goGame = controller.getGame();
    assertThat(goGame.getMoveHistory())
        .containsExactly(goGame.getPos(2, 3), goGame.getPos(4, 5));
  }

  @Test
  public void testGetGameData() {
    PlayGameData.GameConfiguration gameConfiguration = GAME_DATAS.createGameConfiguration(9, TEST_HANDICAP, TEST_KOMI, PlayGameData.GameType.LOCAL, TEST_BLACK_PLAYER, TEST_WHITE_PLAYER);
    GoGameController controller = new GoGameController(GAME_DATAS, GAME_DATAS.createGameData(GameDatas.LOCAL_MATCH_ID, Phase.IN_GAME, gameConfiguration));
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(0, 0));
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(1, 1));
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove());
    assertThat(controller.getGameData()).isEqualTo(GameData.newBuilder()
        .setGameConfiguration(gameConfiguration)
        .addMove(GAME_DATAS.createMove(0, 0))
        .addMove(GAME_DATAS.createMove(1, 1))
        .addMove(GAME_DATAS.createPassMove())
        .setVersion(GAME_DATAS.VERSION)
        .setMatchId(GameDatas.LOCAL_MATCH_ID)
        .setTurn(PlayGameData.Color.WHITE)
        .build());
  }

  private GoGameController createGoGameController(boolean accepted) {
    Phase phase = accepted ? Phase.IN_GAME : Phase.INITIAL;
    return new GoGameController(GAME_DATAS, GAME_DATAS.createGameData(GameDatas.LOCAL_MATCH_ID, phase,
        9, TEST_HANDICAP, 0, PlayGameData.GameType.LOCAL, TEST_BLACK_PLAYER, TEST_WHITE_PLAYER));
  }

  @Test
  public void testToString() {
    GoGameController controller = createGoGameController(false);
    assertThat(controller.toString()).isNotNull();
  }

  @Test
  public void testPlayMove_tooEarly() {
    GoGameController controller = createGoGameController(false);
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(0, 0))).isFalse();
  }

  @Test
  public void testPlayMove_tooLate() {
    GoGameController controller = createGoGameController(true);
    PlayGameData.Move pass = GAME_DATAS.createPassMove();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(0, 0))).isFalse();
  }

  @Test
  public void testUndoRedo() {
    GoGameController controller = createGoGameController(true);
    PlayGameData.Move move = GAME_DATAS.createMove(0, 0);
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue();
    assertThat(controller.undo()).isTrue();
    assertThat(controller.getGameData().getMoveCount()).isZero();
    assertThat(controller.redo()).isTrue();
    assertThat(controller.getGameData().getMoveList())
        .isEqualTo(ImmutableList.of(move));
  }

  @Test
  public void testUndo_empty() {
    GoGameController controller = createGoGameController(true);
    assertThat(controller.undo()).isFalse();
  }

  @Test
  public void testRedoSame() {
    GoGameController controller = createGoGameController(true);
    PlayGameData.Move move1 = GAME_DATAS.createMove(1, 0);
    PlayGameData.Move move2 = GAME_DATAS.createMove(2, 0);
    assertThat(controller.playMoveOrToggleDeadStone(move1)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(move2)).isTrue();
    assertThat(controller.undo()).isTrue();
    assertThat(controller.undo()).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(move1)).isTrue();
    assertThat(controller.redo()).isTrue();
  }

  @Test
  public void testRedoDifferent() {
    GoGameController controller = createGoGameController(true);
    PlayGameData.Move move1 = GAME_DATAS.createMove(1, 0);
    PlayGameData.Move move2 = GAME_DATAS.createMove(2, 0);
    assertThat(controller.playMoveOrToggleDeadStone(move1)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(move2)).isTrue();
    assertThat(controller.undo()).isTrue();
    assertThat(controller.undo()).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(move2)).isTrue();
    assertThat(controller.redo()).isFalse();
  }

  @Test
  public void testCheckForMatchEnd() {
    GoGameController controller = createGoGameController(true);
    assertThat(controller.getPhase()).isEqualTo(Phase.IN_GAME);
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove())).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove())).isTrue();
    assertThat(controller.getPhase()).isEqualTo(Phase.DEAD_STONE_MARKING);
  }

  @Test
  public void testToggleDeadStone() {
    GoGameController controller = createGoGameController(true);
    PlayGameData.Move move = GAME_DATAS.createMove(1, 1);
    PlayGameData.Move pass = GAME_DATAS.createPassMove();
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.getGameData().getMatchEndStatus().getDeadStoneList()).isEmpty();
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue();
    assertThat(controller.getGameData().getMatchEndStatus().getDeadStoneList()).isEqualTo(ImmutableList.of(GAME_DATAS.createPosition(1, 1)));
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue();
    assertThat(controller.getGameData().getMatchEndStatus().getDeadStoneList()).isEmpty();
  }

  @Test
  public void testToggleDeadStone_empty() {
    GoGameController controller = createGoGameController(true);
    PlayGameData.Move pass = GAME_DATAS.createPassMove();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(1, 1))).isFalse();
  }
}
