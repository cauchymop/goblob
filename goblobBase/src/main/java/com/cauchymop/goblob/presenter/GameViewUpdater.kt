package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.*
import com.cauchymop.goblob.view.GameView
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel
import com.cauchymop.goblob.viewmodel.ConfigurationViewModels
import com.cauchymop.goblob.viewmodel.InGameViewModel
import com.cauchymop.goblob.viewmodel.InGameViewModels
import javax.inject.Inject

open class GameViewUpdater @Inject constructor(private val configurationViewModels: ConfigurationViewModels,
                                          private val inGameViewModels: InGameViewModels) {
    fun update(goGameController: GoGameController?, view: GameView) = goGameController?.let {
        if (isConfigured(it)) view.setInGameViewModel(inGameViewModel(it))
        else view.setConfigurationViewModel(configurationViewModel(it))
    }

    private fun isConfigured(goGameController: GoGameController): Boolean = with(goGameController) {
        when (phase) {
            INITIAL, CONFIGURATION -> return false
            IN_GAME, DEAD_STONE_MARKING, FINISHED -> return true
            else -> throw RuntimeException("Invalid phase for game: " + phase)
        }
    }

    private fun configurationViewModel(goGameController: GoGameController): ConfigurationViewModel =
            configurationViewModels.from(goGameController)

    private fun inGameViewModel(goGameController: GoGameController): InGameViewModel =
            inGameViewModels.from(goGameController)
}