package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase
import com.cauchymop.goblob.view.GameView
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel
import com.cauchymop.goblob.viewmodel.ConfigurationViewModels
import com.cauchymop.goblob.viewmodel.InGameViewModel
import com.cauchymop.goblob.viewmodel.InGameViewModels
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GameViewUpdaterTest {
    @Mock private lateinit var gameView: GameView
    @Mock private lateinit var goGameController: GoGameController
    @Mock private lateinit var configurationViewModels: ConfigurationViewModels
    @Mock private lateinit var inGameViewModels: InGameViewModels
    @Mock private lateinit var inGameViewModel: InGameViewModel
    @Mock private lateinit var configurationViewModel: ConfigurationViewModel

    private lateinit var gameViewUpdater: GameViewUpdater

    @Before
    @Throws(Exception::class)
    fun setUp() {
        gameViewUpdater = GameViewUpdater(configurationViewModels, inGameViewModels, goGameController)
//        gameViewUpdater.goGameController = goGameController
        gameViewUpdater.view = gameView
    }

    @Test
    fun update_initial() {
        testUpdateConfigurationViewModelForPhase(Phase.INITIAL)
    }

    @Test
    fun update_configuration() {
        testUpdateConfigurationViewModelForPhase(Phase.CONFIGURATION)
    }

    @Test
    fun update_ingame() {
        testUpdateInGameViewModelForPhase(Phase.IN_GAME)
    }

    @Test
    fun update_deadStoneMarking() {
        testUpdateInGameViewModelForPhase(Phase.DEAD_STONE_MARKING)
    }

    @Test
    fun update_finished() {
        testUpdateInGameViewModelForPhase(Phase.FINISHED)
    }

    @Test(expected = IllegalArgumentException::class)
    fun update_unknown_phase() {
        given(goGameController.phase).willReturn(Phase.UNKNOWN)

        gameViewUpdater.update()
    }

    @Test(expected = IllegalArgumentException::class)
    fun update_null_phase() {
        given(goGameController.phase).willReturn(null)

        gameViewUpdater.update()
    }

    fun testUpdateConfigurationViewModelForPhase(phase: Phase) {
        given(goGameController.phase).willReturn(phase)
        given(configurationViewModels.from(goGameController)).willReturn(configurationViewModel)

        gameViewUpdater.update()

        Mockito.verify(gameView).setConfigurationViewModel(configurationViewModel)
    }

    fun testUpdateInGameViewModelForPhase(phase: Phase) {
        given(goGameController.phase).willReturn(phase)
        given(inGameViewModels.from(goGameController)).willReturn(inGameViewModel)

        gameViewUpdater.update()

        Mockito.verify(gameView).setInGameViewModel(inGameViewModel)
    }

}
