package com.cauchymop.goblob.presenter

import javax.inject.Inject

class ConfigurationViewEventProcessor @Inject constructor() : GameEventProcessor(), ConfigurationEventListener {

    override fun onBlackPlayerNameChanged(blackPlayerName: String) =
        goGameController.setBlackPlayerName(blackPlayerName)

    override fun onWhitePlayerNameChanged(whitePlayerName: String) =
        goGameController.setWhitePlayerName(whitePlayerName)

    override fun onHandicapChanged(handicap: Int) =
        goGameController.setHandicap(handicap)

    override fun onKomiChanged(komi: Float) =
        goGameController.setKomi(komi)

    override fun onBoardSizeChanged(boardSize: Int) =
        goGameController.setBoardSize(boardSize)

    override fun onSwapEvent() {
        goGameController.swapPlayers()
        helper.updateView()
    }

    override fun onConfigurationValidationEvent() {
        goGameController.validateConfiguration()
        helper.commitGameChanges()
    }

}