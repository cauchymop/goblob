package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.model.createGameData
import com.cauchymop.goblob.presenter.GameMessageGenerator
import com.cauchymop.goblob.proto.PlayGameData
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

@RunWith(MockitoJUnitRunner::class)
class InGameViewModelsTest {

  @Mock(answer = Answers.RETURNS_SMART_NULLS)
  private lateinit var gameMessageGenerator: GameMessageGenerator

  private lateinit var goGameController: GoGameController

  private lateinit var inGameViewModels: InGameViewModels

  @Before
  fun setUp() {
    val gameDatas = GameDatas()
    inGameViewModels = InGameViewModels(gameDatas, gameMessageGenerator)
    goGameController = GoGameController(gameDatas, mock(Analytics::class.java));
  }

  @After
  fun tearDown() {
  }

  @Test
  fun from_gameInConfigurationState_throws() {
    goGameController.gameData = createGameData().setPhase(CONFIGURATION).build()

    assertFailsWith<IllegalArgumentException> { inGameViewModels.from(goGameController) }
  }

  @Test
  fun from_boardViewModel_mappedSuccessfully() {
    // TODO
  }

  @Test
  fun from_currentPlayerViewModel_mappedSuccessfully() {
    val mockContoller = getMockGoGameController()
    given(mockContoller.currentPlayer).willReturn(PlayGameData.GoPlayer.newBuilder().setName("🍕").setId("id").build())
    given(mockContoller.currentColor).willReturn(PlayGameData.Color.WHITE)

    val actual = inGameViewModels.from(mockContoller)

    assertThat(actual.currentPlayerViewModel).isEqualTo(PlayerViewModel("🍕", PlayGameData.Color.WHITE))
  }

  @Test
  fun from_message_mappedSuccessfully() {
    // TODO
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
    checkActionMatchesForAllControllerValues({it.canUndo()}, {it.isUndoActionAvailable})
  }

  @Test
  fun from_isRedoActionAvailable_matches_GoGameControllerCanRedo() {
    checkActionMatchesForAllControllerValues({it.canRedo()}, {it.isRedoActionAvailable})
  }

  private fun checkActionMatchesForAllControllerValues(actionToMock:(GoGameController) -> (Boolean), actionToAssert:(InGameViewModel) -> (Boolean)) {
    checkActionMatchesForControllerValue(actionToMock, actionToAssert, true)
    checkActionMatchesForControllerValue(actionToMock, actionToAssert, false)
  }

  private fun checkActionMatchesForControllerValue(actionToMock:(GoGameController) -> (Boolean), actionToAssert:(InGameViewModel) -> (Boolean), value:Boolean) {
    val mockController = getMockGoGameController()
    given(actionToMock.invoke(mockController)).willReturn(value)

    val actual = inGameViewModels.from(mockController)

    assertThat(actionToAssert.invoke(actual)).isEqualTo(value)
  }

  private fun checkIsActionAvailable(phase: PlayGameData.GameData.Phase, isLocalTurn: Boolean, expectedValue: Boolean, actionToAssert:(InGameViewModel) -> (Boolean)) {
    val mockController = getMockGoGameController()
    given(mockController.phase).willReturn(phase)
    given(mockController.isLocalTurn).willReturn(isLocalTurn)

    val actual = inGameViewModels.from(mockController)

    assertThat(actionToAssert.invoke(actual)).isEqualTo(expectedValue)
  }

  private fun getMockGoGameController(): GoGameController {
    goGameController.gameData = createGameData().build()
    val mockContoller = spy(goGameController)
    return mockContoller
  }
}