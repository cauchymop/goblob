package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.*
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.INITIAL
import com.cauchymop.goblob.view.GameView
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GamePresenterTest {

  @Mock
  private lateinit var analytics: Analytics

  @Mock
  private lateinit var gameRepository: GameRepository

  @Mock
  private lateinit var achievementManager: AchievementManager

  @Mock
  private lateinit var view: GameView

  @Mock
  private lateinit var configurationViewEventProcessor: ConfigurationViewEventProcessor

  @Mock
  private lateinit var inGameViewEventProcessor: InGameViewEventProcessor

  @Mock
  private lateinit var goGameController: GoGameController

  @Mock
  private lateinit var feedbackSender: FeedbackSender

  @Mock
  private lateinit var gameViewUpdater: GameViewUpdater


  private lateinit var gamePresenter: GamePresenter

  @Before
  fun setUp() {
    gamePresenter = createGamePresenter()
    gamePresenter.view = view
    reset<Any>(view, gameViewUpdater, gameRepository, achievementManager, goGameController)
  }

  @After
  fun tearDown() {
    verify(goGameController, atLeast(0)).matchId
    verifyNoMoreInteractions(analytics, gameRepository, achievementManager, gameViewUpdater, view, goGameController)
  }

  @Test
  fun setView() {
    gamePresenter.view = view

    verify(gameViewUpdater).view = view
    verify(gameRepository).addGameSelectionListener(gamePresenter)
    verify(gameRepository).addGameChangeListener(gamePresenter)
    verify(view).setConfigurationViewListener(configurationViewEventProcessor)
    verify(view).setInGameActionListener(inGameViewEventProcessor)
    // from gameUpdated
    verify(achievementManager).updateAchievements(goGameController)
    verify(gameViewUpdater).update()
  }


  @Test
  fun gameSelected_withGameData() {
    val gameData = createGameData().setMatchId("pipo").setPhase(INITIAL).build()

    gamePresenter.gameSelected(gameData)

    verify(goGameController).gameData = gameData
    verify(gameViewUpdater).update()
  }

  @Test
  fun gameSelected_withNoGameData_doesNothing() {

    gamePresenter.gameSelected(null)

  }


  @Test
  fun gameChanged_withDifferentMatchId_doesNothing() {
    setInitialGame("pizza")

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").build())

    // Does nothing.
  }

  @Test
  fun clear() {
    gamePresenter.clear()

    verify(gameRepository).removeGameChangeListener(gamePresenter)
    verify(gameRepository).removeGameSelectionListener(gamePresenter)
    verify(view).setConfigurationViewListener(null)
    verify(view).setInGameActionListener(null)
  }

  @Test
  fun gameChanged_withSameMatchId() {
    setInitialGame("pizza")

    gamePresenter.gameChanged(createGameData().setMatchId("pizza").setPhase(INITIAL).build())

    verify(gameViewUpdater).update()
    verify(achievementManager).updateAchievements(goGameController)
  }

  private fun setInitialGame(matchId: String) {
    BDDMockito.given(goGameController.matchId).willReturn(matchId)
  }

  fun setInitialGame() {
    gamePresenter.gameSelected(createGameData().build())
    reset<Any>(gameRepository, achievementManager, gameViewUpdater, view, goGameController)
  }

  private fun createGamePresenter(): GamePresenter {
    return GamePresenter(gameRepository,
        achievementManager,
        gameViewUpdater,
        configurationViewEventProcessor,
        inGameViewEventProcessor,
        goGameController)
  }

  companion object {
    private val GAME_DATAS = GameDatas()
  }
}
