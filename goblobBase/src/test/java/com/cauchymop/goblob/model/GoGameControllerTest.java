package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link GoGameController}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GoGameControllerTest {

  @Mock
  private Analytics analytics;

  private static final GameDatas GAME_DATAS = new GameDatas();
  private GameData gameData;
  private GoGameController controller;

  @Before
  public void setUp() throws Exception {
    PlayGameData.GoPlayer black = GAME_DATAS.createGamePlayer("pipo", "player1", true);
    PlayGameData.GoPlayer white = GAME_DATAS.createGamePlayer("bimbo", "player2", true);
    gameData = GAME_DATAS.createNewGameData("pizza", PlayGameData.GameType.LOCAL, black, white).toBuilder().setPhase(Phase.IN_GAME).build();
    controller = new GoGameController(GAME_DATAS, analytics);
    controller.setGameData(gameData);
  }

  @Test
  public void testNew_initGoGame() {
    gameData = gameData.toBuilder().addAllMove(ImmutableList.of(GAME_DATAS.createMove(2, 3), GAME_DATAS.createMove(4, 5))).build();
    controller.setGameData(gameData);

    GoGame goGame = controller.getGame();
    assertThat(goGame.getMoveHistory())
        .containsExactly(goGame.getPos(2, 3), goGame.getPos(4, 5));
    assertThat(goGame.getBoardSize())
        .isEqualTo(gameData.getGameConfiguration().getBoardSize());
  }

  @Test
  public void buildGameData_incrementsSequence() {
    gameData = gameData.toBuilder().addAllMove(ImmutableList.of(GAME_DATAS.createMove(2, 3), GAME_DATAS.createMove(4, 5))).setSequenceNumber(3).build();
    controller.setGameData(gameData);

    GameData controllerGameData = controller.getGameData();

    assertThat(controllerGameData.getSequenceNumber()).isEqualTo(4);
  }

  @Test
  public void testPlayMoveOrToggleDeadStone() {
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(0, 0));
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(1, 1));
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove());

    GameData controllerGameData = controller.getGameData();

    assertThat(controllerGameData.getMoveList()).containsExactly(
        GAME_DATAS.createMove(0, 0),
        GAME_DATAS.createMove(1, 1),
        GAME_DATAS.createPassMove());
    assertThat(controllerGameData.getTurn()).isEqualTo(PlayGameData.Color.WHITE);
  }

  @Test
  public void testToString() {
    assertThat(controller.toString()).isNotNull();
  }

  @Test
  public void testPlayMove_tooLate() {
    PlayGameData.Move pass = GAME_DATAS.createPassMove();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(0, 0))).isFalse();
  }

  @Test
  public void testUndoRedo() {
    PlayGameData.Move move = GAME_DATAS.createMove(0, 0);
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue();
    assertThat(controller.undo()).isTrue();
    assertThat(controller.getGameData().getMoveList()).isEmpty();
    assertThat(controller.redo()).isTrue();
    assertThat(controller.getGameData().getMoveList())
        .isEqualTo(ImmutableList.of(move));
  }

  @Test
  public void testUndo_empty() {
    assertThat(controller.undo()).isFalse();
  }

  @Test
  public void testRedoSame() {
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
    assertThat(controller.getPhase()).isEqualTo(Phase.IN_GAME);
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove())).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove())).isTrue();
    assertThat(controller.getPhase()).isEqualTo(Phase.DEAD_STONE_MARKING);
  }

  @Test
  public void testToggleDeadStone() {
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
    PlayGameData.Move pass = GAME_DATAS.createPassMove();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue();
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(1, 1))).isFalse();
  }
}
