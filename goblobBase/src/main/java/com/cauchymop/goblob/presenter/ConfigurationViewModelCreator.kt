package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.INITIAL
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel
import javax.inject.Inject

class ConfigurationViewModelCreator @Inject constructor(val gameMessageGenerator: GameMessageGenerator) {

    fun getConfigurationViewModel(goGameController: GoGameController): ConfigurationViewModel {
        val gameConfiguration = goGameController.getGameConfiguration()
        return ConfigurationViewModel(
                komi = gameConfiguration.komi,
                boardSize = gameConfiguration.boardSize,
                handicap = gameConfiguration.handicap,
                blackPlayerName = gameConfiguration.black.name,
                whitePlayerName = gameConfiguration.white.name,
                message = getMessage(goGameController),
                interactionsEnabled = goGameController.isLocalTurn())
    }

    private fun getMessage(goGameController: GoGameController): String = when {
        goGameController.phase == INITIAL -> gameMessageGenerator.configurationMessageInitial
        goGameController.isLocalTurn -> gameMessageGenerator.configurationMessageAcceptOrChange
        else -> gameMessageGenerator.configurationMessageWaitingForOpponent
    }
}