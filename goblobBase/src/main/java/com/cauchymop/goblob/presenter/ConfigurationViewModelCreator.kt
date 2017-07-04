package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.INITIAL
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel
import javax.inject.Inject

class ConfigurationViewModelCreator @Inject constructor(val gameMessageGenerator: GameMessageGenerator) {

    fun getConfigurationViewModel(goGameController: GoGameController) = with(goGameController.gameConfiguration) {
        ConfigurationViewModel(
                komi = komi,
                boardSize = boardSize,
                handicap = handicap,
                blackPlayerName = black.name,
                whitePlayerName = white.name,
                message = getMessage(goGameController),
                interactionsEnabled = goGameController.isLocalTurn())
    }

    private fun getMessage(goGameController: GoGameController) = when {
        goGameController.phase == INITIAL -> gameMessageGenerator.configurationMessageInitial
        goGameController.isLocalTurn -> gameMessageGenerator.configurationMessageAcceptOrChange
        else -> gameMessageGenerator.configurationMessageWaitingForOpponent
    }
}