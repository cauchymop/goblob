package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.model.createGameData
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.INITIAL
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SingleGamePresenterTest {

    @Mock lateinit var gameRepository: GameRepository
    @Mock lateinit var achievementManager: AchievementManager
    @Mock lateinit var gameViewUpdater: GameViewUpdater
    @Mock lateinit var goGameController: GoGameController

    lateinit var gamePresenter:SingleGamePresenter

    @Before
    fun setUp() {
        gamePresenter = SingleGamePresenter(gameRepository, achievementManager, goGameController, gameViewUpdater)
        reset(gameRepository, achievementManager, goGameController, gameViewUpdater)
    }

    @After
    fun tearDown() {
        verify(goGameController, Mockito.atLeast(0)).matchId
        Mockito.verifyNoMoreInteractions(gameRepository, achievementManager, goGameController, gameViewUpdater)
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
        given(goGameController.matchId).willReturn(matchId)
    }
}