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

    @Test
    fun onWhitePlayerNameChanged() {
        configurationViewEventProcessor.onWhitePlayerNameChanged("whity")

        verify(goGameController).setWhitePlayerName("whity")
    }

    @Test
    fun onHandicapChanged() {
    }

    @Test
    fun onKomiChanged() {
    }

    @Test
    fun onBoardSizeChanged() {
    }

    @Test
    fun onSwapEvent() {
    }

    @Test
    fun onConfigurationValidationEvent() {
    }

}