package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase
import com.google.common.collect.ImmutableList

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import com.cauchymop.goblob.proto.PlayGameData.GameData
import com.google.common.truth.Truth.assertThat
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import kotlin.test.assertFailsWith

/**
 * Tests for [GoGameController].
 */
@RunWith(MockitoJUnitRunner::class)
class GoGameControllerTest {

  @Mock
  private lateinit var analytics: Analytics

  private lateinit var gameDataBuilder: GameData.Builder
  private lateinit var gameData: GameData
  private lateinit var controller: GoGameController


  @Before
  @Throws(Exception::class)
  fun setUp() {
    gameDataBuilder = GAME_DATAS.createNewGameData(MATCH_ID, PlayGameData.GameType.LOCAL, BLACK_PLAYER, WHITE_PLAYER).toBuilder()
    gameData = gameDataBuilder.setPhase(Phase.IN_GAME).build()
    controller = GoGameController(GAME_DATAS, analytics)
    controller.gameData = gameData
  }

  @Test
  fun testNew_initGoGame() {
    gameData = gameData.toBuilder().addAllMove(ImmutableList.of<PlayGameData.Move>(GAME_DATAS.createMove(2, 3), GAME_DATAS.createMove(4, 5))).build()
    controller.gameData = gameData

    assertThat(controller.getColor(2, 3)).isEqualTo(PlayGameData.Color.BLACK)
    assertThat(controller.getColor(4, 5)).isEqualTo(PlayGameData.Color.WHITE)
    assertThat(controller.boardSize).isEqualTo(gameData.gameConfiguration.boardSize)
  }

  @Test
  fun buildGameData_incrementsSequence() {
    gameData = gameData.toBuilder().addAllMove(ImmutableList.of(GAME_DATAS.createMove(2, 3), GAME_DATAS.createMove(4, 5))).setSequenceNumber(3).build()
    controller.gameData = gameData

    val controllerGameData = controller.gameData

    assertThat(controllerGameData.sequenceNumber).isEqualTo(4)
  }

