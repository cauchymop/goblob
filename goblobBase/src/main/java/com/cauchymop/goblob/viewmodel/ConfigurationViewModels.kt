package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.presenter.GameMessageGenerator
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.INITIAL
import javax.inject.Inject

open class ConfigurationViewModels @Inject constructor(val gameMessageGenerator: GameMessageGenerator) {

    fun from(goGameController: GoGameController) = with(goGameController.gameConfiguration) {
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