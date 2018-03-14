package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.*
import com.cauchymop.goblob.view.GameView
import com.cauchymop.goblob.viewmodel.ConfigurationViewModels
import com.cauchymop.goblob.viewmodel.InGameViewModels
import javax.inject.Inject
import kotlin.properties.Delegates

open class GameViewUpdater @Inject constructor(
        private val configurationViewModels: ConfigurationViewModels,
        private val inGameViewModels: InGameViewModels) {

    var view: GameView by Delegates.notNull<GameView>()
    var goGameController: GoGameController by Delegates.notNull<GoGameController>()

    fun update() = goGameController.let {
        if (isConfigured(it)) view.setInGameViewModel(inGameViewModels.from(it))
        else view.setConfigurationViewModel(configurationViewModels.from(it))
    }

    private fun isConfigured(goGameController: GoGameController): Boolean = with(goGameController) {
        when (phase) {
            INITIAL, CONFIGURATION -> false
            IN_GAME, DEAD_STONE_MARKING, FINISHED -> true
            UNKNOWN, null -> throw IllegalArgumentException("Invalid phase for game: " + phase)
        }
    }

}