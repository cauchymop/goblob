package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ConfigurationViewEventProcessorTest {

    @Mock private lateinit var gamePresenterHelper: GamePresenterHelper
    @Mock private lateinit var goGameController: GoGameController

    private lateinit var configurationViewEventProcessor: ConfigurationViewEventProcessor

    @Before
    fun setUp() {
        configurationViewEventProcessor = ConfigurationViewEventProcessor()
        configurationViewEventProcessor.helper = gamePresenterHelper
        configurationViewEventProcessor.goGameControllerProvider = { goGameController }
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(gamePresenterHelper, goGameController)
    }

    @Test
    fun onBlackPlayerNameChanged() {
        configurationViewEventProcessor.onBlackPlayerNameChanged("blacky")

        verify(goGameController).setBlackPlayerName("blacky")
    }

    @Test(expected = Exception::class)
    fun onBlackPlayerNameChanged_withNoGameController_shouldThrow() {
        val pizza:() -> GoGameController? = {null}
        configurationViewEventProcessor.goGameControllerProvider = pizza

        configurationViewEventProcessor.onBlackPlayerNameChanged("blacky")

        verify(configurationViewEventProcessor).goGameControllerProvider
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
        verify(gamePresenterHelper).updateView()
    }

    @Test
    fun onConfigurationValidationEvent() {
        configurationViewEventProcessor.onConfigurationValidationEvent()

        verify(goGameController).validateConfiguration()
        verify(gamePresenterHelper).commitGameChanges()
    }
}