package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.model.*
import com.cauchymop.goblob.presenter.GameMessageGenerator
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.*
import com.cauchymop.goblob.proto.PlayGameData.Color.BLACK
import com.cauchymop.goblob.proto.PlayGameData.Color.WHITE
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.*
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertFailsWith

private val GAMEDATAS: GameDatas = GameDatas()

@RunWith(MockitoJUnitRunner::class)
class InGameViewModelsTest {

  @Mock(answer = Answers.RETURNS_SMART_NULLS)
  private lateinit var gameMessageGenerator: GameMessageGenerator

  private lateinit var goGameController: GoGameController

  private lateinit var inGameViewModels: InGameViewModels

  private lateinit var goGame: GoGame

  @Before
  fun setUp() {
    inGameViewModels = InGameViewModels(GAMEDATAS, gameMessageGenerator)
    goGameController = GoGameController(GAMEDATAS, mock(Analytics::class.java));
    goGame = GoGame(9, 0)
  }

  @After
  fun tearDown() {
  }

  @Test
  fun from_gameInConfigurationState_throws() {
    goGameController.gameData = createGameData(IN_GAME).setPhase(CONFIGURATION).build()

    assertFailsWith<IllegalArgumentException> { inGameViewModels.from(goGameController) }
  }

  @Test
  fun from_boardViewModel_boardSize_mappedSuccessfully() {
    goGameController.gameData = createGameData(IN_GAME).apply { gameConfigurationBuilder.boardSize = 17 }.build()

    val actual = inGameViewModels.from(goGameController)

    assertThat(actual.boardViewModel.boardSize).isEqualTo(17)
  }

  @Test
  fun from_boardViewModel_stones_mappedSuccessfully() {
    val mockController = getMockGoGameController()
    val mockGoGame = spy(goGame)
    given(mockGoGame.board).willReturn(GoBoard(9).apply {
      fill(boardString =
          ".........\n" +
          ".........\n" +
          "....●....\n" +
          ".........\n" +
          "......○..\n" +
          ".........\n" +
          ".........\n" +
          ".........\n" +
          ".........\n")
    })
    given(mockController.game).willReturn(mockGoGame)


    val actual = inGameViewModels.from(mockController)

    assertThat(actual.boardViewModel.getColor(4, 2)).isEqualTo(BLACK)
    assertThat(actual.boardViewModel.getColor(6, 4)).isEqualTo(WHITE)
    assertThat(actual.boardViewModel.getColor(3, 3)).isNull()
  }

  @Test
  fun from_boardViewModel_noGame_throws() {
    val mockController = getMockGoGameController()
    given(mockController.game).willReturn(null)

    assertFailsWith<KotlinNullPointerException> { inGameViewModels.from(mockController) }
  }

  @Test
  fun from_boardViewModel_territories_mappedSuccessfully() {
    val mockController = getMockGoGameController()
    given(mockController.score).willReturn(Score.newBuilder()
        .setWinner(WHITE)
        .addBlackTerritory(GAMEDATAS.createPosition(1,2))
        .addWhiteTerritory(GAMEDATAS.createPosition(3,4))
        .build())

    val actual = inGameViewModels.from(mockController)

    assertThat(actual.boardViewModel.getTerritory(1, 2)).isEqualTo(BLACK)
    assertThat(actual.boardViewModel.getTerritory(3, 4)).isEqualTo(WHITE)
    assertThat(actual.boardViewModel.getTerritory(3, 3)).isNull()
  }

  @Test
  fun from_boardViewModel_deadStones_mappedSuccessfully() {
    val mockController = getMockGoGameController()
    given(mockController.deadStones).willReturn(listOf(GAMEDATAS.createPosition(1,2), GAMEDATAS.createPosition(5,6)))
    val mockGoGame = spy(goGame)
    given(mockGoGame.board).willReturn(GoBoard(9).apply {
      fill(boardString =
          ".........\n" +
          ".........\n" +
          ".●.......\n" +
          ".........\n" +
          "...○.....\n" +
          ".........\n" +
          ".....○...\n" +
          ".........\n" +
          ".........\n")
    })
    given(mockController.game).willReturn(mockGoGame)

    val actual = inGameViewModels.from(mockController)

    assertThat(actual.boardViewModel.getTerritory(1, 2)).isEqualTo(WHITE)
    assertThat(actual.boardViewModel.getTerritory(3, 4)).isNull()
    assertThat(actual.boardViewModel.getTerritory(5, 6)).isEqualTo(BLACK)
    assertThat(actual.boardViewModel.getTerritory(3, 3)).isNull()
  }

