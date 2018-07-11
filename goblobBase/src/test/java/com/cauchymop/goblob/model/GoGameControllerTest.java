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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link GoGameController}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GoGameControllerTest {

  @Mock
  private Analytics analytics;

  private static final String MATCH_ID = "pizza";
  private static final GameDatas GAME_DATAS = new GameDatas();
  private static final PlayGameData.GoPlayer WHITE_PLAYER = GAME_DATAS.createGamePlayer("bimbo", "player2", true);
  private static final PlayGameData.GoPlayer BLACK_PLAYER = GAME_DATAS.createGamePlayer("pipo", "player1", true);
  private static final String ANOTHER_PLAYER_NAME = "myname";

  private GameData.Builder gameDataBuilder;
  private GameData gameData;
  private GoGameController controller;


  @Before
  public void setUp() throws Exception {
    gameDataBuilder = GAME_DATAS.createNewGameData(MATCH_ID, PlayGameData.GameType.LOCAL, BLACK_PLAYER, WHITE_PLAYER).toBuilder();
    gameData = gameDataBuilder.setPhase(Phase.IN_GAME).build();
    controller = new GoGameController(GAME_DATAS, analytics);
    controller.setGameData(gameData);
  }

  @Test
  public void testNew_initGoGame() {
    gameData = gameData.toBuilder().addAllMove(ImmutableList.of(GAME_DATAS.createMove(2, 3), GAME_DATAS.createMove(4, 5))).build();
    controller.setGameData(gameData);

    assertThat(controller.getColor(2,3)).isEqualTo(PlayGameData.Color.BLACK);
    assertThat(controller.getColor(4,5)).isEqualTo(PlayGameData.Color.WHITE);
    assertThat(controller.getBoardSize()).isEqualTo(gameData.getGameConfiguration().getBoardSize());
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
  public void resign() {
    controller.resign();

    assertThat(controller.getPhase()).isEqualTo(Phase.FINISHED);
    assertThat(controller.getWinner()).isEqualTo(WHITE_PLAYER);
    PlayGameData.Score score = PlayGameData.Score.newBuilder().setWinner(PlayGameData.Color.WHITE).setResigned(true).build();
    verify(analytics).gameFinished(any(PlayGameData.GameConfiguration.class), eq(score));
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

  @Test
  public void commitConfiguration_configNotChanged_noHandicap_startsGame() {
    GameData initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION).build();
    controller.setGameData(initialGameData);
    assertThat(controller.getPhase()).isEqualTo(Phase.CONFIGURATION);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.BLACK);

    controller.commitConfiguration();

    assertThat(controller.getPhase()).isEqualTo(Phase.IN_GAME);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.BLACK);
    verify(analytics).configurationChanged(any(GameData.class));
  }

  @Test
  public void commitConfiguration_configNotChanged_handicap_startsGame() {
    GameData.Builder initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION);
    initialGameData.getGameConfigurationBuilder().setHandicap(2);
    controller.setGameData(initialGameData.build());
    assertThat(controller.getPhase()).isEqualTo(Phase.CONFIGURATION);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.BLACK);

    controller.commitConfiguration();

    assertThat(controller.getPhase()).isEqualTo(Phase.IN_GAME);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.WHITE);
    verify(analytics).configurationChanged(any(GameData.class));
  }

  @Test
  public void commitConfiguration_local_configChanged() {
    GameData initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION).build();
    controller.setGameData(initialGameData);
    assertThat(controller.getPhase()).isEqualTo(Phase.CONFIGURATION);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.BLACK);

    controller.setBoardSize(6);
    controller.commitConfiguration();

    assertThat(controller.getPhase()).isEqualTo(Phase.IN_GAME);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.BLACK);
    verify(analytics).configurationChanged(any(GameData.class));
  }

  @Test
  public void commitConfiguration_remote_configChanged() {
    GameData.Builder initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION);
    initialGameData.getGameConfigurationBuilder().setGameType(PlayGameData.GameType.REMOTE);
    controller.setGameData(initialGameData.build());
    assertThat(controller.getPhase()).isEqualTo(Phase.CONFIGURATION);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.BLACK);

    controller.setBoardSize(6);
    controller.commitConfiguration();

    assertThat(controller.getPhase()).isEqualTo(Phase.CONFIGURATION);
    assertThat(controller.getCurrentColor()).isEqualTo(PlayGameData.Color.WHITE);
    verify(analytics).configurationChanged(any(GameData.class));
  }

  @Test
  public void swapPlayer() {
    assertThat(controller.getPlayerForColor(PlayGameData.Color.BLACK)).isEqualTo(BLACK_PLAYER);
    assertThat(controller.getPlayerForColor(PlayGameData.Color.WHITE)).isEqualTo(WHITE_PLAYER);

    controller.swapPlayers();

    assertThat(controller.getPlayerForColor(PlayGameData.Color.BLACK)).isEqualTo(WHITE_PLAYER);
    assertThat(controller.getPlayerForColor(PlayGameData.Color.WHITE)).isEqualTo(BLACK_PLAYER);
  }

  @Test
  public void getMatchId() {
    controller.setGameData(gameDataBuilder.setMatchId(MATCH_ID).build());
    assertThat(controller.getMatchId()).isEqualTo(MATCH_ID);
  }

  @Test
  public void setWhitePlayerName() {
    controller.setWhitePlayerName(ANOTHER_PLAYER_NAME);
    assertThat(controller.getPlayerForColor(PlayGameData.Color.WHITE).getName()).isEqualTo(ANOTHER_PLAYER_NAME);
  }

  @Test
  public void setBlackPlayerName() {
    controller.setBlackPlayerName(ANOTHER_PLAYER_NAME);
    assertThat(controller.getPlayerForColor(PlayGameData.Color.BLACK).getName()).isEqualTo(ANOTHER_PLAYER_NAME);
  }

  @Test
  public void pass() {
    assertThat(controller.isLastMovePass()).isFalse();

    controller.pass();

    assertThat(controller.isLastMovePass()).isTrue();
  }
}