  @Test
  fun testPlayMoveOrToggleDeadStone() {
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(0, 0))
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(1, 1))
    controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove())

    val controllerGameData = controller.gameData

    assertThat(controllerGameData.moveList).containsExactly(
        GAME_DATAS.createMove(0, 0),
        GAME_DATAS.createMove(1, 1),
        GAME_DATAS.createPassMove())
    assertThat(controllerGameData.turn).isEqualTo(PlayGameData.Color.WHITE)
  }

  @Test
  fun testPlayMoveOrToggleDeadStone_invalidMove() {
    val move = GAME_DATAS.createMove(0, 0)
    var played = controller.playMoveOrToggleDeadStone(move)
    assertThat(played).isTrue()

    // Play same move again
    played = controller.playMoveOrToggleDeadStone(move)

    assertThat(played).isFalse()
    assertThat(controller.gameData.moveList).hasSize(1)
  }

  @Test
  fun testPlayMoveOrToggleDeadStone_invalidPhase() {
    controller.gameData = gameData.toBuilder().setPhase(Phase.INITIAL).build()
    val move = GAME_DATAS.createMove(0, 0)

    assertFailsWith<IllegalArgumentException> {
      controller.playMoveOrToggleDeadStone(move)
    }

  }

  @Test
  fun testToString() {
    assertThat(controller.toString()).isNotNull()
  }

  @Test
  fun testPlayMove_tooLate() {
    val pass = GAME_DATAS.createPassMove()
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(0, 0))).isFalse()
  }

  @Test
  fun testUndoRedo() {
    val move = GAME_DATAS.createMove(0, 0)
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue()
    assertThat(controller.undo()).isTrue()
    assertThat(controller.gameData.moveList).isEmpty()
    assertThat(controller.redo()).isTrue()
    assertThat(controller.gameData.moveList)
        .isEqualTo(ImmutableList.of<PlayGameData.Move>(move))
  }

  @Test
  fun testUndo_empty() {
    assertThat(controller.undo()).isFalse()
  }

  @Test
  fun testRedoSame() {
    val move1 = GAME_DATAS.createMove(1, 0)
    val move2 = GAME_DATAS.createMove(2, 0)
    assertThat(controller.playMoveOrToggleDeadStone(move1)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(move2)).isTrue()
    assertThat(controller.undo()).isTrue()
    assertThat(controller.undo()).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(move1)).isTrue()
    assertThat(controller.redo()).isTrue()
  }

  @Test
  fun testRedoDifferent() {
    val move1 = GAME_DATAS.createMove(1, 0)
    val move2 = GAME_DATAS.createMove(2, 0)
    assertThat(controller.playMoveOrToggleDeadStone(move1)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(move2)).isTrue()
    assertThat(controller.undo()).isTrue()
    assertThat(controller.undo()).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(move2)).isTrue()
    assertThat(controller.redo()).isFalse()
  }

  @Test
  fun resign() {
    controller.resign()

    assertThat(controller.phase).isEqualTo(Phase.FINISHED)
    assertThat(controller.winner).isEqualTo(WHITE_PLAYER)
    val score = PlayGameData.Score.newBuilder().setWinner(PlayGameData.Color.WHITE).setResigned(true).build()
    verify<Analytics>(analytics).gameFinished(any<PlayGameData.GameConfiguration>(PlayGameData.GameConfiguration::class.java), eq<PlayGameData.Score>(score))
  }

  @Test
  fun testCheckForMatchEnd() {
    assertThat(controller.phase).isEqualTo(Phase.IN_GAME)
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove())).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createPassMove())).isTrue()
    assertThat(controller.phase).isEqualTo(Phase.DEAD_STONE_MARKING)
  }

  @Test
  fun testToggleDeadStone() {
    val move = GAME_DATAS.createMove(1, 1)
    val pass = GAME_DATAS.createPassMove()
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue()
    assertThat(controller.gameData.matchEndStatus.deadStoneList).isEmpty()
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue()
    assertThat(controller.gameData.matchEndStatus.deadStoneList).isEqualTo(ImmutableList.of<PlayGameData.Position>(GAME_DATAS.createPosition(1, 1)))
    assertThat(controller.playMoveOrToggleDeadStone(move)).isTrue()
    assertThat(controller.gameData.matchEndStatus.deadStoneList).isEmpty()
  }

  @Test
  fun testToggleDeadStone_empty() {
    val pass = GAME_DATAS.createPassMove()
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(pass)).isTrue()
    assertThat(controller.playMoveOrToggleDeadStone(GAME_DATAS.createMove(1, 1))).isFalse()
  }

  @Test
  fun commitConfiguration_configNotChanged_noHandicap_startsGame() {
    val initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION).build()
    controller.gameData = initialGameData
    assertThat(controller.phase).isEqualTo(Phase.CONFIGURATION)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.BLACK)

    controller.commitConfiguration()

    assertThat(controller.phase).isEqualTo(Phase.IN_GAME)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.BLACK)
    verify<Analytics>(analytics).configurationChanged(any(GameData::class.java))
  }

  @Test
  fun commitConfiguration_configNotChanged_handicap_startsGame() {
    val initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION)
    initialGameData.gameConfigurationBuilder.handicap = 2
    controller.gameData = initialGameData.build()
    assertThat(controller.phase).isEqualTo(Phase.CONFIGURATION)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.BLACK)

    controller.commitConfiguration()

    assertThat(controller.phase).isEqualTo(Phase.IN_GAME)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.WHITE)
    verify<Analytics>(analytics).configurationChanged(any(GameData::class.java))
  }

  @Test
  fun commitConfiguration_local_configChanged() {
    val initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION).build()
    controller.gameData = initialGameData
    assertThat(controller.phase).isEqualTo(Phase.CONFIGURATION)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.BLACK)

    controller.setBoardSize(6)
    controller.commitConfiguration()

    assertThat(controller.phase).isEqualTo(Phase.IN_GAME)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.BLACK)
    verify<Analytics>(analytics).configurationChanged(any(GameData::class.java))
  }

  @Test
  fun commitConfiguration_remote_configChanged() {
    val initialGameData = gameDataBuilder.setPhase(Phase.CONFIGURATION)
    initialGameData.gameConfigurationBuilder.gameType = PlayGameData.GameType.REMOTE
    controller.gameData = initialGameData.build()
    assertThat(controller.phase).isEqualTo(Phase.CONFIGURATION)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.BLACK)

    controller.setBoardSize(6)
    controller.commitConfiguration()

    assertThat(controller.phase).isEqualTo(Phase.CONFIGURATION)
    assertThat(controller.currentColor).isEqualTo(PlayGameData.Color.WHITE)
    verify<Analytics>(analytics).configurationChanged(any(GameData::class.java))
  }

  @Test
  fun swapPlayer() {
    assertThat(controller.getPlayerForColor(PlayGameData.Color.BLACK)).isEqualTo(BLACK_PLAYER)
    assertThat(controller.getPlayerForColor(PlayGameData.Color.WHITE)).isEqualTo(WHITE_PLAYER)

    controller.swapPlayers()

    assertThat(controller.getPlayerForColor(PlayGameData.Color.BLACK)).isEqualTo(WHITE_PLAYER)
    assertThat(controller.getPlayerForColor(PlayGameData.Color.WHITE)).isEqualTo(BLACK_PLAYER)
  }

  @Test
  fun getMatchId() {
    controller.gameData = gameDataBuilder.setMatchId(MATCH_ID).build()
    assertThat(controller.matchId).isEqualTo(MATCH_ID)
  }

  @Test
  fun setWhitePlayerName() {
    controller.setWhitePlayerName(ANOTHER_PLAYER_NAME)
    assertThat(controller.getPlayerForColor(PlayGameData.Color.WHITE).name).isEqualTo(ANOTHER_PLAYER_NAME)
  }

  @Test
  fun setBlackPlayerName() {
    controller.setBlackPlayerName(ANOTHER_PLAYER_NAME)
    assertThat(controller.getPlayerForColor(PlayGameData.Color.BLACK).name).isEqualTo(ANOTHER_PLAYER_NAME)
  }

  @Test
  fun setKomi() {
    controller.setKomi(3.14f)

    val gameConfiguration = controller.gameData.gameConfiguration
    assertThat(gameConfiguration.komi).isEqualTo(3.14f)
  }

  @Test
  fun setHandicap() {
    controller.setHandicap(12)

    val gameConfiguration = controller.gameData.gameConfiguration
    assertThat(gameConfiguration.handicap).isEqualTo(12)
  }


  @Test
  fun pass() {
    assertThat(controller.isLastMovePass).isFalse()

    controller.pass()

    assertThat(controller.isLastMovePass).isTrue()
  }

  companion object {

    private val MATCH_ID = "pizza"
    private val GAME_DATAS = GameDatas()
    private val WHITE_PLAYER = GAME_DATAS.createGamePlayer("bimbo", "player2", true)
    private val BLACK_PLAYER = GAME_DATAS.createGamePlayer("pipo", "player1", true)
    private val ANOTHER_PLAYER_NAME = "myname"
  }
}
