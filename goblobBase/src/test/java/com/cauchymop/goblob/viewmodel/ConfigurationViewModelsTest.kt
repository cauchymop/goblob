package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.model.createGameData
import com.cauchymop.goblob.presenter.GameMessageGenerator
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private val GAMEDATAS: GameDatas = GameDatas()

private const val INITIAL = "initial"
private const val ACCEPT_CHANGE = "accept-change"
private const val WAITING = "waiting"

@RunWith(MockitoJUnitRunner::class)
class ConfigurationViewModelsTest {

  @Mock
  private lateinit var gameMessageGenerator: GameMessageGenerator

  private lateinit var goGameController: GoGameController

  private lateinit var configurationViewModels: ConfigurationViewModels

  @Before
  fun setUp() {
    goGameController = GoGameController(GAMEDATAS, mock(Analytics::class.java));
    given(gameMessageGenerator.configurationMessageInitial).willReturn(INITIAL)
    given(gameMessageGenerator.configurationMessageAcceptOrChange).willReturn(ACCEPT_CHANGE)
    given(gameMessageGenerator.configurationMessageWaitingForOpponent).willReturn(WAITING)

    configurationViewModels = ConfigurationViewModels(gameMessageGenerator)
  }

  @After
  fun tearDown() {
  }

  @Test
  fun from_komi() {
    goGameController.gameData = createGameData(Phase.IN_GAME).apply { gameConfigurationBuilder.komi = 3.14f }.build()

    val actual = configurationViewModels.from(goGameController)

    assertThat(actual.komi).isEqualTo(3.14f)
  }

  @Test
  fun from_boardsize() {
    goGameController.gameData = createGameData(Phase.IN_GAME).apply { gameConfigurationBuilder.boardSize = 17 }.build()

    val actual = configurationViewModels.from(goGameController)

    assertThat(actual.boardSize).isEqualTo(17)
  }

  @Test
  fun from_handicap() {
    goGameController.gameData = createGameData(Phase.IN_GAME).apply { gameConfigurationBuilder.handicap = 14 }.build()

    val actual = configurationViewModels.from(goGameController)

    assertThat(actual.handicap).isEqualTo(14)
  }

  @Test
  fun from_blackPlayerName() {
    goGameController.gameData = createGameData(Phase.IN_GAME).apply { gameConfigurationBuilder.black = getPlayer("Blacky") }.build()

    val actual = configurationViewModels.from(goGameController)

    assertThat(actual.blackPlayerName).isEqualTo("Blacky")
  }

  @Test
  fun from_whitePlayerName() {
    goGameController.gameData = createGameData(Phase.IN_GAME).apply { gameConfigurationBuilder.white = getPlayer("Whitey") }.build()

    val actual = configurationViewModels.from(goGameController)

    assertThat(actual.whitePlayerName).isEqualTo("Whitey")
  }

  @Test
  fun from_message_initial() {
    goGameController.gameData = createGameData(Phase.INITIAL).build()

    val actual = configurationViewModels.from(goGameController)

    assertThat(actual.message).isEqualTo(INITIAL)
  }

  @Test
  fun from_message_acceptOrChange() {
    val mockController = getMockGoGameController()
    given(mockController.phase).willReturn(Phase.CONFIGURATION)
    given(mockController.isLocalTurn).willReturn(true)

    val actual = configurationViewModels.from(mockController)

    assertThat(actual.message).isEqualTo(ACCEPT_CHANGE)
  }

  @Test
  fun from_message_waiting() {
    val mockController = getMockGoGameController()
    given(mockController.phase).willReturn(Phase.CONFIGURATION)
    given(mockController.isLocalTurn).willReturn(false)

    val actual = configurationViewModels.from(mockController)

    assertThat(actual.message).isEqualTo(WAITING)
  }

  @Test
  fun from_interactionsEnabled_true_whenLocalTurnTrue() {
    val mockController = getMockGoGameController()
    given(mockController.isLocalTurn).willReturn(true)

    val actual = configurationViewModels.from(mockController)

    assertThat(actual.interactionsEnabled).isTrue()
  }

  @Test
  fun from_interactionsEnabled_false_whenLocalTurnFalse() {
    val mockController = getMockGoGameController()
    given(mockController.isLocalTurn).willReturn(false)

    val actual = configurationViewModels.from(mockController)

    assertThat(actual.interactionsEnabled).isFalse()
  }

  private fun getMockGoGameController(): GoGameController {
    goGameController.gameData = createGameData(Phase.CONFIGURATION).build()
    val mockContoller = spy(goGameController)
    return mockContoller
  }

  private fun getPlayer(name: String) = GoPlayer.newBuilder().setName(name).setId("id").build()
}