  @Test
  fun from_boardViewModel_lastMove_mappedSuccessfully() {
    val mockController = getMockGoGameController()
    val mockGoGame = spy(goGame)
    given(mockGoGame.lastMove).willReturn(goGame.getPos(3,4))
    given(mockController.game).willReturn(mockGoGame)

    val actual = inGameViewModels.from(mockController)

    assertThat(actual.boardViewModel.isLastMove(3,4)).isTrue()
    assertThat(actual.boardViewModel.isLastMove(4,5)).isFalse()
  }

  @Test
  fun from_boardViewModel_isInteractive() {
    val mockController = getMockGoGameController()
    given(mockController.isLocalTurn).willReturn(true)

    val actual = inGameViewModels.from(mockController)

    assertThat(actual.boardViewModel.isInteractive).isTrue()
  }

  @Test
  fun from_boardViewModel_isNotInteractive() {
    val mockController = getMockGoGameController()
    given(mockController.isLocalTurn).willReturn(false)

    val actual = inGameViewModels.from(mockController)

    assertThat(actual.boardViewModel.isInteractive).isFalse()
  }


  @Test
  fun from_currentPlayerViewModel_mappedSuccessfully() {
    val mockContoller = getMockGoGameController()
    given(mockContoller.currentPlayer).willReturn(getPlayer(name = "🍕"))
    given(mockContoller.currentColor).willReturn(WHITE)

    val actual = inGameViewModels.from(mockContoller)

    assertThat(actual.currentPlayerViewModel).isEqualTo(PlayerViewModel("🍕", WHITE))
  }

  @Test
  fun from_message_gameFinished_resigned() {
    val mockContoller = getMockGoGameController()
    given(mockContoller.isGameFinished).willReturn(true)
    given(mockContoller.getPlayerForColor(WHITE)).willReturn(getPlayer(name = "The Winner"))
    given(mockContoller.score).willReturn(Score.newBuilder()
        .setWinner(WHITE)
        .setResigned(true)
        .build())
    given(gameMessageGenerator.getGameResignedMessage(anyString())).willReturn("resigned")

    val actual = inGameViewModels.from(mockContoller)

    assertThat(actual.message).isEqualTo("resigned")
    verify(gameMessageGenerator).getGameResignedMessage("The Winner")
  }

  @Test
  fun from_message_gameFinished_won() {
    val mockContoller = getMockGoGameController()
    given(mockContoller.isGameFinished).willReturn(true)
    given(mockContoller.getPlayerForColor(WHITE)).willReturn(getPlayer(name = "The Winner"))
    given(mockContoller.score).willReturn(Score.newBuilder()
        .setWinner(WHITE)
        .setResigned(false)
        .setWonBy(3.5f)
        .build())
    given(gameMessageGenerator.getEndOfGameMessage(anyString(), anyFloat())).willReturn("Olivier won (easily) against Jérôme")

    val actual = inGameViewModels.from(mockContoller)

    assertThat(actual.message).isEqualTo("Olivier won (easily) against Jérôme")
    verify(gameMessageGenerator).getEndOfGameMessage("The Winner", 3.5f)
  }

  @Test
  fun from_message_deadStoneMarkingPhase() {
    goGameController.gameData = createGameData(IN_GAME).setPhase(DEAD_STONE_MARKING).build()
    given(gameMessageGenerator.stoneMarkingMessage).willReturn("Stone marking time!")

    val actual = inGameViewModels.from(goGameController)

    assertThat(actual.message).isEqualTo("Stone marking time!")
  }

  @Test
  fun from_message_opponentPassed() {
    val mockController = getMockGoGameController()
    val mockGoGame = spy(goGame)
    given(mockGoGame.isLastMovePass).willReturn(true)
    given(mockController.game).willReturn(mockGoGame)
    given(mockController.opponent).willReturn(getPlayer(name = "The Loser"))
    given(gameMessageGenerator.getOpponentPassedMessage(anyString())).willReturn("Opponent passed")

    val actual = inGameViewModels.from(mockController)

    assertThat(actual.message).isEqualTo("Opponent passed")
    verify(gameMessageGenerator).getOpponentPassedMessage("The Loser")
  }

  @Test
  fun from_message_noMessage() {
    val mockContoller = getMockGoGameController()

    val actual = inGameViewModels.from(mockContoller)

    assertThat(actual.message).isEqualTo("")
  }

  @Test
  fun from_isPassActionAvailable_false_whenNotLocalTurn() {
    checkIsActionAvailable(phase = IN_GAME, isLocalTurn = false, expectedValue = false) { it.isPassActionAvailable }
  }

  @Test
  fun from_isPassActionAvailable_false_whenPhaseIsDEAD_STONE_MARKING() {
    checkIsActionAvailable(phase = DEAD_STONE_MARKING, isLocalTurn = true, expectedValue = false) { it.isPassActionAvailable }
  }

