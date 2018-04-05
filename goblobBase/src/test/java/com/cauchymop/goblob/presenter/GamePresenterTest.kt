package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.*
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.view.GameView

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.INITIAL
import com.cauchymop.goblob.view.InGameView
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

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
  @Throws(Exception::class)
  fun setUp() {
    gamePresenter = createGamePresenter()
    gamePresenter!!.view = view!!
    reset<Any>(view, gameViewUpdater, gameRepository)
  }

  @After
  @Throws(Exception::class)
  fun tearDown() {
    verifyNoMoreInteractions(analytics, gameRepository, achievementManager, gameViewUpdater, view, goGameController)
  }

  @Test
  fun setView_registersAsGameRepositoryListener() {
    gamePresenter!!.view = view!!

    verify<GameViewUpdater>(gameViewUpdater).view = view
    verify<GameRepository>(gameRepository).addGameSelectionListener(gamePresenter!!)
  }


  @Test
  @Throws(Exception::class)
  fun gameSelected_updatesView() {
    val gameData = createGameData().setMatchId("pipo").setPhase(INITIAL).build()

    gamePresenter!!.gameSelected(gameData)

    //    verify(gameViewUpdater).setGoGameController(any());
    verify<GameView>(view).setConfigurationViewListener(any<ConfigurationEventListener>())
    verify<GameView>(view).setInGameActionListener(any<InGameView.InGameEventListener>())
    verify<GoGameController>(goGameController).setGameData(gameData)

    verify<GameRepository>(gameRepository).addGameChangeListener(any<GameChangeListener>())
    verify<AchievementManager>(achievementManager).updateAchievements(goGameController!!)
    verify<GameViewUpdater>(gameViewUpdater).update()
  }

  @Test
  @Throws(Exception::class)
  fun gameSelected_withNullGameData_noPreviousGame_doesNothing() {

    gamePresenter!!.gameSelected(null)

  }

  @Test
  @Throws(Exception::class)
  fun gameSelected_withNullGameData_andPreviousGame_clearsPreviousGame() {
    setInitialGame()

    gamePresenter!!.gameSelected(null)

    // From SingleGamePresenter.clear()
    verify<GameRepository>(gameRepository).removeGameChangeListener(any<GameChangeListener>())

  }

  @Test
  @Throws(Exception::class)
  fun gameSelected_withPreviousGame_clearPreviousGame_andUpdatesView() {
    setInitialGame()
    val selectedGameData = createGameData().setMatchId("pipo").setPhase(INITIAL).build()

    gamePresenter!!.gameSelected(selectedGameData)

    //    verify(gameViewUpdater).setGoGameController(any());
    verify<GameView>(view).setConfigurationViewListener(any<ConfigurationEventListener>())
    verify<GameView>(view).setInGameActionListener(any<InGameView.InGameEventListener>())
    verify<GoGameController>(goGameController).setGameData(selectedGameData)

    verify<GameRepository>(gameRepository).addGameChangeListener(any<GameChangeListener>())
    verify<AchievementManager>(achievementManager).updateAchievements(goGameController!!)
    verify<GameViewUpdater>(gameViewUpdater).update()
    // From SingleGamePresenter.clear()
    verify<GameRepository>(gameRepository).removeGameChangeListener(any<GameChangeListener>())
  }

  @Test
  @Throws(Exception::class)
  fun clear_withNoGame() {
    gamePresenter!!.clear()

    verify<GameRepository>(gameRepository).removeGameSelectionListener(gamePresenter!!)
    verify<GameView>(view).setConfigurationViewListener(null)
    verify<GameView>(view).setInGameActionListener(null)
  }

  @Test
  @Throws(Exception::class)
  fun clear_withGame() {
    setInitialGame()

    gamePresenter!!.clear()

    verify<GameRepository>(gameRepository).removeGameSelectionListener(gamePresenter!!)
    verify<GameView>(view).setConfigurationViewListener(null)
    verify<GameView>(view).setInGameActionListener(null)
    // From SingleGamePresenter.clear()
    verify<GameRepository>(gameRepository).removeGameChangeListener(any<GameChangeListener>())
  }

  @Test
  @Throws(Exception::class)
  fun gameChanged_withDifferentMatchId_doesNothing() {
    setInitialGame("pizza")

    gamePresenter.gameChanged(createGameData().setMatchId("pipo").build())

    // Does nothing.
  }

  @Test
  @Throws(Exception::class)
  fun gameChanged_withSameMatchId() {
    setInitialGame("pizza")

    gamePresenter.gameChanged(createGameData().setMatchId("pizza").setPhase(INITIAL).build())

    verify(gameViewUpdater).update()
    verify(achievementManager).updateAchievements(goGameController)
  }

  @Test
  fun clear() {
    gamePresenter.clear()

    verify(gameRepository).removeGameChangeListener(gamePresenter)
  }

  private fun setInitialGame(matchId: String) {
    BDDMockito.given(goGameController.matchId).willReturn(matchId)
  }

  fun setInitialGame() {
    gamePresenter!!.gameSelected(createGameData().build())
    reset<Any>(gameRepository, achievementManager, gameViewUpdater, view, goGameController)
  }

  private fun createGamePresenter(): GamePresenter {
    return GamePresenter(GAME_DATAS,
        analytics,
        gameRepository,
        achievementManager,
        gameViewUpdater,
        feedbackSender,
        goGameController)
  }

  companion object {
    private val GAME_DATAS = GameDatas()
  }
}
