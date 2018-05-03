package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.model.*
import com.cauchymop.goblob.presenter.GameMessageGenerator
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.*
import com.google.common.truth.Truth.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.BDDMockito
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
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
  fun from_currentPlayer() {

  }

  @Test
  fun from_currentPlayerViewModel() {
    goGameController.gameData = createGameData().build()
    val mockContoller = spy(goGameController)
    given(mockContoller.currentPlayer).willReturn(PlayGameData.GoPlayer.newBuilder().setName("🍕").setId("id").build())
    given(mockContoller.currentColor).willReturn(PlayGameData.Color.WHITE)

    val actual = inGameViewModels.from(mockContoller)

    assertThat(actual.currentPlayerViewModel).isEqualTo(PlayerViewModel("🍕", PlayGameData.Color.WHITE))
  }
}