  @Test
  fun from_isPassActionAvailable_false_whenPhaseIsFINISHED() {
    checkIsActionAvailable(phase = FINISHED, isLocalTurn = true, expectedValue = false) { it.isPassActionAvailable }
  }

  @Test
  fun from_isPassActionAvailable_true_whenLocalTurnAndPhaseIsIN_GAME() {
    checkIsActionAvailable(phase = IN_GAME, isLocalTurn = true, expectedValue = true) { it.isPassActionAvailable }
  }

  @Test
  fun from_isDoneActionAvailable_false_whenNotLocalTurn() {
    checkIsActionAvailable(phase = DEAD_STONE_MARKING, isLocalTurn = false, expectedValue = false) { it.isDoneActionAvailable }
  }

  @Test
  fun from_isDoneActionAvailable_false_whenPhaseIsIN_GAME() {
    checkIsActionAvailable(phase = IN_GAME, isLocalTurn = true, expectedValue = false) { it.isDoneActionAvailable }
  }

  @Test
  fun from_isDoneActionAvailable_false_whenPhaseIsFINISHED() {
    checkIsActionAvailable(phase = FINISHED, isLocalTurn = true, expectedValue = false) { it.isDoneActionAvailable }
  }

  @Test
  fun from_isDoneActionAvailable_true_whenLocalTurnAndPhaseIsDEAD_STONE_MARKING() {
    checkIsActionAvailable(phase = DEAD_STONE_MARKING, isLocalTurn = true, expectedValue = true) { it.isDoneActionAvailable }
  }

  @Test
  fun from_isResignActionAvailable_false_whenNotLocalTurn() {
    checkIsActionAvailable(phase = IN_GAME, isLocalTurn = false, expectedValue = false) { it.isResignActionAvailable }
  }

  @Test
  fun from_isResignActionAvailable_true_whenLocalTurnAndPhaseIsIN_GAME() {
    checkIsActionAvailable(phase = IN_GAME, isLocalTurn = true, expectedValue = true) { it.isResignActionAvailable }
  }

  @Test
  fun from_isResignActionAvailable_true_whenLocalTurnAndPhaseIsDEAD_STONE_MARKING() {
    checkIsActionAvailable(phase = DEAD_STONE_MARKING, isLocalTurn = true, expectedValue = true) { it.isResignActionAvailable }
  }

  @Test
  fun from_isResignActionAvailable_true_whenLocalTurnAndPhaseIsFINISHED() {
    checkIsActionAvailable(phase = FINISHED, isLocalTurn = true, expectedValue = true) { it.isResignActionAvailable }
  }

  @Test
  fun from_isUndoActionAvailable_matches_GoGameControllerCanUndo() {
    checkActionMatchesForAllControllerValues({ it.canUndo() }, { it.isUndoActionAvailable })
  }

  @Test
  fun from_isRedoActionAvailable_matches_GoGameControllerCanRedo() {
    checkActionMatchesForAllControllerValues({ it.canRedo() }, { it.isRedoActionAvailable })
  }

  private fun checkActionMatchesForAllControllerValues(actionToMock: (GoGameController) -> (Boolean), actionToAssert: (InGameViewModel) -> (Boolean)) {
    checkActionMatchesForControllerValue(actionToMock, actionToAssert, true)
    checkActionMatchesForControllerValue(actionToMock, actionToAssert, false)
  }

  private fun checkActionMatchesForControllerValue(actionToMock: (GoGameController) -> (Boolean), actionToAssert: (InGameViewModel) -> (Boolean), value: Boolean) {
    val mockController = getMockGoGameController()
    given(actionToMock.invoke(mockController)).willReturn(value)

    val actual = inGameViewModels.from(mockController)

    assertThat(actionToAssert.invoke(actual)).isEqualTo(value)
  }

  private fun checkIsActionAvailable(phase: PlayGameData.GameData.Phase, isLocalTurn: Boolean, expectedValue: Boolean, actionToAssert: (InGameViewModel) -> (Boolean)) {
    val mockController = getMockGoGameController()
    given(mockController.phase).willReturn(phase)
    given(mockController.isLocalTurn).willReturn(isLocalTurn)

    val actual = inGameViewModels.from(mockController)

    assertThat(actionToAssert.invoke(actual)).isEqualTo(expectedValue)
  }

  private fun getMockGoGameController(): GoGameController {
    goGameController.gameData = createGameData(IN_GAME).build()
    val mockContoller = spy(goGameController)
    return mockContoller
  }

  private fun getPlayer(name: String) = GoPlayer.newBuilder().setName(name).setId("id").build()
}