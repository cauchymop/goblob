package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ConfigurationViewEventProcessorTest {

    @Mock private lateinit var gameRepository:GameRepository
    @Mock private lateinit var gameViewUpdater:GameViewUpdater
    @Mock private lateinit var goGameController: GoGameController

    private lateinit var configurationViewEventProcessor: ConfigurationViewEventProcessor

    @Before
    fun setUp() {
        configurationViewEventProcessor = ConfigurationViewEventProcessor(goGameController, gameViewUpdater, gameRepository)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(gameViewUpdater, goGameController, gameRepository)
    }

    @Test
    fun onBlackPlayerNameChanged() {
        configurationViewEventProcessor.onBlackPlayerNameChanged("blacky")

        verify(goGameController).setBlackPlayerName("blacky")
    }

    @Test
    fun onWhitePlayerNameChanged() {
        configurationViewEventProcessor.onWhitePlayerNameChanged("whity")

        verify(goGameController).setWhitePlayerName("whity")
    }

    @Test
    fun onHandicapChanged() {
        configurationViewEventProcessor.onHandicapChanged(3)

        verify(goGameController).setHandicap(3)
    }

    @Test
    fun onKomiChanged() {
        configurationViewEventProcessor.onKomiChanged(6.5F)

        verify(goGameController).setKomi(6.5F)
    }

    @Test
    fun onBoardSizeChanged() {
        configurationViewEventProcessor.onBoardSizeChanged(19)

        verify(goGameController).setBoardSize(19)
    }

    @Test
    fun onSwapEvent() {
        configurationViewEventProcessor.onSwapEvent()

        verify(goGameController).swapPlayers()
        verify(gameViewUpdater).update()
    }

    @Test
    fun onConfigurationValidationEvent() {
        val gameData = PlayGameData.GameData.getDefaultInstance()
        given(goGameController.gameData).willReturn(gameData)

        configurationViewEventProcessor.onConfigurationValidationEvent()

        verify(goGameController).gameData
        verify(goGameController).commitConfiguration()
        verify(gameRepository).commitGameChanges(gameData)
        verify(gameViewUpdater).update()
    }
}